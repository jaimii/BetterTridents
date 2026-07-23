package project.kompass.btk.listener

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import project.kompass.btk.BTK
import project.kompass.btk.hook.ConsumeFood2Hook
import project.kompass.btk.util.isFood

class AlwaysEatListener(private val plugin: BTK) : Listener {

    private val isAlwaysEatEnabled: Boolean = plugin.config.getBoolean("always-eat-enabled", true)

    init {
        if (Bukkit.getPluginManager().isPluginEnabled("ConsumeFood2")) {
            ConsumeFood2Hook.hookAlwaysEat(plugin)
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
        if (!isAlwaysEatEnabled) return

        val item = event.item ?: return
        val player = event.player

        if (player.foodLevel >= 20 && item.isFood()) {
            val meta = item.itemMeta ?: return
            val food = meta.food
            if (!food.canAlwaysEat()) {
                food.setCanAlwaysEat(true)
                meta.setFood(food)
                item.itemMeta = meta
            }
        }
    }
}