package project.kompass.btk.listener;

import project.kompass.btk.BTK;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class PotionSoupStackListener implements Listener {

    // Main optimization: checking materials first bypasses expensive ItemMeta allocations for 95%+ of items
    private void updateStackSize(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        Material type = item.getType();

        // 1. Potions: Custom stack size 16
        if (type == Material.POTION || type == Material.SPLASH_POTION || type == Material.LINGERING_POTION) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && (!meta.hasMaxStackSize() || meta.getMaxStackSize() != 16)) {
                meta.setMaxStackSize(16);
                item.setItemMeta(meta);
            }
        }
        // 2. Soups and Stews: Custom stack size 8
        else if (type == Material.MUSHROOM_STEW || type == Material.RABBIT_STEW ||
                type == Material.BEETROOT_SOUP || type == Material.SUSPICIOUS_STEW) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && (!meta.hasMaxStackSize() || meta.getMaxStackSize() != 8)) {
                meta.setMaxStackSize(8);
                item.setItemMeta(meta);
            }
        }
    }

    private void updateInventory(Inventory inventory) {
        if (inventory == null) return;
        for (ItemStack item : inventory.getContents()) {
            updateStackSize(item);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateInventory(event.getPlayer().getInventory());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        updateInventory(event.getInventory());
        updateInventory(event.getPlayer().getInventory());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        // Skip creative clicks here; they are handled separately by onInventoryCreative
        if (event instanceof InventoryCreativeEvent) return;

        updateStackSize(event.getCurrentItem());
        updateStackSize(event.getCursor());

        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(BTK.class), () -> {
            if (event.getClickedInventory() != null) {
                updateStackSize(event.getClickedInventory().getItem(event.getSlot()));
            }
            updateStackSize(event.getCursor());
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryCreative(InventoryCreativeEvent event) {
        updateStackSize(event.getCurrentItem());
        updateStackSize(event.getCursor());

        // Next-tick execution forces the server to send an update packet (SetSlot) back to
        // the client, overriding the client's local creative inventory assumptions.
        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(BTK.class), () -> {
            updateStackSize(event.getWhoClicked().getItemOnCursor());
            if (event.getClickedInventory() != null) {
                updateStackSize(event.getClickedInventory().getItem(event.getSlot()));
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        for (ItemStack item : event.getNewItems().values()) {
            updateStackSize(item);
        }
        updateStackSize(event.getOldCursor());
        updateStackSize(event.getCursor());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item itemEntity = event.getEntity();
        ItemStack item = itemEntity.getItemStack();
        updateStackSize(item);
        itemEntity.setItemStack(item);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemPickup(EntityPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        updateStackSize(item);
        event.getItem().setItemStack(item);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBrew(BrewEvent event) {
        updateInventory(event.getContents());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCraft(CraftItemEvent event) {
        updateStackSize(event.getCurrentItem());
        updateStackSize(event.getCursor());
        updateStackSize(event.getRecipe().getResult());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        updateStackSize(event.getItem());
    }
}