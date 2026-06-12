package project.kompass.btk.listener;

import project.kompass.btk.util.TridentUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class TridentRiptideListener implements Listener {

    private static final double TRIDENT_BASE_DAMAGE = 10.0;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        if (!TridentUtil.isTrident(item) || !item.containsEnchantment(Enchantment.RIPTIDE)) return;

        Player player = event.getPlayer();
        boolean isWet = TridentUtil.isPlayerWet(player);

        // works in dry weather and snowfall with a 5s cooldown.
        // Wet players run standard vanilla Riptide with no cooldown.
        if (!isWet && !player.hasCooldown(Material.TRIDENT)) {
            event.setCancelled(true);
            int level = item.getEnchantmentLevel(Enchantment.RIPTIDE);
            player.setVelocity(player.getLocation().getDirection().normalize().multiply(1.8 + (level * 0.5)));

            player.startRiptideAttack(20, (float) TRIDENT_BASE_DAMAGE, item);

            player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, 1.0f, 1.0f);
            player.setCooldown(Material.TRIDENT, 100); // 100 ticks = 5 seconds
        }
    }
}