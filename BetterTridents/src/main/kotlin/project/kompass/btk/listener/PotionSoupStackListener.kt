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
import org.bukkit.plugin.java.JavaPlugin

class PotionSoupStackListener : Listener {

    private fun updateStackSize(item: ItemStack?) {
        if (item == null || item.type == Material.AIR) return
        val type = item.type

        // 1. Potions: Custom stack size 16
        if (type == Material.POTION || type == Material.SPLASH_POTION || type == Material.LINGERING_POTION) {
            val meta = item.itemMeta
            if (meta != null && (!meta.hasMaxStackSize() || meta.maxStackSize != 16)) {
                meta.setMaxStackSize(16)
                item.itemMeta = meta
            }
        }
        // 2. Soups and Stews: Custom stack size 8
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
            if (item.isPotionOrSoup()) {
                updateStackSize(item)
            }
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

        event.currentItem = currentItem
        event.whoClicked.setItemOnCursor(cursor)

        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(BTK::class.java), Runnable {
            event.clickedInventory?.let {
                val slotItem = it.getItem(event.slot)
                if (slotItem.isPotionOrSoup()) {
                    updateStackSize(slotItem)
                    it.setItem(event.slot, slotItem)
                }
            }
            val player = event.whoClicked as? Player
            if (player != null && player.isOnline) {
                val cursorItem = player.itemOnCursor
                if (cursorItem.isPotionOrSoup()) {
                    updateStackSize(cursorItem)
                    player.setItemOnCursor(cursorItem)
                }
            }
        })
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onInventoryCreative(event: InventoryCreativeEvent) {
        val cursor = event.cursor
        val currentItem = event.currentItem

        if (!cursor.isPotionOrSoup() && !currentItem.isPotionOrSoup()) return

        val oldCursor = event.whoClicked.itemOnCursor
        if (oldCursor != null && oldCursor.type != Material.AIR &&
            cursor != null && cursor.type != Material.AIR &&
            oldCursor.isSimilar(cursor)) {

            val type = oldCursor.type
            val isPotion = (type == Material.POTION || type == Material.SPLASH_POTION || type == Material.LINGERING_POTION)
            val isSoup = (type == Material.MUSHROOM_STEW || type == Material.RABBIT_STEW ||
                    type == Material.BEETROOT_SOUP || type == Material.SUSPICIOUS_STEW)

            if (isPotion || isSoup) {
                val max = if (isPotion) 16 else 8
                val newAmount = Math.min(oldCursor.amount + cursor.amount, max)

                val stacked = oldCursor.clone()
                stacked.amount = newAmount
                updateStackSize(stacked)

                event.whoClicked.setItemOnCursor(stacked)
                event.setResult(org.bukkit.event.Event.Result.ALLOW)
            }
        } else {
            updateStackSize(currentItem)
            updateStackSize(cursor)

            event.currentItem = currentItem
            event.whoClicked.setItemOnCursor(cursor)
        }

        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(BTK::class.java), Runnable {
            val player = event.whoClicked as? Player ?: return@Runnable
            if (player.isOnline) {
                val cursorItem = player.itemOnCursor
                if (cursorItem.isPotionOrSoup()) {
                    updateStackSize(cursorItem)
                    player.setItemOnCursor(cursorItem)
                }

                event.clickedInventory?.let {
                    val slotItem = it.getItem(event.slot)
                    if (slotItem.isPotionOrSoup()) {
                        updateStackSize(slotItem)
                        it.setItem(event.slot, slotItem)
                    }
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

        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(BTK::class.java), Runnable {

            for (slot in event.newItems.keys) {
                val slotItem = event.view.getItem(slot)
                if (slotItem.isPotionOrSoup()) {
                    updateStackSize(slotItem)
                    event.view.setItem(slot, slotItem)
                }
            }
            val player = event.whoClicked as? Player
            if (player != null && player.isOnline) {
                val cursorItem = player.itemOnCursor
                if (cursorItem.isPotionOrSoup()) {
                    updateStackSize(cursorItem)
                    player.setItemOnCursor(cursorItem)
                }
            }
        })
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
        val itemEntity = event.item
        val item = itemEntity.itemStack
        if (!item.isPotionOrSoup()) return

        updateStackSize(item)
        itemEntity.itemStack = item
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onBrew(event: BrewEvent) {
        updateInventory(event.contents)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onCraft(event: CraftItemEvent) {
        val result = event.recipe.result
        if (!result.isPotionOrSoup()) return

        val currentItem = event.currentItem
        val cursor = event.cursor

        updateStackSize(currentItem)
        updateStackSize(cursor)
        updateStackSize(result)

        event.currentItem = currentItem
        event.whoClicked.setItemOnCursor(cursor)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!event.hasItem()) return
        val item = event.item
        if (!item.isPotionOrSoup()) return

        updateStackSize(item)
        event.player.inventory.setItem(event.hand ?: org.bukkit.inventory.EquipmentSlot.HAND, item)
    }
}