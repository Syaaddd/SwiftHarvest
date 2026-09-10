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
        return isOnCooldown(player, 1.0);
    }

    public boolean isOnCooldown(Player player, double multiplier) {
        if (!cooldowns.containsKey(player.getUniqueId())) {
            return false;
        }

        long lastUse = cooldowns.get(player.getUniqueId());
        long currentTime = System.currentTimeMillis();
        long cooldownMs = (long) (cooldownTicks * 50L * multiplier);

        if (currentTime - lastUse >= cooldownMs) {
            cooldowns.remove(player.getUniqueId());
            return false;
        }

        return true;
    }

    public long getRemainingCooldown(Player player) {
        return getRemainingCooldown(player, 1.0);
    }

    public long getRemainingCooldown(Player player, double multiplier) {
        if (!cooldowns.containsKey(player.getUniqueId())) {
            return 0;
        }

        long lastUse = cooldowns.get(player.getUniqueId());
        long currentTime = System.currentTimeMillis();
        long cooldownMs = (long) (cooldownTicks * 50L * multiplier);
        long remaining = cooldownMs - (currentTime - lastUse);

        return Math.max(0, remaining / 1000);
    }

    public void setCooldown(Player player) {
        setCooldown(player, 1.0);
    }

    public void setCooldown(Player player, double multiplier) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void removeCooldown(Player player) {
        cooldowns.remove(player.getUniqueId());
    }

    public void clearAll() {
        cooldowns.clear();
    }
}
