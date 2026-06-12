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

    private static final double TRIDENT_ATTACK_SPEED = 4.2;
    private static final double DEFAULT_ATTACK_SPEED = 4.0;
    private static final double TRIDENT_REACH = 5.2;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        applyTridentAttributesIfHeld(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        resetAttributes(event.getPlayer());
        applyTridentAttributesIfHeld(event.getPlayer());
    }

    @EventHandler
    public void onItemHold(PlayerItemHeldEvent event) {
        resetAttributes(event.getPlayer());
        ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
        if (TridentUtil.isTrident(item)) {
            applyTridentAttributes(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (TridentUtil.isTrident(event.getItemDrop().getItemStack())) {
            resetAttributes(event.getPlayer());
        }
    }

    private void applyTridentAttributesIfHeld(Player player) {
        if (TridentUtil.isTrident(player.getInventory().getItemInMainHand())) {
            applyTridentAttributes(player);
        }
    }

    private void applyTridentAttributes(Player player) {
        AttributeInstance speed = player.getAttribute(Attribute.ATTACK_SPEED);
        if (speed != null) {
            speed.setBaseValue(TRIDENT_ATTACK_SPEED);
        }

        applyRangeModifier(player, Attribute.ENTITY_INTERACTION_RANGE, TRIDENT_REACH, TridentUtil.RANGE_KEY);
        applyRangeModifier(player, Attribute.BLOCK_INTERACTION_RANGE, TRIDENT_REACH, TridentUtil.BLOCK_RANGE_KEY);
    }

    private void applyRangeModifier(Player player, Attribute attribute, double targetValue, org.bukkit.NamespacedKey key) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(key);

        instance.addModifier(new AttributeModifier(key, targetValue - instance.getBaseValue(),
                AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
    }

    private void resetAttributes(Player player) {
        AttributeInstance speed = player.getAttribute(Attribute.ATTACK_SPEED);
        if (speed != null) {
            speed.setBaseValue(DEFAULT_ATTACK_SPEED);
        }

        AttributeInstance entityRange = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
        if (entityRange != null) {
            entityRange.removeModifier(TridentUtil.RANGE_KEY);
        }

        AttributeInstance blockRange = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
        if (blockRange != null) {
            blockRange.removeModifier(TridentUtil.BLOCK_RANGE_KEY);
        }
    }
}