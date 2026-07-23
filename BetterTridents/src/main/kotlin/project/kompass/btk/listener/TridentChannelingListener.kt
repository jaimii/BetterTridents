package project.kompass.btk.listener

import project.kompass.btk.BTK
import project.kompass.btk.util.TridentUtil
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.persistence.PersistentDataType

class TridentChannelingListener(private val plugin: BTK) : Listener {

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

    // Channeling on ANY item during melee attacks
    @EventHandler
    fun onMeleeHit(event: EntityDamageByEntityEvent) {
        // Prevent Channeling from triggering on Thorns armor damage reflection
        if (event.cause == EntityDamageEvent.DamageCause.THORNS) return

        val player = event.damager as? Player ?: return
        val item = player.inventory.itemInMainHand

        // Any item carrying Channeling will trigger lightning on melee hits
        if (item.containsEnchantment(Enchantment.CHANNELING)) {
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

        // 1. Cancel all lightning strike damage to item entities
        if (entity is Item) {
            event.isCancelled = true
            return
        }

        // 2. Prevent player self-damage
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

    // Prevents custom channeling lightning from igniting surrounding blocks (creating fire)
    @EventHandler
    fun onBlockIgnite(event: BlockIgniteEvent) {
        if (event.cause == BlockIgniteEvent.IgniteCause.LIGHTNING) {
            val igniter = event.ignitingEntity as? LightningStrike ?: return

            if (igniter.persistentDataContainer.has(TridentUtil.CHANNELING_LIGHTNING_KEY, PersistentDataType.BYTE)) {
                event.isCancelled = true
            }
        }
    }
}