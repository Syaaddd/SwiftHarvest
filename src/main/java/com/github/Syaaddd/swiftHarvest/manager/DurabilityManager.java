package com.github.Syaaddd.swiftHarvest.manager;

import org.bukkit.inventory.ItemStack;

public class DurabilityManager {

    public static boolean hasEnoughDurability(ItemStack tool, int blocksToBreak) {
        if (tool == null) return false;
        if (!tool.getType().isItem()) return false;
        
        short durability = tool.getDurability();
        short maxDurability = tool.getType().getMaxDurability();
        
        return (maxDurability - durability) >= blocksToBreak;
    }

    public static ItemStack reduceDurability(ItemStack tool, int amount) {
        if (tool == null) return null;
        
        short newDurability = (short) (tool.getDurability() + amount);
        tool.setDurability(newDurability);
        
        if (newDurability >= tool.getType().getMaxDurability()) {
            tool.setAmount(0);
            return null;
        }
        
        return tool;
    }

    public static int calculateRequiredDurability(int blockCount) {
        return blockCount;
    }
}
