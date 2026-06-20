package project.kompass.btk.listener

import project.kompass.btk.BTK
import project.kompass.btk.util.isPotionOrSoup
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.inventory.*
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.java.JavaPlugin

class PotionSoupStackListener : Listener {

    private fun updateStackSize(item: ItemStack?) {
        if (item == null || item.type == Material.AIR) return
        val type = item.type

        if (type == Material.POTION || type == Material.SPLASH_POTION || type == Material.LINGERING_POTION) {
            val meta = item.itemMeta
            if (meta != null && (!meta.hasMaxStackSize() || meta.maxStackSize != 16)) {
                meta.setMaxStackSize(16)
                item.itemMeta = meta
            }
        }
        else if (type == Material.MUSHROOM_STEW || type == Material.RABBIT_STEW ||
            type == Material.BEETROOT_SOUP || type == Material.SUSPICIOUS_STEW) {
            val meta = item.itemMeta
            if (meta != null && (!meta.hasMaxStackSize() || meta.maxStackSize != 8)) {
                meta.setMaxStackSize(8)
                item.itemMeta = meta
            }
        }
    }

    private fun updateInventory(inventory: Inventory?) {
        if (inventory == null) return
        for (item in inventory.contents) {
            updateStackSize(item)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        updateInventory(event.player.inventory)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onInventoryOpen(event: InventoryOpenEvent) {
        updateInventory(event.inventory)
        updateInventory(event.player.inventory)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event is InventoryCreativeEvent) return

        val cursor = event.cursor
        val currentItem = event.currentItem

        if (!cursor.isPotionOrSoup() && !currentItem.isPotionOrSoup()) return

        updateStackSize(currentItem)
        updateStackSize(cursor)

        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(BTK::class.java), Runnable {
            event.clickedInventory?.let {
                updateStackSize(it.getItem(event.slot))
            }
            updateStackSize(event.cursor)
        })
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onInventoryCreative(event: InventoryCreativeEvent) {
        val cursor = event.cursor
        val currentItem = event.currentItem

        if (!cursor.isPotionOrSoup() && !currentItem.isPotionOrSoup()) return

        updateStackSize(currentItem)
        updateStackSize(cursor)

        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(BTK::class.java), Runnable {
            val player = event.whoClicked as? Player ?: return@Runnable
            if (player.isOnline) {
                updateStackSize(player.itemOnCursor)
                event.clickedInventory?.let {
                    updateStackSize(it.getItem(event.slot))
                }
            }
        })
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onInventoryDrag(event: InventoryDragEvent) {
        val oldCursor = event.oldCursor
        val cursor = event.cursor

        var involved = oldCursor.isPotionOrSoup() || cursor.isPotionOrSoup()
        if (!involved) {
            for (item in event.newItems.values) {
                if (item.isPotionOrSoup()) {
                    involved = true
                    break
                }
            }
        }
        if (!involved) return

        for (item in event.newItems.values) {
            updateStackSize(item)
        }
        updateStackSize(oldCursor)
        updateStackSize(cursor)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onItemSpawn(event: ItemSpawnEvent) {
        val itemEntity = event.entity
        val item = itemEntity.itemStack
        if (!item.isPotionOrSoup()) return

        updateStackSize(item)
        itemEntity.itemStack = item
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onItemPickup(event: EntityPickupItemEvent) {
        val item = event.item.itemStack
        if (!item.isPotionOrSoup()) return

        updateStackSize(item)
        event.item.itemStack = item
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onBrew(event: BrewEvent) {
        updateInventory(event.contents)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onCraft(event: CraftItemEvent) {
        val result = event.recipe.result
        if (!result.isPotionOrSoup()) return

        updateStackSize(event.currentItem)
        updateStackSize(event.cursor)
        updateStackSize(result)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!event.hasItem()) return
        val item = event.item
        if (!item.isPotionOrSoup()) return

        updateStackSize(item)
    }
}