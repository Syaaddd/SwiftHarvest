package com.github.Syaaddd.swiftHarvest.integration;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sk89q.worldguard.LocalPlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WorldGuardHook {

    private static boolean enabled = false;
    private static StateFlag SWIFT_HARVEST_FLAG = null;

    public static void init(Plugin worldGuard) {
        if (worldGuard == null || !worldGuard.isEnabled()) {
            return;
        }

        try {
            FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
            if (registry != null) {
                SWIFT_HARVEST_FLAG = new StateFlag("swift-harvest", true);
                registry.register(SWIFT_HARVEST_FLAG);
            }
            enabled = true;
        } catch (Exception e) {
            enabled = false;
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean canBuild(Player player, Block block) {
        if (!enabled) return true;
        if (player == null || block == null) return true;

        try {
            // Fix 1: Gunakan getPlatform().getRegionContainer()
            RegionContainer container = WorldGuard.getInstance()
                    .getPlatform()
                    .getRegionContainer();
            if (container == null) return true;

            // Fix 2: Konversi Bukkit World ke WorldEdit World via BukkitAdapter
            RegionManager rm = container.get(BukkitAdapter.adapt(block.getWorld()));
            if (rm == null) return true;

            // Fix 3: Gunakan BlockVector3 dari WorldEdit bukan BlockVector lama
            BlockVector3 vec = BlockVector3.at(
                    block.getX(),
                    block.getY(),
                    block.getZ()
            );

            ApplicableRegionSet regions = rm.getApplicableRegions(vec);

            // Gunakan LocalPlayer untuk queryState agar permission player ikut dihitung
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            StateFlag.State state = regions.queryState(localPlayer, SWIFT_HARVEST_FLAG);

            return state != StateFlag.State.DENY;

        } catch (Exception e) {
            return true;
        }
    }
}