package com.github.Syaaddd.swiftHarvest.scanner;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.*;

public class BlockScanner {

    private static final BlockFace[] DIRECTIONS_6 = {
        BlockFace.UP, BlockFace.DOWN,
        BlockFace.NORTH, BlockFace.SOUTH,
        BlockFace.EAST, BlockFace.WEST
    };

    // 10 directions: 6 cardinal + 4 horizontal diagonals
    // Note: BlockFace doesn't have UP_NORTH etc, so we only use what's available
    private static final BlockFace[] DIRECTIONS_10 = {
        // 6 cardinal
        BlockFace.UP, BlockFace.DOWN,
        BlockFace.NORTH, BlockFace.SOUTH,
        BlockFace.EAST, BlockFace.WEST,
        // 4 horizontal diagonals
        BlockFace.NORTH_EAST, BlockFace.NORTH_WEST,
        BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST
    };

    public static List<Block> findConnectedBlocks(Block startBlock, Material targetMaterial, int maxBlocks, boolean diagonal) {
        List<Block> connected = new ArrayList<>();
        Queue<Block> queue = new ArrayDeque<>(maxBlocks);
        Set<Long> visited = new HashSet<>(maxBlocks * 2);

        BlockFace[] directions = diagonal ? DIRECTIONS_10 : DIRECTIONS_6;

        queue.add(startBlock);
        visited.add(encodeBlock(startBlock));

        while (!queue.isEmpty() && connected.size() < maxBlocks) {
            Block current = queue.poll();
            Material currentType = current.getType();

            if (currentType == targetMaterial) {
                connected.add(current);
            }

            for (BlockFace face : directions) {
                Block neighbor = current.getRelative(face);
                long neighborKey = encodeBlock(neighbor);
                
                if (!visited.contains(neighborKey)) {
                    visited.add(neighborKey);
                    if (neighbor.getType() == targetMaterial) {
                        queue.add(neighbor);
                    }
                }
            }
        }
        return connected;
    }

    public static List<Block> findConnectedBlocks(Block startBlock, Material targetMaterial, int maxBlocks) {
        return findConnectedBlocks(startBlock, targetMaterial, maxBlocks, false);
    }

