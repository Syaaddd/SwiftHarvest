package com.github.Syaaddd.swiftHarvest.manager;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages per-player toggle state and hold tracking for activation modes.
 * 
 * Activation modes:
 * - sneak: classic (handled in BlockBreakListener directly)
 * - toggle: player runs /sh toggle, then breaks normally
 * - hold: player must hold sneak for N ticks, then active until sneak released
 */
public class ActivationManager {

    // Toggle mode: tracks which players have SwiftHarvest toggled ON
    private final Map<UUID, Boolean> toggleState = new HashMap<>();

    // Hold mode: tracks how many ticks player has been holding sneak
    private final Map<UUID, Integer> holdTicks = new HashMap<>();
    // Whether hold threshold has been reached (active until release)
    private final Map<UUID, Boolean> holdActive = new HashMap<>();

    // === Toggle Mode ===

    public boolean isToggledOn(Player player) {
        return toggleState.getOrDefault(player.getUniqueId(), true); // default ON
    }

    public void toggle(Player player) {
        UUID id = player.getUniqueId();
        toggleState.put(id, !toggleState.getOrDefault(id, true));
    }

    public void setToggled(Player player, boolean enabled) {
        toggleState.put(player.getUniqueId(), enabled);
    }

    public void removeToggle(Player player) {
        toggleState.remove(player.getUniqueId());
    }

    // === Hold Mode ===

    /**
     * Call every tick while player is sneaking in hold mode.
     * @return true if hold threshold is now reached (active)
     */
    public boolean tickHold(Player player, int requiredTicks) {
        UUID id = player.getUniqueId();
        int current = holdTicks.getOrDefault(id, 0) + 1;
        holdTicks.put(id, current);

        if (current >= requiredTicks) {
            holdActive.put(id, true);
            // Play a subtle activation sound
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.5f);
            return true;
        }
        return false;
    }

    /**
     * Call when player stops sneaking in hold mode. Resets hold counter.
     */
    public void releaseHold(Player player) {
        UUID id = player.getUniqueId();
        holdTicks.remove(id);
        holdActive.remove(id);
    }

    /**
     * Check if hold is currently active (threshold reached, still sneaking).
     */
    public boolean isHoldActive(Player player) {
        return holdActive.getOrDefault(player.getUniqueId(), false);
    }

    /**
     * Get current hold tick count.
     */
    public int getHoldTicks(Player player) {
        return holdTicks.getOrDefault(player.getUniqueId(), 0);
    }

    public void clearAll() {
        toggleState.clear();
        holdTicks.clear();
        holdActive.clear();
    }
}
