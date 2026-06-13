package project.kompass.btk.listener;

import project.kompass.btk.util.TridentUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;

public class TridentAttributeListener implements Listener {

    private static final double TRIDENT_REACH = 4.5;
    private static final double LUNGE_COOLDOWN_TICKS = 6.0; // 6 ticks per lunge for spears
    private static final double LUNGE_ATTACK_SPEED = 20.0 / LUNGE_COOLDOWN_TICKS; // 3.33 attacks per second

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        applyHeldAttributes(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        resetAttributes(event.getPlayer());
        applyHeldAttributes(event.getPlayer());
    }

    @EventHandler
    public void onItemHold(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());

        resetAttributes(player);
        if (TridentUtil.isTrident(newItem)) {
            applyTridentAttributes(player, newItem);
        } else if (TridentUtil.isSpear(newItem)) {
            applySpearAttributes(player, newItem);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (TridentUtil.isTrident(item) || TridentUtil.isSpear(item)) {
            resetAttributes(event.getPlayer());
        }
    }

    private void applyHeldAttributes(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (TridentUtil.isTrident(held)) {
            applyTridentAttributes(player, held);
        } else if (TridentUtil.isSpear(held)) {
            applySpearAttributes(player, held);
        }
    }

    private void applyTridentAttributes(Player player, ItemStack trident) {
        applyRangeModifier(player, Attribute.ENTITY_INTERACTION_RANGE, TRIDENT_REACH, TridentUtil.RANGE_KEY);
        applyRangeModifier(player, Attribute.BLOCK_INTERACTION_RANGE, TRIDENT_REACH, TridentUtil.BLOCK_RANGE_KEY);

        AttributeInstance speedInstance = player.getAttribute(Attribute.ATTACK_SPEED);
        if (speedInstance != null && speedInstance.getModifier(TridentUtil.SPEED_KEY) == null) {
            // Standard Better Trident speed: add 0.2 to vanilla 1.1, yielding 1.3
            speedInstance.addModifier(new AttributeModifier(TridentUtil.SPEED_KEY, 0.2,
                    AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
        }
    }

    private void applySpearAttributes(Player player, ItemStack spear) {
        // Change attack speed if enchanted with Lunge to align perfectly with the 6-tick duration
        if (spear.containsEnchantment(org.bukkit.enchantments.Enchantment.LUNGE)) {
            AttributeInstance speedInstance = player.getAttribute(Attribute.ATTACK_SPEED);
            if (speedInstance != null && speedInstance.getModifier(TridentUtil.SPEED_KEY) == null) {
                // 6 ticks per lunge (3.33 speed). Spears natively yield 1.2, so we add the difference.
                double modifierAmount = LUNGE_ATTACK_SPEED - 1.2;

                speedInstance.addModifier(new AttributeModifier(TridentUtil.SPEED_KEY, modifierAmount,
                        AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
            }
        }
    }

    private void applyRangeModifier(Player player, Attribute attribute, double targetValue, org.bukkit.NamespacedKey key) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(key);

        instance.addModifier(new AttributeModifier(key, targetValue - instance.getBaseValue(),
                AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
    }

    private void resetAttributes(Player player) {
        AttributeInstance rangeInstance = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
        if (rangeInstance != null && rangeInstance.getModifier(TridentUtil.RANGE_KEY) != null) {
            rangeInstance.removeModifier(TridentUtil.RANGE_KEY);
        }

        AttributeInstance blockRangeInstance = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
        if (blockRangeInstance != null && blockRangeInstance.getModifier(TridentUtil.BLOCK_RANGE_KEY) != null) {
            blockRangeInstance.removeModifier(TridentUtil.BLOCK_RANGE_KEY);
        }

        AttributeInstance speedInstance = player.getAttribute(Attribute.ATTACK_SPEED);
        if (speedInstance != null && speedInstance.getModifier(TridentUtil.SPEED_KEY) != null) {
            speedInstance.removeModifier(TridentUtil.SPEED_KEY);
        }
    }
}