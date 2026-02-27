package com.github.Syaaddd.swiftHarvest.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.github.Syaaddd.swiftHarvest.SwiftHarvest;

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
    }

    private void createDefaultConfig() {
        try (InputStream is = plugin.getResource("config.yml")) {
            if (is != null) {
                Files.copy(is, configFile.toPath());
            } else {
                config = new YamlConfiguration();
                config.set("settings.require-sneak", true);
                config.set("settings.max-blocks", 64);
                config.set("settings.cooldown", 10);
                config.set("settings.preview-particles", true);
                config.set("veinminer.enabled", true);
                config.set("veinminer.allowed-tools", Arrays.asList(
                    "WOODEN_PICKAXE", "STONE_PICKAXE", "IRON_PICKAXE",
                    "GOLDEN_PICKAXE", "DIAMOND_PICKAXE", "NETHERITE_PICKAXE"
                ));
                config.set("veinminer.valid-blocks", Arrays.asList(
                    "COAL_ORE", "IRON_ORE", "GOLD_ORE", "DIAMOND_ORE",
                    "REDSTONE_ORE", "LAPIS_ORE", "EMERALD_ORE",
                    "NETHER_QUARTZ_ORE", "ANCIENT_DEBRIS", "COPPER_ORE", "DEEPSLATE_COAL_ORE",
                    "DEEPSLATE_IRON_ORE", "DEEPSLATE_GOLD_ORE", "DEEPSLATE_DIAMOND_ORE",
                    "DEEPSLATE_REDSTONE_ORE", "DEEPSLATE_LAPIS_ORE", "DEEPSLATE_EMERALD_ORE",
                    "DEEPSLATE_COPPER_ORE", "COPPER_ORE", "GLOWSTONE"
                ));
                config.set("timber.enabled", true);
                config.set("timber.allowed-tools", Arrays.asList(
                    "WOODEN_AXE", "STONE_AXE", "IRON_AXE",
                    "GOLDEN_AXE", "DIAMOND_AXE", "NETHERITE_AXE"
                ));
                config.set("timber.break-leaves", false);
                config.set("timber.require-sapling", false);
                config.set("worlds.blacklist", Arrays.asList("world_nether", "world_the_end"));
                config.save(configFile);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create default config: " + e.getMessage());
        }
    }

    private void createDefaultMessages() {
        try {
            messages = new YamlConfiguration();
            messages.set("prefix", "&7[&bSwiftHarvest&7] ");
            messages.set("messages.cooldown", "&cTunggu %seconds% detik sebelum menggunakan lagi!");
            messages.set("messages.full-inventory", "&cInventory penuh! Item akan dijatuhkan.");
            messages.set("messages.no-permission", "&cKamu tidak memiliki izin untuk menggunakan ini.");
            messages.set("messages.disabled-world", "&cFitur ini tidak aktif di world ini.");
            messages.set("messages.veinminer-started", "&aVeinMiner diaktifkan! Menambang %amount% blok.");
            messages.set("messages.timber-started", "&aTimber diaktifkan! Menebang %amount% blok.");
            messages.set("messages.low-durability", "&cDurability alat tidak cukup!");
            messages.set("messages.config-reloaded", "&aKonfigurasi berhasil dimuat ulang.");
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
        return config.getBoolean("timber.break-leaves", false);
    }

    public boolean isRequireSapling() {
        return config.getBoolean("timber.require-sapling", false);
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
        return leaves;
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
        return messages.getString("prefix", "&7[&bSwiftHarvest&7] ") + 
               messages.getString("messages." + path, "");
    }

    public String getRawMessage(String path) {
        return messages.getString("messages." + path, "");
    }
}
