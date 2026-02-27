package com.github.Syaaddd.swiftHarvest.manager;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private int cooldownTicks = 10;

    public void setCooldownTicks(int ticks) {
        this.cooldownTicks = ticks;
    }

    public boolean isOnCooldown(Player player) {
        if (!cooldowns.containsKey(player.getUniqueId())) {
            return false;
        }

        long lastUse = cooldowns.get(player.getUniqueId());
        long currentTime = System.currentTimeMillis();
        long cooldownMs = cooldownTicks * 50L;

        if (currentTime - lastUse >= cooldownMs) {
            cooldowns.remove(player.getUniqueId());
            return false;
        }

        return true;
    }

    public long getRemainingCooldown(Player player) {
        if (!cooldowns.containsKey(player.getUniqueId())) {
            return 0;
        }

        long lastUse = cooldowns.get(player.getUniqueId());
        long currentTime = System.currentTimeMillis();
        long cooldownMs = cooldownTicks * 50L;
        long remaining = cooldownMs - (currentTime - lastUse);

        return Math.max(0, remaining / 1000);
    }

    public void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void removeCooldown(Player player) {
        cooldowns.remove(player.getUniqueId());
    }

    public void clearAll() {
        cooldowns.clear();
    }
}
