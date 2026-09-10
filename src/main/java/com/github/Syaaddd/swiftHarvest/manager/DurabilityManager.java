package com.github.Syaaddd.swiftHarvest.manager;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class DurabilityManager {

    /**
     * Check if tool has enough durability to break the given number of blocks.
     */
    public static boolean hasEnoughDurability(ItemStack tool, int blocksToBreak) {
        if (tool == null) return false;
        int maxDurability = tool.getType().getMaxDurability();
        if (maxDurability <= 0 || blocksToBreak < 0) return false;

        if (!(tool.getItemMeta() instanceof Damageable damageable)) return false;
        return maxDurability - damageable.getDamage() >= blocksToBreak;
    }

    /**
     * Check if tool has enough durability considering a multiplier (tier config).
     * Multiplier > 1.0 means tool takes MORE durability per block (harder tools = cheaper).
     * Multiplier < 1.0 means tool takes LESS durability per block (better tools = more efficient).
     */
    public static boolean hasEnoughDurability(ItemStack tool, int blocksToBreak, double durabilityMultiplier) {
        int adjusted = (int) Math.ceil(blocksToBreak * durabilityMultiplier);
        return hasEnoughDurability(tool, adjusted);
    }

    /**
     * Reduce durability by the number of blocks broken.
     */
    public static ItemStack reduceDurability(ItemStack tool, int amount) {
        if (tool == null) return null;

        if (amount <= 0 || !(tool.getItemMeta() instanceof Damageable damageable)) return tool;

        int newDamage = damageable.getDamage() + amount;
        if (newDamage >= tool.getType().getMaxDurability()) {
            tool.setAmount(0);
            return null;
        }

        damageable.setDamage(newDamage);
        tool.setItemMeta(damageable);
        return tool;
    }

    /**
     * Reduce durability considering a multiplier (tier config).
     */
    public static ItemStack reduceDurability(ItemStack tool, int amount, double durabilityMultiplier) {
        int adjusted = (int) Math.ceil(amount * durabilityMultiplier);
        return reduceDurability(tool, adjusted);
    }

    public static int calculateRequiredDurability(int blockCount) {
        return blockCount;
    }

    public static int calculateRequiredDurability(int blockCount, double multiplier) {
        return (int) Math.ceil(blockCount * multiplier);
    }
}
