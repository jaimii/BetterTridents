package project.kompass.btk.listener

import org.bukkit.Bukkit
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAnimationEvent
import org.bukkit.event.player.PlayerAnimationType
import org.bukkit.plugin.java.JavaPlugin
import project.kompass.btk.BTK
import project.kompass.btk.util.isSpear

class SpearListener : Listener {

    @EventHandler
    fun onPlayerSwing(event: PlayerAnimationEvent) {
        if (event.animationType != PlayerAnimationType.ARM_SWING) return
        val player = event.player
        val item = player.inventory.itemInMainHand

        if (item.isSpear() && item.containsEnchantment(Enchantment.LUNGE)) {
            if (player.hasCooldown(item.type)) {
                Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(BTK::class.java), Runnable {
                    if (player.isOnline) {
                        player.setCooldown(item.type, 0)
                    }
                })
            }
        }
    }
}