package com.github.Syaaddd.swiftHarvest.config;

import com.github.Syaaddd.swiftHarvest.SwiftHarvest;
import com.github.Syaaddd.swiftHarvest.util.MessageUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

public class SwiftHarvestConfig {

    private final SwiftHarvest plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private File configFile;
    private File messagesFile;

    // Tool tier cache
    private Map<Material, ToolTier> veinMinerTiers = new HashMap<>();
    private Map<Material, ToolTier> timberTiers = new HashMap<>();

    public SwiftHarvestConfig(SwiftHarvest plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        configFile = new File(plugin.getDataFolder(), "config.yml");
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!configFile.exists()) {
            createDefaultConfig();
        }
        if (!messagesFile.exists()) {
            createDefaultMessages();
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        // Load tool tiers
        loadToolTiers();
    }

    private void loadToolTiers() {
        veinMinerTiers.clear();
        timberTiers.clear();

        // Load VeinMiner tiers
        ConfigurationSection veinTiers = config.getConfigurationSection("veinminer.tool-tiers");
        if (veinTiers != null) {
            for (String toolName : veinTiers.getKeys(false)) {
                try {
                    Material tool = Material.valueOf(toolName);
                    ConfigurationSection tierConfig = veinTiers.getConfigurationSection(toolName);
                    if (tierConfig != null) {
                        int maxBlocks = tierConfig.getInt("max-blocks", 64);
                        double durabilityMult = tierConfig.getDouble("durability-multiplier", 1.0);
                        double cooldownMult = tierConfig.getDouble("cooldown-multiplier", 1.0);
                        veinMinerTiers.put(tool, new ToolTier(maxBlocks, durabilityMult, cooldownMult));
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        }

        // Load Timber tiers
        ConfigurationSection timberTiersSection = config.getConfigurationSection("timber.tool-tiers");
        if (timberTiersSection != null) {
            for (String toolName : timberTiersSection.getKeys(false)) {
                try {
                    Material tool = Material.valueOf(toolName);
                    ConfigurationSection tierConfig = timberTiersSection.getConfigurationSection(toolName);
                    if (tierConfig != null) {
                        int maxBlocks = tierConfig.getInt("max-blocks", 64);
                        double durabilityMult = tierConfig.getDouble("durability-multiplier", 1.0);
                        double cooldownMult = tierConfig.getDouble("cooldown-multiplier", 1.0);
                        timberTiers.put(tool, new ToolTier(maxBlocks, durabilityMult, cooldownMult));
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    private void createDefaultConfig() {
        try (InputStream is = plugin.getResource("config.yml")) {
            if (is != null) {
                Files.copy(is, configFile.toPath());
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create default config: " + e.getMessage());
        }
    }

    private void createDefaultMessages() {
        try {
            messages = new YamlConfiguration();
            messages.set("prefix", "&7[&bSwiftHarvest&7] ");
            messages.set("messages.cooldown", "&cWait %seconds% seconds before using again!");
            messages.set("messages.full-inventory", "&cInventory full! Items will be dropped.");
            messages.set("messages.no-permission", "&cYou do not have permission to use this.");
            messages.set("messages.disabled-world", "&cThis feature is disabled in this world.");
            messages.set("messages.veinminer-started", "&aVeinMiner activated! Mining %amount% blocks.");
            messages.set("messages.timber-started", "&aTimber activated! Chopping %amount% blocks.");
            messages.set("messages.low-durability", "&cNot enough durability on tool!");
            messages.set("messages.config-reloaded", "&aConfiguration reloaded successfully.");
            messages.set("messages.toggle-on", "&aSwiftHarvest enabled!");
            messages.set("messages.toggle-off", "&cSwiftHarvest disabled.");
            messages.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create default messages: " + e.getMessage());
        }
    }

    public void reload() {
        loadConfig();
    }

    // Settings
    public boolean isRequireSneak() {
        return config.getBoolean("settings.require-sneak", true);
    }

    public int getMaxBlocks() {
        return config.getInt("settings.max-blocks", 64);
    }

    public int getCooldown() {
        return config.getInt("settings.cooldown", 10);
    }

    public boolean isPreviewParticles() {
        return config.getBoolean("settings.preview-particles", true);
    }

    public String getActivationMode() {
        return config.getString("settings.activation-mode", "sneak");
    }

    public int getHoldTicks() {
        return config.getInt("settings.hold-ticks", 10);
    }

    // VeinMiner
    public boolean isVeinMinerEnabled() {
        return config.getBoolean("veinminer.enabled", true);
    }

    public Set<Material> getVeinMinerTools() {
        List<String> list = config.getStringList("veinminer.allowed-tools");
        Set<Material> tools = new HashSet<>();
        for (String s : list) {
            try {
                tools.add(Material.valueOf(s));
            } catch (IllegalArgumentException ignored) {}
        }
        return tools;
    }

    public Set<Material> getVeinMinerBlocks() {
        List<String> list = config.getStringList("veinminer.valid-blocks");
        Set<Material> blocks = new HashSet<>();
        for (String s : list) {
            try {
                blocks.add(Material.valueOf(s));
            } catch (IllegalArgumentException ignored) {}
        }
        return blocks;
    }

    public String getVeinMinerScanMode() {
        return config.getString("veinminer.scan-mode", "cardinal");
    }

    public boolean isHonorFortune() {
        return config.getBoolean("veinminer.honor-fortune", true);
    }

    public boolean isHonorSilkTouch() {
        return config.getBoolean("veinminer.honor-silk-touch", true);
    }

    public double getMaxFortuneMultiplier() {
        return config.getDouble("veinminer.max-fortune-multiplier", 3.0);
    }

    public boolean isUseVeinMinerTiers() {
        return config.getBoolean("veinminer.use-tiers", false);
    }

    public ToolTier getVeinMinerTier(Material tool) {
        return veinMinerTiers.get(tool);
    }

    public int getMaxBlocksForVeinMiner(Material tool) {
        if (isUseVeinMinerTiers() && veinMinerTiers.containsKey(tool)) {
            return veinMinerTiers.get(tool).getMaxBlocks();
        }
        return getMaxBlocks();
    }

    // Timber
    public boolean isTimberEnabled() {
        return config.getBoolean("timber.enabled", true);
    }

    public Set<Material> getTimberTools() {
        List<String> list = config.getStringList("timber.allowed-tools");
        Set<Material> tools = new HashSet<>();
        for (String s : list) {
            try {
                tools.add(Material.valueOf(s));
            } catch (IllegalArgumentException ignored) {}
        }
        return tools;
    }

    public boolean isBreakLeaves() {
        return config.getBoolean("timber.break-leaves", true);
    }

    public boolean isRequireSapling() {
        return config.getBoolean("timber.require-sapling", false);
    }

    public String getTimberDetection() {
        return config.getString("timber.detection", "strict");
    }

    public int getMaxHorizontalRadius() {
        return config.getInt("timber.max-horizontal-radius", 2);
    }

    public boolean isUseTimberTiers() {
        return config.getBoolean("timber.use-tiers", false);
    }

    public ToolTier getTimberTier(Material tool) {
        return timberTiers.get(tool);
    }

    public int getMaxBlocksForTimber(Material tool) {
        if (isUseTimberTiers() && timberTiers.containsKey(tool)) {
            return timberTiers.get(tool).getMaxBlocks();
        }
        return getMaxBlocks();
    }

    public Set<Material> getTimberBlocks() {
        Set<Material> blocks = new HashSet<>();
        blocks.add(Material.OAK_LOG);
        blocks.add(Material.SPRUCE_LOG);
        blocks.add(Material.BIRCH_LOG);
        blocks.add(Material.JUNGLE_LOG);
        blocks.add(Material.ACACIA_LOG);
        blocks.add(Material.DARK_OAK_LOG);
        blocks.add(Material.MANGROVE_LOG);
        blocks.add(Material.CHERRY_LOG);
        blocks.add(Material.PALE_OAK_LOG);
        blocks.add(Material.CRIMSON_STEM);
        blocks.add(Material.WARPED_STEM);
        return blocks;
    }

    public Set<Material> getLeafBlocks() {
        Set<Material> leaves = new HashSet<>();
        leaves.add(Material.OAK_LEAVES);
        leaves.add(Material.SPRUCE_LEAVES);
        leaves.add(Material.BIRCH_LEAVES);
        leaves.add(Material.JUNGLE_LEAVES);
        leaves.add(Material.ACACIA_LEAVES);
        leaves.add(Material.DARK_OAK_LEAVES);
        leaves.add(Material.MANGROVE_LEAVES);
        leaves.add(Material.CHERRY_LEAVES);
        leaves.add(Material.PALE_OAK_LEAVES);
        return leaves;
    }

    // Animation
    public boolean isAnimationEnabled() {
        return config.getBoolean("animation.enabled", false);
    }

    public int getBlocksPerTick() {
        return config.getInt("animation.blocks-per-tick", 4);
    }

    public String getAnimationStyle() {
        return config.getString("animation.style", "ripple");
    }

    // Worlds
    public List<String> getBlacklistedWorlds() {
        return config.getStringList("worlds.blacklist");
    }

    public boolean isWorldBlacklisted(String worldName) {
        return getBlacklistedWorlds().contains(worldName.toLowerCase());
    }

    // Messages
    public String getMessage(String path) {
        String prefix = MessageUtils.colorize(messages.getString("prefix", "&7[&bSwiftHarvest&7] "));
        String message = MessageUtils.colorize(messages.getString("messages." + path, ""));
        return prefix + message;
    }

    public String getRawMessage(String path) {
        return MessageUtils.colorize(messages.getString("messages." + path, ""));
    }

    // Tool tier data class
    public static class ToolTier {
        private final int maxBlocks;
        private final double durabilityMultiplier;
        private final double cooldownMultiplier;

        public ToolTier(int maxBlocks, double durabilityMultiplier, double cooldownMultiplier) {
            this.maxBlocks = maxBlocks;
            this.durabilityMultiplier = durabilityMultiplier;
            this.cooldownMultiplier = cooldownMultiplier;
        }

        public int getMaxBlocks() {
            return maxBlocks;
        }

        public double getDurabilityMultiplier() {
            return durabilityMultiplier;
        }

        public double getCooldownMultiplier() {
            return cooldownMultiplier;
        }
    }
}
