package project.kompass.btk.listener;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;

import java.util.Random;

public class TridentLootingListener implements Listener {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent damageEvent) ||
                !(damageEvent.getDamager() instanceof Trident trident)) {
            return;
        }

        int level = trident.getItemStack().getEnchantmentLevel(Enchantment.LOOTING);
        if (level <= 0 || !(trident.getShooter() instanceof Player shooter)) return;

        LootTable lootTable = ((Mob) event.getEntity()).getLootTable();
        if (lootTable != null) {
            event.getDrops().clear();
            event.getDrops().addAll(lootTable.populateLoot(
                    new Random(),
                    new LootContext.Builder(event.getEntity().getLocation())
                            .lootedEntity(event.getEntity())
                            .killer(shooter)
                            .luck(level)
                            .build()
            ));
        }
    }
}