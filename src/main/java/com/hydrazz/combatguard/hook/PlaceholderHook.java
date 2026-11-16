package com.hydrazz.combatguard.hook;

import com.hydrazz.combatguard.CombatGuard;
import com.hydrazz.combatguard.manager.CombatManager;
import com.hydrazz.combatguard.manager.CombatTag;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderHook extends PlaceholderExpansion {

    private final CombatGuard plugin;
    private final CombatManager combatManager;

    public PlaceholderHook(CombatGuard plugin) {
        this.plugin = plugin;
        this.combatManager = plugin.getCombatManager();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "combatguard";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Hydrazz";
    }

    @Override
    public @NotNull String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        boolean inCombat = this.combatManager.isInCombat(player.getUniqueId());
        CombatTag tag = this.combatManager.getTag(player.getUniqueId());

        switch (params.toLowerCase()) {
            case "in_combat":
            case "is_tagged":
                return inCombat ? "true" : "false";

            case "time_left":
            case "seconds_left":
                return inCombat && tag != null ? String.valueOf(tag.getRemainingSeconds()) : "0";

            case "time_formatted": {
                if (!inCombat || tag == null) return "00:00";
                int secs = tag.getRemainingSeconds();
                int m = secs / 60;
                int s = secs % 60;
                return String.format("%02d:%02d", m, s);
            }

            case "opponent":
            case "opponent_name":
                return inCombat && tag != null ? tag.getOpponentName() : "None";

            case "status":
                return inCombat ? "In Combat" : "Safe";

            case "progress":
                return inCombat && tag != null ? String.format("%.2f", tag.getProgress()) : "0.00";

            default:
                return null;
        }
    }
}