    /**
     * Strict timber detection: trunk-based algorithm that prevents bleeding to adjacent trees.
     * Algorithm:
     * 1. Find the full trunk by scanning UP from start block
     * 2. Expand horizontally only if connected to trunk logs (max radius from trunk)
     * 3. Collect leaves within radius of trunk (if breakLeaves enabled)
     */
    public static List<Block> findConnectedTimberStrict(Block startBlock, Set<Material> logTypes, 
            Set<Material> leafTypes, int maxBlocks, boolean breakLeaves, int maxHorizontalRadius) {
        
        List<Block> result = new ArrayList<>();
        Set<Long> visited = new HashSet<>(maxBlocks * 2);
        
        Material startType = startBlock.getType();
        if (!logTypes.contains(startType)) {
            return result;
        }

        // Step 1: Find trunk by scanning UP and DOWN
        List<Block> trunk = new ArrayList<>();
        Queue<Block> trunkQueue = new ArrayDeque<>();
        trunkQueue.add(startBlock);
        visited.add(encodeBlock(startBlock));

        // BFS only UP/DOWN and horizontal if part of same trunk
        while (!trunkQueue.isEmpty() && trunk.size() < maxBlocks) {
            Block current = trunkQueue.poll();
            if (logTypes.contains(current.getType())) {
                trunk.add(current);
            }

            // Only scan UP/DOWN for trunk
            for (BlockFace face : new BlockFace[]{BlockFace.UP, BlockFace.DOWN}) {
                Block neighbor = current.getRelative(face);
                long neighborKey = encodeBlock(neighbor);
                
                if (!visited.contains(neighborKey) && logTypes.contains(neighbor.getType())) {
                    visited.add(neighborKey);
                    trunkQueue.add(neighbor);
                }
            }
        }

        // Step 2: Expand horizontally from trunk, but only within radius
        // Find trunk center X/Z
        int centerX = startBlock.getX();
        int centerZ = startBlock.getZ();

        // BFS from trunk with horizontal expansion limited by radius
        Queue<Block> expandQueue = new ArrayDeque<>(trunk);
        Set<Long> expandVisited = new HashSet<>(visited);

        while (!expandQueue.isEmpty() && result.size() < maxBlocks) {
            Block current = expandQueue.poll();
            
            // Only add if within horizontal radius of trunk center
            int dx = Math.abs(current.getX() - centerX);
            int dz = Math.abs(current.getZ() - centerZ);
            
            if (dx <= maxHorizontalRadius && dz <= maxHorizontalRadius) {
                if (logTypes.contains(current.getType())) {
                    if (!result.contains(current)) {
                        result.add(current);
                    }
                }
            }

            // Check all 6 directions
            for (BlockFace face : DIRECTIONS_6) {
                Block neighbor = current.getRelative(face);
                long neighborKey = encodeBlock(neighbor);
                
                if (!expandVisited.contains(neighborKey)) {
                    Material neighborType = neighbor.getType();
                    
                    if (logTypes.contains(neighborType)) {
                        // Check if this neighbor is still within radius
                        int ndx = Math.abs(neighbor.getX() - centerX);
                        int ndz = Math.abs(neighbor.getZ() - centerZ);
                        
                        if (ndx <= maxHorizontalRadius && ndz <= maxHorizontalRadius) {
                            expandVisited.add(neighborKey);
                            expandQueue.add(neighbor);
                        }
                    }
                }
            }
        }

        // Step 3: Collect leaves within radius of trunk logs (if breakLeaves enabled)
        if (breakLeaves && result.size() < maxBlocks) {
            Set<Block> leaves = new HashSet<>();
            
            for (Block log : result) {
                // Scan 3x3x3 area around each log for leaves
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            Block neighbor = log.getRelative(x, y, z);
                            if (leafTypes.contains(neighbor.getType())) {
                                leaves.add(neighbor);
                            }
                        }
                    }
                }
            }

            // Add leaves to result (up to maxBlocks)
            for (Block leaf : leaves) {
                if (result.size() >= maxBlocks) break;
                result.add(leaf);
            }
        }

        return result;
    }

    /**
     * Original loose BFS timber detection (kept for backwards compatibility)
     */
    public static List<Block> findConnectedTimberLoose(Block startBlock, Set<Material> logTypes, 
            Set<Material> leafTypes, int maxBlocks, boolean breakLeaves) {
        
        List<Block> connected = new ArrayList<>();
        Queue<Block> queue = new ArrayDeque<>(maxBlocks);
        Set<Long> visited = new HashSet<>(maxBlocks * 2);

        Material startType = startBlock.getType();
        if (!logTypes.contains(startType) && !leafTypes.contains(startType)) {
            return connected;
        }

        queue.add(startBlock);
        visited.add(encodeBlock(startBlock));

        while (!queue.isEmpty() && connected.size() < maxBlocks) {
            Block current = queue.poll();
            Material currentType = current.getType();

            if (logTypes.contains(currentType)) {
                connected.add(current);
            } else if (breakLeaves && leafTypes.contains(currentType)) {
                connected.add(current);
            }

            for (BlockFace face : DIRECTIONS_6) {
                Block neighbor = current.getRelative(face);
                long neighborKey = encodeBlock(neighbor);
                
                if (visited.contains(neighborKey)) continue;

                Material neighborType = neighbor.getType();
                if (logTypes.contains(neighborType)) {
                    visited.add(neighborKey);
                    queue.add(neighbor);
                } else if (breakLeaves && leafTypes.contains(neighborType)) {
                    visited.add(neighborKey);
                    queue.add(neighbor);
                }
            }
        }
        return connected;
    }

    public static List<Block> findConnectedTimber(Block startBlock, Set<Material> logTypes, 
            Set<Material> leafTypes, int maxBlocks, boolean breakLeaves, boolean strict, int maxHorizontalRadius) {
        
        if (strict) {
            return findConnectedTimberStrict(startBlock, logTypes, leafTypes, maxBlocks, breakLeaves, maxHorizontalRadius);
        } else {
            return findConnectedTimberLoose(startBlock, logTypes, leafTypes, maxBlocks, breakLeaves);
        }
    }

    public static List<Block> findConnectedTimber(Block startBlock, Set<Material> logTypes, 
            Set<Material> leafTypes, int maxBlocks, boolean breakLeaves) {
        return findConnectedTimber(startBlock, logTypes, leafTypes, maxBlocks, breakLeaves, true, 2);
    }

    public static List<Block> findVeinPreview(Block startBlock, Material targetMaterial, int maxBlocks, boolean diagonal) {
        return findConnectedBlocks(startBlock, targetMaterial, maxBlocks, diagonal);
    }

    public static List<Block> findVeinPreview(Block startBlock, Material targetMaterial, int maxBlocks) {
        return findVeinPreview(startBlock, targetMaterial, maxBlocks, false);
    }

    public static List<Block> findTimberPreview(Block startBlock, Set<Material> logTypes, 
            Set<Material> leafTypes, int maxBlocks, boolean breakLeaves, boolean strict, int maxHorizontalRadius) {
        return findConnectedTimber(startBlock, logTypes, leafTypes, maxBlocks, breakLeaves, strict, maxHorizontalRadius);
    }

    public static List<Block> findTimberPreview(Block startBlock, Set<Material> logTypes, 
            Set<Material> leafTypes, int maxBlocks, boolean breakLeaves) {
        return findTimberPreview(startBlock, logTypes, leafTypes, maxBlocks, breakLeaves, true, 2);
    }

    /**
     * Encode block position to long for efficient HashSet storage
     * Format: X (26 bits) | Z (26 bits) | Y (12 bits)
     */
    private static long encodeBlock(Block block) {
        return encodePosition(block.getX(), block.getY(), block.getZ());
    }

    private static long encodePosition(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (y & 0xFFF);
    }
}
