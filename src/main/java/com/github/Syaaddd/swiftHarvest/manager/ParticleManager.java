package com.github.Syaaddd.swiftHarvest.manager;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

public class ParticleManager {

    private static final Particle DUST_PARTICLE = Particle.DUST;
    private static final Color PREVIEW_COLOR = Color.fromRGB(0, 255, 128);

    public static void showVeinPreview(Player player, List<Block> blocks) {
        if (blocks == null || blocks.isEmpty()) return;

        World world = player.getWorld();
        Location center = player.getLocation();

        for (Block block : blocks) {
            Location blockLoc = block.getLocation();
            
            double x = blockLoc.getX();
            double y = blockLoc.getY();
            double z = blockLoc.getZ();
            
            world.spawnParticle(DUST_PARTICLE, x + 0.5, y + 0.5, z + 0.5, 1, 
                new Particle.DustOptions(PREVIEW_COLOR, 1.0f));
        }
    }

    public static void showTimberPreview(Player player, List<Block> blocks) {
        showVeinPreview(player, blocks);
    }

    public static void clearPreview(Player player) {
    }

    public static void showBlockHighlight(Player player, Block block) {
        if (block == null) return;

        World world = player.getWorld();
        Location loc = block.getLocation();

        world.spawnParticle(DUST_PARTICLE, loc.getX() + 0.5, loc.getY() + 0.5, loc.getZ() + 0.5, 4,
            new Particle.DustOptions(PREVIEW_COLOR, 1.5f));
    }
}
