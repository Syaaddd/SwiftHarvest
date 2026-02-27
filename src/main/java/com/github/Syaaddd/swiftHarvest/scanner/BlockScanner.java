package com.github.Syaaddd.swiftHarvest.scanner;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.*;

public class BlockScanner {

    private static final BlockFace[] DIRECTIONS = {
        BlockFace.UP, BlockFace.DOWN,
        BlockFace.NORTH, BlockFace.SOUTH,
        BlockFace.EAST, BlockFace.WEST
    };

    public static List<Block> findConnectedBlocks(Block startBlock, Material targetMaterial, int maxBlocks) {
        List<Block> connected = new ArrayList<>();
        Queue<Block> queue = new LinkedList<>();
        Set<Block> visited = new HashSet<>();

        queue.add(startBlock);
        visited.add(startBlock);

        while (!queue.isEmpty() && connected.size() < maxBlocks) {
            Block current = queue.poll();

            if (current.getType() == targetMaterial) {
                connected.add(current);
            }

            for (BlockFace face : DIRECTIONS) {
                Block neighbor = current.getRelative(face);
                if (!visited.contains(neighbor) && neighbor.getType() == targetMaterial) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return connected;
    }

    public static List<Block> findConnectedTimber(Block startBlock, Set<Material> logTypes, Set<Material> leafTypes, int maxBlocks, boolean breakLeaves) {
        List<Block> connected = new ArrayList<>();
        Queue<Block> queue = new LinkedList<>();
        Set<Block> visited = new HashSet<>();

        Material startType = startBlock.getType();
        if (!logTypes.contains(startType) && !leafTypes.contains(startType)) {
            return connected;
        }

        queue.add(startBlock);
        visited.add(startBlock);

        while (!queue.isEmpty() && connected.size() < maxBlocks) {
            Block current = queue.poll();
            Material currentType = current.getType();

            if (logTypes.contains(currentType)) {
                connected.add(current);
            } else if (breakLeaves && leafTypes.contains(currentType)) {
                connected.add(current);
            }

            for (BlockFace face : DIRECTIONS) {
                Block neighbor = current.getRelative(face);
                if (visited.contains(neighbor)) continue;

                Material neighborType = neighbor.getType();
                if (logTypes.contains(neighborType)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                } else if (breakLeaves && leafTypes.contains(neighborType)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return connected;
    }

    public static List<Block> findVeinPreview(Block startBlock, Material targetMaterial, int maxBlocks) {
        return findConnectedBlocks(startBlock, targetMaterial, maxBlocks);
    }

    public static List<Block> findTimberPreview(Block startBlock, Set<Material> logTypes, Set<Material> leafTypes, int maxBlocks, boolean breakLeaves) {
        return findConnectedTimber(startBlock, logTypes, leafTypes, maxBlocks, breakLeaves);
    }
}
