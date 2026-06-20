package project.kompass.btk.listener

import project.kompass.btk.BTK
import project.kompass.btk.util.TridentUtil
import project.kompass.btk.util.isDamageDealingTool
import org.bukkit.Bukkit
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.persistence.PersistentDataType

class TridentChannelingListener(private val plugin: BTK) : Listener {

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

    @Suppress("DEPRECATION")
    @EventHandler
    fun onLightningDamage(event: EntityDamageByEntityEvent) {
        val lightning = event.damager as? LightningStrike ?: return
        val entity = event.entity

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
                if (hand.containsEnchantment(Enchantment.CHANNELING) && hand.isDamageDealingTool()) { // Updated to extension syntax
                    killedByChanneling = true
                }
            } else if (damager is LightningStrike) {
                if (damager.causingPlayer != null) {
                    killedByChanneling = true
                }
            }
        }

        if (killedByChanneling) {
            val drops = ArrayList(event.drops)
            val deathLoc = mob.location

            Bukkit.getScheduler().runTask(plugin, Runnable {
                for (entity in deathLoc.world.getNearbyEntities(deathLoc, 1.5, 1.5, 1.5) { it is Item }) {
                    val itemEntity = entity as Item
                    val itemStack = itemEntity.itemStack
                    for (drop in drops) {
                        if (drop.isSimilar(itemStack)) {
                            itemEntity.persistentDataContainer.set(
                                TridentUtil.CHANNELING_PROTECTED_KEY,
                                PersistentDataType.BYTE,
                                1.toByte()
                            )
                            break
                        }
                    }
                }
            })
        }
    }

    @EventHandler
    fun onItemDamage(event: EntityDamageEvent) {
        val item = event.entity as? Item ?: return
        if (item.persistentDataContainer.has(TridentUtil.CHANNELING_PROTECTED_KEY, PersistentDataType.BYTE)) {
            event.isCancelled = true
        }
    }
}