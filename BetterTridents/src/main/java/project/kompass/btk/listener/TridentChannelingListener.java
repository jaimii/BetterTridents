package project.kompass.btk.listener;

import project.kompass.btk.BTK;
import project.kompass.btk.util.TridentUtil;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class TridentChannelingListener implements Listener {

    private final BTK plugin;

    public TridentChannelingListener(BTK plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTridentHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Trident trident)) return;
        ItemStack item = trident.getItemStack();

        if (item.containsEnchantment(Enchantment.CHANNELING)) {
            if (event.getHitEntity() != null) {
                Entity victim = event.getHitEntity();
                LightningStrike strike = trident.getWorld().spawn(victim.getLocation(), LightningStrike.class);

                strike.getPersistentDataContainer().set(TridentUtil.CHANNELING_LIGHTNING_KEY, PersistentDataType.BYTE, (byte) 1);

                if (trident.getShooter() instanceof Player p) {
                    strike.setCausingPlayer(p);
                }
            } else if (event.getHitBlock() != null) {
                Location loc = event.getHitBlock().getLocation();
                LightningStrike strike = trident.getWorld().spawn(loc, LightningStrike.class);

                strike.getPersistentDataContainer().set(TridentUtil.CHANNELING_LIGHTNING_KEY, PersistentDataType.BYTE, (byte) 1);

                if (trident.getShooter() instanceof Player p) {
                    strike.setCausingPlayer(p);
                }
            }
        }
    }

    @EventHandler
    public void onMeleeHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.containsEnchantment(Enchantment.CHANNELING)) return;

        if (TridentUtil.isDamageDealingTool(item)) {
            Entity victim = event.getEntity();
            LightningStrike strike = player.getWorld().spawn(victim.getLocation(), LightningStrike.class);

            strike.getPersistentDataContainer().set(TridentUtil.CHANNELING_LIGHTNING_KEY, PersistentDataType.BYTE, (byte) 1);

            strike.setCausingPlayer(player);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onLightningDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof LightningStrike lightning) {
            if (event.getEntity() instanceof Player player) {
                if (player.equals(lightning.getCausingPlayer())) {
                    event.setCancelled(true);
                    return;
                }
            }

            boolean isChanneling = lightning.getPersistentDataContainer().has(
                    TridentUtil.CHANNELING_LIGHTNING_KEY,
                    PersistentDataType.BYTE
            );

            if (isChanneling) {
                event.setDamage(2.0);
            } else {
                event.setDamage(5.0);
            }

            if (event.isApplicable(EntityDamageEvent.DamageModifier.ARMOR)) {
                event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0.0);
            }
        }
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;

        boolean killedByChanneling = false;

        if (mob.getLastDamageCause() instanceof EntityDamageByEntityEvent damageEvent) {
            if (damageEvent.getDamager() instanceof Trident trident) {
                if (trident.getItemStack().containsEnchantment(Enchantment.CHANNELING)) {
                    killedByChanneling = true;
                }
            } else if (damageEvent.getDamager() instanceof Player player) {
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand != null && hand.containsEnchantment(Enchantment.CHANNELING) && TridentUtil.isDamageDealingTool(hand)) {
                    killedByChanneling = true;
                }
            } else if (damageEvent.getDamager() instanceof LightningStrike strike) {
                if (strike.getCausingPlayer() != null) {
                    killedByChanneling = true;
                }
            }
        }

        if (killedByChanneling) {
            final java.util.List<ItemStack> drops = new java.util.ArrayList<>(event.getDrops());
            final Location deathLoc = mob.getLocation();

            org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                // OPTIMIZATION: Uses Spigot's predicate filter to return ONLY Item drop entities, bypassing nearby mobs/players
                for (Entity entity : deathLoc.getWorld().getNearbyEntities(deathLoc, 1.5, 1.5, 1.5, entity -> entity instanceof Item)) {
                    Item itemEntity = (Item) entity;
                    ItemStack itemStack = itemEntity.getItemStack();
                    for (ItemStack drop : drops) {
                        if (drop.isSimilar(itemStack)) {
                            itemEntity.getPersistentDataContainer().set(
                                    TridentUtil.CHANNELING_PROTECTED_KEY,
                                    PersistentDataType.BYTE,
                                    (byte) 1
                            );
                            break;
                        }
                    }
                }
            });
        }
    }

    @EventHandler
    public void onItemDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Item item) {
            if (item.getPersistentDataContainer().has(TridentUtil.CHANNELING_PROTECTED_KEY, PersistentDataType.BYTE)) {
                event.setCancelled(true);
            }
        }
    }
}