package com.github.Syaaddd.swiftHarvest.manager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class DropManager {

    public static boolean consolidateDrops(Player player, List<ItemStack> drops) {
        PlayerInventory inventory = player.getInventory();
        List<ItemStack> remainingDrops = new ArrayList<>();

        for (ItemStack drop : drops) {
            if (drop == null || drop.getType().isAir()) continue;

            Map<Integer, ItemStack> remaining = inventory.addItem(drop);
            if (!remaining.isEmpty()) {
                remainingDrops.addAll(remaining.values());
            }
        }

        if (!remainingDrops.isEmpty()) {
            Location loc = player.getLocation();
            for (ItemStack item : remainingDrops) {
                if (item != null && !item.getType().isAir()) {
                    loc.getWorld().dropItemNaturally(loc, item);
                }
            }
            return false;
        }

        return true;
    }

    public static List<ItemStack> mergeDrops(List<ItemStack> drops) {
        Map<Material, ItemStack> merged = new HashMap<>();

        for (ItemStack drop : drops) {
            if (drop == null || drop.getType().isAir()) continue;

            Material type = drop.getType();
            if (merged.containsKey(type)) {
                ItemStack existing = merged.get(type);
                int total = existing.getAmount() + drop.getAmount();
                if (total <= existing.getMaxStackSize()) {
                    existing.setAmount(total);
                } else {
                    existing.setAmount(existing.getMaxStackSize());
                    int remaining = total - existing.getMaxStackSize();
                    ItemStack newStack = new ItemStack(type, remaining);
                    merged.put(type, newStack);
                }
            } else {
                merged.put(type, drop.clone());
            }
        }

        return new ArrayList<>(merged.values());
    }

    public static boolean hasSpace(Player player, ItemStack item) {
        PlayerInventory inventory = player.getInventory();
        Map<Integer, ItemStack> remaining = inventory.addItem(item);
        return remaining.isEmpty();
    }
}
