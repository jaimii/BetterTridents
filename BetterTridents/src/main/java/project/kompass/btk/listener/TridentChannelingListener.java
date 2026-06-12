package project.kompass.btk.listener;

import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

public class TridentChannelingListener implements Listener {

    @EventHandler
    public void onTridentHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Trident trident)) return;
        ItemStack item = trident.getItemStack();
        if (!item.containsEnchantment(Enchantment.CHANNELING)) return;

        Location loc = event.getHitEntity() != null ? event.getHitEntity().getLocation() :
                (event.getHitBlock() != null ? event.getHitBlock().getLocation() : trident.getLocation());

        LightningStrike strike = trident.getWorld().spawn(loc, LightningStrike.class);
        if (trident.getShooter() instanceof Player p) {
            strike.setCausingPlayer(p);
        }
    }
}