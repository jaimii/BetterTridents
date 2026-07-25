package project.kompass.btk.listener

import project.kompass.btk.BTK
import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent
import io.papermc.paper.event.entity.EntityKnockbackEvent
import io.papermc.paper.event.entity.EntityPushedByEntityAttackEvent
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.entity.AbstractWindCharge
import org.bukkit.entity.BreezeWindCharge
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.WindCharge
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class WindChargeListener(private val plugin: BTK) : Listener {

    private fun isWindCharge(entity: Entity?): Boolean {
        if (entity == null) return false
        return entity is WindCharge ||
                entity is BreezeWindCharge ||
                entity is AbstractWindCharge ||
                entity.type.name.contains("WIND_CHARGE")
    }

    // 1. Intercepts Paper's native knockback events cleanly across subclasses
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityKnockback(event: EntityKnockbackEvent) {
        val player = event.entity as? Player ?: return

        var isFromWindCharge = false

        // Safely check specific subclasses for source entity references
        if (event is EntityKnockbackByEntityEvent && isWindCharge(event.hitBy)) {
            isFromWindCharge = true
        } else if (event is EntityPushedByEntityAttackEvent && isWindCharge(event.pushedBy)) {
            isFromWindCharge = true
        } else if (event.cause == EntityKnockbackEvent.Cause.EXPLOSION) {
            isFromWindCharge = true
        }

        if (isFromWindCharge) {
            // Override final knockback with the raw 0-armor unmitigated vector
            val rawKnockback = event.knockback
            event.setKnockback(rawKnockback)
        }
    }

    // 2. Fallback for players wearing 100% Netherite knockback resistance armor
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onWindChargeDamage(event: EntityDamageByEntityEvent) {
        val player = event.entity as? Player ?: return
        val damager = event.damager

        if (isWindCharge(damager)) {
            val kbRes = player.getAttribute(Attribute.KNOCKBACK_RESISTANCE)?.value ?: 0.0

            // If player's armor knockback resistance dampens the launch, enforce full velocity
            if (kbRes > 0.0) {
                val blastLoc = damager.location
                val playerLoc = player.location

                val direction = playerLoc.toVector().subtract(blastLoc.toVector())
                if (direction.lengthSquared() == 0.0) {
                    direction.y = 1.0 // Straight up if centered directly under the player
                } else {
                    direction.normalize()
                }

                // Full unmitigated 0-armor wind charge launch vector
                val forceVector = direction.multiply(1.1).setY(0.6)

                Bukkit.getScheduler().runTask(plugin, Runnable {
                    if (player.isOnline && !player.isDead) {
                        player.velocity = forceVector
                    }
                })
            }
        }
    }
}