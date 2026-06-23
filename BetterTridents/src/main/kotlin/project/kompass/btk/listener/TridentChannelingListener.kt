package project.kompass.btk.listener

import project.kompass.btk.BTK
import project.kompass.btk.util.TridentUtil
import project.kompass.btk.util.isDamageDealingTool
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.bukkit.Location
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.*
import org.bukkit.persistence.PersistentDataType
import java.util.concurrent.TimeUnit
import java.util.UUID
import java.util.HashMap

class TridentChannelingListener(private val plugin: BTK) : Listener {

    // Thread-safe memory cache that auto-purges after 10 minutes to prevent memory leaks,
    // keeping the items NBT-free so they can merge natively on the ground at full speed.
    private val protectedItemsCache: Cache<UUID, Boolean> = CacheBuilder.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build()

    // Temporary map to track channeling mob deaths during the active tick
    private val recentChannelingDeaths = HashMap<Location, Long>()

    // Handles Channeling logic when a thrown trident hits a target
    @EventHandler
    fun onTridentHit(event: ProjectileHitEvent) {
        val trident = event.entity as? Trident ?: return
        val item = trident.itemStack

        if (item.containsEnchantment(Enchantment.CHANNELING)) {
            val hitEntity = event.hitEntity
            val hitBlock = event.hitBlock

            if (hitEntity != null) {
                val strike = trident.world.spawn(hitEntity.location, LightningStrike::class.java)
                strike.persistentDataContainer.set(TridentUtil.CHANNELING_LIGHTNING_KEY, PersistentDataType.BYTE, 1.toByte())
                (trident.shooter as? Player)?.let { strike.causingPlayer = it }
            } else if (hitBlock != null) {
                val strike = trident.world.spawn(hitBlock.location, LightningStrike::class.java)
                strike.persistentDataContainer.set(TridentUtil.CHANNELING_LIGHTNING_KEY, PersistentDataType.BYTE, 1.toByte())
                (trident.shooter as? Player)?.let { strike.causingPlayer = it }
            }
        }
    }

    // Channeling on all melee damage-dealing tools
    @EventHandler
    fun onMeleeHit(event: EntityDamageByEntityEvent) {
        val player = event.damager as? Player ?: return
        val item = player.inventory.itemInMainHand
        if (!item.containsEnchantment(Enchantment.CHANNELING)) return

        if (item.isDamageDealingTool()) {
            val victim = event.entity
            val strike = player.world.spawn(victim.location, LightningStrike::class.java)
            strike.persistentDataContainer.set(TridentUtil.CHANNELING_LIGHTNING_KEY, PersistentDataType.BYTE, 1.toByte())
            strike.causingPlayer = player
        }
    }

    // Handles self-damage cancellation & forces lightning to bypass armor protection
    @Suppress("DEPRECATION")
    @EventHandler
    fun onLightningDamage(event: EntityDamageByEntityEvent) {
        val lightning = event.damager as? LightningStrike ?: return
        val entity = event.entity

        // Prevent player self-damage
        if (entity is Player && entity == lightning.causingPlayer) {
            event.isCancelled = true
            return
        }

        val isChanneling = lightning.persistentDataContainer.has(
            TridentUtil.CHANNELING_LIGHTNING_KEY,
            PersistentDataType.BYTE
        )

        if (isChanneling) {
            event.damage = 2.0
        } else {
            event.damage = 5.0
        }

        if (event.isApplicable(EntityDamageEvent.DamageModifier.ARMOR)) {
            event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0.0)
        }
    }

    // Protect items dropped by any mob when killed with Channeling
    @EventHandler
    fun onMobDeath(event: EntityDeathEvent) {
        val mob = event.entity as? Mob ?: return
        var killedByChanneling = false

        val damageEvent = mob.lastDamageCause as? EntityDamageByEntityEvent
        if (damageEvent != null) {
            val damager = damageEvent.damager
            if (damager is Trident) {
                if (damager.itemStack.containsEnchantment(Enchantment.CHANNELING)) {
                    killedByChanneling = true
                }
            } else if (damager is Player) {
                val hand = damager.inventory.itemInMainHand
                if (hand.containsEnchantment(Enchantment.CHANNELING) && hand.isDamageDealingTool()) {
                    killedByChanneling = true
                }
            } else if (damager is LightningStrike) {
                if (damager.causingPlayer != null) {
                    killedByChanneling = true
                }
            }
        }

        if (killedByChanneling) {
            // Record this death location and the exact world time to register instant protection
            recentChannelingDeaths[mob.location] = mob.world.fullTime
        }
    }

    // Instantly catch drops on spawn to protect them before the tick-loop can damage or ignite them
    @EventHandler(priority = EventPriority.LOWEST)
    fun onItemSpawn(event: ItemSpawnEvent) {
        // Fast-exit optimization: 99.9% of item spawns (mining, chest breaks) bypass this instantly
        if (recentChannelingDeaths.isEmpty()) return

        val itemEntity = event.entity
        val loc = itemEntity.location
        val world = itemEntity.world
        val currentTick = world.fullTime

        var isChannelingDrop = false
        val iterator = recentChannelingDeaths.entries.iterator()

        while (iterator.hasNext()) {
            val entry = iterator.next()
            val deathLoc = entry.key
            val deathTick = entry.value

            // Prune expired ticks from memory (older than 10 ticks / 0.5s)
            if (currentTick - deathTick > 10L) {
                iterator.remove()
                continue
            }

            // OPTIMIZATION: distanceSquared <= 4.0 (2.0^2) avoids expensive Math.sqrt calculations
            if (deathLoc.world == world && deathLoc.distanceSquared(loc) <= 4.0) {
                isChannelingDrop = true
            }
        }

        if (isChannelingDrop) {
            protectedItemsCache.put(itemEntity.uniqueId, true)

            // Extinguish the drop instantly to maintain clean, mergeable NBT tags
            itemEntity.fireTicks = 0
        }
    }

    // Cancel environment damage ONLY to the cached, protected mob drops
    @EventHandler
    fun onItemDamage(event: EntityDamageEvent) {
        val item = event.entity as? Item ?: return

        // If the item is in our cache, protect it and keep it extinguished
        if (protectedItemsCache.getIfPresent(item.uniqueId) != null) {
            event.isCancelled = true
            item.fireTicks = 0
        }
    }

    // Propagate protection to the resulting merged stack on the ground
    @EventHandler
    fun onItemMerge(event: ItemMergeEvent) {
        val target = event.target
        val entity = event.entity

        if (protectedItemsCache.getIfPresent(entity.uniqueId) != null ||
            protectedItemsCache.getIfPresent(target.uniqueId) != null) {
            protectedItemsCache.put(target.uniqueId, true)
        }

        // Clean up merged entity from memory
        protectedItemsCache.invalidate(entity.uniqueId)
    }

    // Clean up entity UUIDs from memory when picked up or despawned
    @EventHandler
    fun onItemPickup(event: EntityPickupItemEvent) {
        protectedItemsCache.invalidate(event.item.uniqueId)
    }

    @EventHandler
    fun onItemDespawn(event: ItemDespawnEvent) {
        protectedItemsCache.invalidate(event.entity.uniqueId)
    }
}