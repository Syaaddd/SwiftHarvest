package com.github.Syaaddd.swiftHarvest.manager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.github.Syaaddd.swiftHarvest.SwiftHarvest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Handles animated block breaking with multiple visual styles.
 */
public class AnimationManager {

    /**
     * Animate block breaking with ripple effect (from player outward)
     */
    public static BukkitTask animateRipple(SwiftHarvest plugin, List<Block> blocks, Player player, int blocksPerTick, BlockBreakCallback callback) {
        List<Block> sorted = new ArrayList<>(blocks);
        Location playerLoc = player.getLocation();
        sorted.sort(Comparator.comparingDouble(b -> b.getLocation().distanceSquared(playerLoc)));
        return animateBatch(plugin, sorted, player, blocksPerTick, callback);
    }

    /**
     * Animate block breaking with wave effect (bottom-up)
     */
    public static BukkitTask animateWave(SwiftHarvest plugin, List<Block> blocks, Player player, int blocksPerTick, BlockBreakCallback callback) {
        List<Block> sorted = new ArrayList<>(blocks);
        sorted.sort(Comparator.comparingInt(Block::getY));
        return animateBatch(plugin, sorted, player, blocksPerTick, callback);
    }

    /**
     * Animate block breaking with random order
     */
    public static BukkitTask animateRandom(SwiftHarvest plugin, List<Block> blocks, Player player, int blocksPerTick, BlockBreakCallback callback) {
        List<Block> shuffled = new ArrayList<>(blocks);
        Collections.shuffle(shuffled);
        return animateBatch(plugin, shuffled, player, blocksPerTick, callback);
    }

    private static BukkitTask animateBatch(SwiftHarvest plugin, List<Block> blocks, Player player, int blocksPerTick, BlockBreakCallback callback) {
        return new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }

                int end = Math.min(index + blocksPerTick, blocks.size());

                for (int i = index; i < end; i++) {
                    Block block = blocks.get(i);

                    if (!block.isEmpty() && block.getType() != Material.AIR) {
                        // Play break sound
                        World world = block.getWorld();
                        Location loc = block.getLocation();
                        world.playSound(loc, Sound.BLOCK_STONE_BREAK, 0.5f, 1.2f);

                        // Spawn break particles
                        try {
                            world.spawnParticle(Particle.BLOCK,
                                loc.getX() + 0.5, loc.getY() + 0.5, loc.getZ() + 0.5,
                                10, block.getBlockData());
                        } catch (Exception ignored) {
                            // Particle data may not match in some cases
                        }

                        // Notify callback BEFORE breaking (so drops can be computed)
                        if (callback != null) {
                            callback.onBlockBreak(block);
                        }

                        // Break the block
                        block.setType(Material.AIR);
                    }
                }

                index = end;

                if (index >= blocks.size()) {
                    if (callback != null) {
                        callback.onComplete();
                    }
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Callback interface for animation progress
     */
    public interface BlockBreakCallback {
        void onBlockBreak(Block block);
        void onComplete();
    }
}
