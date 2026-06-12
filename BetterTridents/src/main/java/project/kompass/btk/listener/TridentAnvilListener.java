package project.kompass.btk.listener;

import project.kompass.btk.util.TridentUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.Set;

public class TridentAnvilListener implements Listener {

    private static final int ANVIL_REPAIR_COST = 5;

    private static final Set<Enchantment> ALLOWED_ENCHANTMENTS = Set.of(
            Enchantment.SHARPNESS,
            Enchantment.SMITE,
            Enchantment.BANE_OF_ARTHROPODS,
            Enchantment.LOOTING,
            Enchantment.FIRE_ASPECT
    );

    @EventHandler
    public void onAnvilUse(PrepareAnvilEvent event) {
        ItemStack first = event.getInventory().getItem(0);
        ItemStack second = event.getInventory().getItem(1);
        if (first == null || second == null) return;

        Material type = first.getType();
        boolean isTrident = (type == Material.TRIDENT);
        boolean isTool = TridentUtil.isDamageDealingTool(first);

        if (!isTrident && !isTool) return;

        ItemStack result = first.clone();
        Map<Enchantment, Integer> incoming = second.getType() == Material.ENCHANTED_BOOK
                ? ((EnchantmentStorageMeta) second.getItemMeta()).getStoredEnchants()
                : second.getEnchantments();

        int added = 0;

        for (var entry : incoming.entrySet()) {
            Enchantment enchant = entry.getKey();
            int incomingLvl = entry.getValue();

            boolean canApply = false;

            if (isTrident) {
                // Allows only standard enchantments (Piercing removed)
                if (ALLOWED_ENCHANTMENTS.contains(enchant)) {
                    canApply = true;
                }
            } else {
                if (enchant.equals(Enchantment.CHANNELING)) {
                    canApply = true;
                }
            }

            if (canApply) {
                int cur = result.getEnchantmentLevel(enchant);
                int next = (cur == incomingLvl) ? cur + 1 : Math.max(cur, incomingLvl);

                int maxLevel = enchant.getMaxLevel();
                next = Math.min(next, maxLevel);

                if (next > cur) {
                    result.addUnsafeEnchantment(enchant, next);
                    added++;
                }
            }
        }

        if (added > 0) {
            ItemMeta meta = result.getItemMeta();
            String renameText = event.getView().getRenameText();

            if (renameText != null && !renameText.isEmpty()) {
                meta.displayName(Component.text(renameText));
            }

            result.setItemMeta(meta);
            event.setResult(result);
            event.getView().setRepairCost(Math.min(added, ANVIL_REPAIR_COST));
        }
    }
}