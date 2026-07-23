package project.kompass.btk.listener

import project.kompass.btk.BTK
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.block.data.type.Campfire
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitRunnable

class MagicalCampfireListener(private val plugin: BTK) : Listener {

    private val miniMessage = MiniMessage.miniMessage()

    fun startCampfireTask() {
        object : BukkitRunnable() {
            override fun run() {
                val enabled = plugin.config.getBoolean("magical-campfire.enabled", true)
                if (!enabled) return

                val campfireEnabled = plugin.config.getBoolean("magical-campfire.campfire.enabled", true)
                val soulCampfireEnabled = plugin.config.getBoolean("magical-campfire.soul-campfire.enabled", true)

                if (!campfireEnabled && !soulCampfireEnabled) return

                for (player in plugin.server.onlinePlayers) {
                    if (player.isDead || player.gameMode == GameMode.SPECTATOR) continue

                    // Non-deprecated 1.21.11 replacement using the Attribute API
                    val maxHealth = player.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
                    if (player.health >= maxHealth) continue

                    if (campfireEnabled) {
                        handleCampfire(player, Material.CAMPFIRE, "campfire", maxHealth)
                    }
                    if (soulCampfireEnabled && player.health < maxHealth) {
                        handleCampfire(player, Material.SOUL_CAMPFIRE, "soul-campfire", maxHealth)
                    }
                }
            }
        }.runTaskTimer(plugin, 40L, 40L) // Executes every 2 seconds (40 ticks)
    }

    private fun handleCampfire(player: Player, targetMaterial: Material, configKey: String, maxHealth: Double) {
        val path = "magical-campfire.$configKey"
        val range = plugin.config.getInt("$path.range", 3).coerceIn(1, 10)
        val requireLit = plugin.config.getBoolean("$path.require-lit", true)
        val healAmount = plugin.config.getDouble("$path.amount", 1.0)
        val actionbarEnabled = plugin.config.getBoolean("$path.actionbar.enabled", false)
        val actionbarMessage = plugin.config.getString("$path.actionbar.message", "<green>You feel the warmth healing you.")

        val centerBlock = player.location.block
        var inRange = false

        // Fast relative block scanning
        scanLoop@ for (x in -range..range) {
            for (y in -range..range) {
                for (z in -range..range) {
                    val block = centerBlock.getRelative(x, y, z)
                    if (block.type == targetMaterial) {
                        if (!requireLit) {
                            inRange = true
                            break@scanLoop
                        }
                        val data = block.blockData as? Campfire
                        if (data != null && data.isLit) {
                            inRange = true
                            break@scanLoop
                        }
                    }
                }
            }
        }

        if (inRange) {
            val newHealth = minOf(player.health + healAmount, maxHealth)
            player.health = newHealth

            if (actionbarEnabled && !actionbarMessage.isNullOrEmpty()) {
                player.sendActionBar(miniMessage.deserialize(actionbarMessage))
            }
        }
    }
}