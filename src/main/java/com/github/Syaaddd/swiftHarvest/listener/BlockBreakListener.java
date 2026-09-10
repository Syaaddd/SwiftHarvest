package com.github.Syaaddd.swiftHarvest.listener;

import com.github.Syaaddd.swiftHarvest.SwiftHarvest;
import com.github.Syaaddd.swiftHarvest.config.SwiftHarvestConfig;
import com.github.Syaaddd.swiftHarvest.config.SwiftHarvestConfig.ToolTier;
import com.github.Syaaddd.swiftHarvest.integration.WorldGuardHook;
import com.github.Syaaddd.swiftHarvest.manager.*;
import com.github.Syaaddd.swiftHarvest.scanner.BlockScanner;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BlockBreakListener implements Listener {

    private final SwiftHarvest plugin;
    private final SwiftHarvestConfig config;
    private final CooldownManager cooldownManager;
    private final ActivationManager activationManager;

    public BlockBreakListener(SwiftHarvest plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.cooldownManager = plugin.getCooldownManager();
        this.activationManager = plugin.getActivationManager();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (config.isWorldBlacklisted(block.getWorld().getName())) {
            return;
        }

        if (WorldGuardHook.isEnabled() && !WorldGuardHook.canBuild(player, block)) {
            return;
        }

        // Check activation based on mode
        if (!isActivationValid(player)) {
            return;
        }

        if (config.isVeinMinerEnabled()) {
            if (handleVeinMiner(event, player, block, blockType, tool)) {
                return;
            }
        }

        if (config.isTimberEnabled()) {
            handleTimber(event, player, block, blockType, tool);
        }
    }

    /**
     * Check if the current activation conditions are met based on configured mode.
     */
    private boolean isActivationValid(Player player) {
        String mode = config.getActivationMode();
        switch (mode.toLowerCase()) {
            case "toggle":
                // In toggle mode, SwiftHarvest must be toggled ON, no sneak required
                return activationManager.isToggledOn(player);
            case "hold":
                // In hold mode, hold must have reached the threshold
                return activationManager.isHoldActive(player);
            case "sneak":
            default:
                // Classic sneak mode
                return config.isRequireSneak() ? player.isSneaking() : true;
        }
    }

    private boolean handleVeinMiner(BlockBreakEvent event, Player player, Block block, Material blockType, ItemStack tool) {
        if (!config.getVeinMinerBlocks().contains(blockType)) {
            return false;
        }

        if (!config.getVeinMinerTools().contains(tool.getType())) {
            return false;
        }

        // Get tool tier config
        ToolTier tier = config.getVeinMinerTier(tool.getType());
        double cooldownMult = (tier != null) ? tier.getCooldownMultiplier() : 1.0;
        double durabilityMult = (tier != null) ? tier.getDurabilityMultiplier() : 1.0;
        int maxBlocks = config.getMaxBlocksForVeinMiner(tool.getType());

        if (cooldownManager.isOnCooldown(player, cooldownMult)) {
            long remaining = cooldownManager.getRemainingCooldown(player, cooldownMult);
            String message = config.getMessage("cooldown").replace("%seconds%", String.valueOf(remaining));
            player.sendMessage(message);
            return false;
        }

        // Scan with diagonal mode if configured
        boolean diagonal = config.getVeinMinerScanMode().equalsIgnoreCase("diagonal");
        List<Block> connectedBlocks = BlockScanner.findConnectedBlocks(block, blockType, maxBlocks, diagonal);

        if (connectedBlocks.size() <= 1) {
            return false;
        }

        if (!DurabilityManager.hasEnoughDurability(tool, connectedBlocks.size(), durabilityMult)) {
            player.sendMessage(config.getMessage("low-durability"));
            return false;
        }

        event.setCancelled(true);

        // Handle animated vs instant breaking
        if (config.isAnimationEnabled()) {
            handleAnimatedVeinMiner(player, tool, connectedBlocks, durabilityMult, cooldownMult);
        } else {
            handleInstantVeinMiner(player, tool, connectedBlocks, durabilityMult, cooldownMult);
        }

        return true;
    }

    private void handleInstantVeinMiner(Player player, ItemStack tool, List<Block> blocks, 
            double durabilityMult, double cooldownMult) {
        List<ItemStack> allDrops = new ArrayList<>();
        
        for (Block b : blocks) {
            Collection<ItemStack> drops = getDropsWithEnchantments(b, tool);
            allDrops.addAll(drops);
            b.setType(Material.AIR);
        }

        finishVeinMiner(player, tool, blocks, allDrops, durabilityMult, cooldownMult);
    }

    private void handleAnimatedVeinMiner(Player player, ItemStack tool, List<Block> blocks, 
            double durabilityMult, double cooldownMult) {
        
        int blocksPerTick = config.getBlocksPerTick();
        String style = config.getAnimationStyle();
        List<ItemStack> allDrops = new ArrayList<>();

        AnimationManager.BlockBreakCallback callback = new AnimationManager.BlockBreakCallback() {
            @Override
            public void onBlockBreak(Block block) {
                Collection<ItemStack> drops = getDropsWithEnchantments(block, tool);
                allDrops.addAll(drops);
            }

            @Override
            public void onComplete() {
                finishVeinMiner(player, tool, blocks, allDrops, durabilityMult, cooldownMult);
            }
        };

        switch (style.toLowerCase()) {
            case "wave":
                AnimationManager.animateWave(plugin, blocks, player, blocksPerTick, callback);
                break;
            case "random":
                AnimationManager.animateRandom(plugin, blocks, player, blocksPerTick, callback);
                break;
            case "ripple":
            default:
                AnimationManager.animateRipple(plugin, blocks, player, blocksPerTick, callback);
                break;
        }
    }

    private void finishVeinMiner(Player player, ItemStack tool, List<Block> blocks,
            List<ItemStack> allDrops, double durabilityMult, double cooldownMult) {
        List<ItemStack> mergedDrops = DropManager.mergeDrops(allDrops);
        boolean inventoryFull = !DropManager.consolidateDrops(player, mergedDrops);

        if (inventoryFull) {
            player.sendMessage(config.getMessage("full-inventory"));
        }

        DurabilityManager.reduceDurability(tool, blocks.size(), durabilityMult);
        cooldownManager.setCooldown(player, cooldownMult);

        player.sendMessage(config.getMessage("veinminer-started").replace("%amount%", String.valueOf(blocks.size())));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    private boolean handleTimber(BlockBreakEvent event, Player player, Block block, Material blockType, ItemStack tool) {
        if (!config.getTimberBlocks().contains(blockType)) {
            return false;
        }

        if (!config.getTimberTools().contains(tool.getType())) {
            return false;
        }

        // Get tool tier config
        ToolTier tier = config.getTimberTier(tool.getType());
        double cooldownMult = (tier != null) ? tier.getCooldownMultiplier() : 1.0;
        double durabilityMult = (tier != null) ? tier.getDurabilityMultiplier() : 1.0;
        int maxBlocks = config.getMaxBlocksForTimber(tool.getType());

        if (cooldownManager.isOnCooldown(player, cooldownMult)) {
            long remaining = cooldownManager.getRemainingCooldown(player, cooldownMult);
            String message = config.getMessage("cooldown").replace("%seconds%", String.valueOf(remaining));
            player.sendMessage(message);
            return false;
        }

        boolean breakLeaves = config.isBreakLeaves();
        boolean strict = config.getTimberDetection().equalsIgnoreCase("strict");
        int maxRadius = config.getMaxHorizontalRadius();

        List<Block> connectedBlocks = BlockScanner.findConnectedTimber(
            block, 
            config.getTimberBlocks(), 
            config.getLeafBlocks(), 
            maxBlocks, 
            breakLeaves,
            strict,
            maxRadius
        );

        if (connectedBlocks.size() <= 1) {
            return false;
        }

        if (!DurabilityManager.hasEnoughDurability(tool, connectedBlocks.size(), durabilityMult)) {
            player.sendMessage(config.getMessage("low-durability"));
            return false;
        }

        event.setCancelled(true);

        if (config.isAnimationEnabled()) {
            handleAnimatedTimber(player, tool, connectedBlocks, durabilityMult, cooldownMult);
        } else {
            handleInstantTimber(player, tool, connectedBlocks, durabilityMult, cooldownMult);
        }

        return true;
    }

    private void handleInstantTimber(Player player, ItemStack tool, List<Block> blocks, 
            double durabilityMult, double cooldownMult) {
        List<ItemStack> allDrops = new ArrayList<>();
        
        for (Block b : blocks) {
            Collection<ItemStack> drops = b.getDrops(tool);
            allDrops.addAll(drops);
            b.setType(Material.AIR);
        }

        finishTimber(player, tool, blocks, allDrops, durabilityMult, cooldownMult);
    }

    private void handleAnimatedTimber(Player player, ItemStack tool, List<Block> blocks, 
            double durabilityMult, double cooldownMult) {
        
        int blocksPerTick = config.getBlocksPerTick();
        String style = config.getAnimationStyle();
        List<ItemStack> allDrops = new ArrayList<>();

        AnimationManager.BlockBreakCallback callback = new AnimationManager.BlockBreakCallback() {
            @Override
            public void onBlockBreak(Block block) {
                Collection<ItemStack> drops = block.getDrops(tool);
                allDrops.addAll(drops);
            }

            @Override
            public void onComplete() {
                finishTimber(player, tool, blocks, allDrops, durabilityMult, cooldownMult);
            }
        };

        switch (style.toLowerCase()) {
            case "wave":
                AnimationManager.animateWave(plugin, blocks, player, blocksPerTick, callback);
                break;
            case "random":
                AnimationManager.animateRandom(plugin, blocks, player, blocksPerTick, callback);
                break;
            case "ripple":
            default:
                AnimationManager.animateRipple(plugin, blocks, player, blocksPerTick, callback);
                break;
        }
    }

    private void finishTimber(Player player, ItemStack tool, List<Block> blocks,
            List<ItemStack> allDrops, double durabilityMult, double cooldownMult) {
        List<ItemStack> mergedDrops = DropManager.mergeDrops(allDrops);
        boolean inventoryFull = !DropManager.consolidateDrops(player, mergedDrops);

        if (inventoryFull) {
            player.sendMessage(config.getMessage("full-inventory"));
        }

        DurabilityManager.reduceDurability(tool, blocks.size(), durabilityMult);
        cooldownManager.setCooldown(player, cooldownMult);

        player.sendMessage(config.getMessage("timber-started").replace("%amount%", String.valueOf(blocks.size())));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    /**
     * Get drops from a block, honoring Fortune and Silk Touch enchantments.
     * Bukkit's getDrops(ItemStack) automatically handles enchantment-based drops.
     */
    private Collection<ItemStack> getDropsWithEnchantments(Block block, ItemStack tool) {
        if (!config.isHonorFortune() && !config.isHonorSilkTouch()) {
            // If both disabled, use tool without enchantment effect
            return block.getDrops();
        }
        return block.getDrops(tool);
    }
}
