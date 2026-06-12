package project.kompass.btk.listener;

import project.kompass.btk.util.TridentUtil;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class TridentDamageListener implements Listener {

    private static final float CRITICAL_HIT_THRESHOLD = 0.1F;
    private static final double TRIDENT_BASE_DAMAGE = 10.0;

    @EventHandler(priority = EventPriority.NORMAL)
    public void onTridentDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        // Melee Damage
        if (event.getDamager() instanceof Player player && TridentUtil.isTrident(player.getInventory().getItemInMainHand())) {
            ItemStack item = player.getInventory().getItemInMainHand();
            double damage = TRIDENT_BASE_DAMAGE;

            if (item.containsEnchantment(Enchantment.SHARPNESS)) {
                damage += (0.5 * item.getEnchantmentLevel(Enchantment.SHARPNESS) + 0.5);
            }

            if (item.containsEnchantment(Enchantment.SMITE) && Tag.ENTITY_TYPES_SENSITIVE_TO_SMITE.isTagged(victim.getType())) {
                damage += (2.5 * item.getEnchantmentLevel(Enchantment.SMITE));
            }

            if (item.containsEnchantment(Enchantment.BANE_OF_ARTHROPODS) && Tag.ENTITY_TYPES_SENSITIVE_TO_BANE_OF_ARTHROPODS.isTagged(victim.getType())) {
                int level = item.getEnchantmentLevel(Enchantment.BANE_OF_ARTHROPODS);
                damage += (2.5 * level);
                int duration = 20 + new Random().nextInt(10 * level + 1);
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, 3));
            }

            if (item.containsEnchantment(Enchantment.IMPALING)) {
                if (TridentUtil.isVictimWet(victim) || Tag.ENTITY_TYPES_SENSITIVE_TO_IMPALING.isTagged(victim.getType())) {
                    damage += (2.5 * item.getEnchantmentLevel(Enchantment.IMPALING));
                }
            }

            if (player.getFallDistance() > CRITICAL_HIT_THRESHOLD && !player.isSwimming()) {
                damage *= 1.5;
            }
            event.setDamage(damage);

            // Thrown Damage
        } else if (event.getDamager() instanceof Trident trident) {
            ItemStack item = trident.getItemStack();
            double extraDamage = 0;

            if (item.containsEnchantment(Enchantment.SHARPNESS)) {
                extraDamage += (0.5 * item.getEnchantmentLevel(Enchantment.SHARPNESS) + 0.5);
            }

            if (item.containsEnchantment(Enchantment.SMITE) && Tag.ENTITY_TYPES_SENSITIVE_TO_SMITE.isTagged(victim.getType())) {
                extraDamage += (2.5 * item.getEnchantmentLevel(Enchantment.SMITE));
            }

            if (item.containsEnchantment(Enchantment.BANE_OF_ARTHROPODS) && Tag.ENTITY_TYPES_SENSITIVE_TO_BANE_OF_ARTHROPODS.isTagged(victim.getType())) {
                int level = item.getEnchantmentLevel(Enchantment.BANE_OF_ARTHROPODS);
                extraDamage += (2.5 * level);
                int duration = 20 + new Random().nextInt(10 * level + 1);
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, 3));
            }

            if (item.containsEnchantment(Enchantment.IMPALING)) {
                if (TridentUtil.isVictimWet(victim) || Tag.ENTITY_TYPES_SENSITIVE_TO_IMPALING.isTagged(victim.getType())) {
                    extraDamage += (2.5 * item.getEnchantmentLevel(Enchantment.IMPALING));
                }
            }

            event.setDamage(event.getDamage() + extraDamage);

            if (item.containsEnchantment(Enchantment.FIRE_ASPECT)) {
                event.getEntity().setFireTicks(item.getEnchantmentLevel(Enchantment.FIRE_ASPECT) * 80);
            }
        }
    }
}