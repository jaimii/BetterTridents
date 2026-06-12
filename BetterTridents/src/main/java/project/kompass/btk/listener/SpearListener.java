package project.kompass.btk.listener;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import project.kompass.btk.BTK;
import project.kompass.btk.util.TridentUtil;

public class SpearListener implements Listener {

    @EventHandler
    public void onPlayerSwing(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (TridentUtil.isSpear(item) && item.containsEnchantment(Enchantment.LUNGE)) {
            // Because the spear's attack speed attribute now aligns perfectly with the
            // lunge animation duration, we only need to clear the visual item cooldown overlay.
            if (player.hasCooldown(item.getType())) {
                Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(BTK.class), () -> {
                    if (player.isOnline()) {
                        player.setCooldown(item.getType(), 0);
                    }
                });
            }
        }
    }
}