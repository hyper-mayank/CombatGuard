package com.hydrazz.combatguard.config;

import com.hydrazz.combatguard.CombatGuard;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final CombatGuard plugin;

    private int durationSeconds;
    private boolean tagOnMobDamage;
    private boolean tagAttacker;
    private boolean tagVictim;
    private boolean tagSelfDamage;
    private boolean blockCommands;
    private Set<String> blockedCommands;
    private boolean blockElytra;
    private boolean blockFlight;
    private boolean blockEnderChest;
    private boolean blockPortals;
    private boolean blockTeleport;

    private boolean bossBarEnabled;
    private String bossBarTitle;
    private String bossBarColor;
    private String bossBarStyle;

    private boolean actionBarEnabled;
    private String actionBarMessage;

    private String punishMode;
    private boolean broadcastDeath;
    private String deathMessage;
    private List<String> consoleCommands;

    private String soundTagged;
    private String soundTick;
    private String soundExpired;
    private String soundCombatLog;
    private String soundBlocked;

    private String prefix;

    public ConfigManager(CombatGuard plugin) {
        this.plugin = plugin;
    }

    public void load() {
        this.plugin.saveDefaultConfig();
        this.plugin.reloadConfig();
        FileConfiguration config = this.plugin.getConfig();

        this.durationSeconds = Math.max(1, config.getInt("combat.duration-seconds", 15));
        this.tagOnMobDamage = config.getBoolean("combat.tag-on-mob-damage", false);
        this.tagAttacker = config.getBoolean("combat.tag-attacker", true);
        this.tagVictim = config.getBoolean("combat.tag-victim", true);
        this.tagSelfDamage = config.getBoolean("combat.tag-self-damage", false);
        this.blockCommands = config.getBoolean("combat.block-commands", true);

        List<String> rawCommands = config.getStringList("combat.blocked-commands");
        this.blockedCommands = new HashSet<>();
        for (String cmd : rawCommands) {
            String clean = cmd.trim().toLowerCase(Locale.ROOT);
            if (!clean.startsWith("/")) {
                clean = "/" + clean;
            }
            this.blockedCommands.add(clean);
        }

        this.blockElytra = config.getBoolean("combat.block-elytra", true);
        this.blockFlight = config.getBoolean("combat.block-flight", true);
        this.blockEnderChest = config.getBoolean("combat.block-ender-chest", true);
        this.blockPortals = config.getBoolean("combat.block-portals", true);
        this.blockTeleport = config.getBoolean("combat.block-teleport", false);

        this.bossBarEnabled = config.getBoolean("ui.bossbar.enabled", true);
        this.bossBarTitle = config.getString("ui.bossbar.title", "<gradient:#ff4444:#ffaa00>⚔ In Combat with %opponent%</gradient> <gray>(%time%s)</gray>");
        this.bossBarColor = config.getString("ui.bossbar.color", "RED");
        this.bossBarStyle = config.getString("ui.bossbar.style", "PROGRESS");

        this.actionBarEnabled = config.getBoolean("ui.actionbar.enabled", true);
        this.actionBarMessage = config.getString("ui.actionbar.message", "<red><bold>⚔ IN COMBAT</bold></red> <gray>•</gray> <yellow>%time%s left</yellow> <gray>(Opponent: <white>%opponent%</white>)</gray>");

        this.punishMode = config.getString("punish.mode", "KILL").toUpperCase(Locale.ROOT);
        this.broadcastDeath = config.getBoolean("punish.broadcast-death", true);
        this.deathMessage = config.getString("punish.death-message", "<red><bold>☠</bold></red> <white>%victim%</white> <red>combat logged while fighting</red> <gold>%attacker%</gold><red>! Items dropped.</red>");
        this.consoleCommands = config.getStringList("punish.commands");

        this.soundTagged = config.getString("sounds.tagged", "ENTITY_ENDER_DRAGON_GROWL");
        this.soundTick = config.getString("sounds.tick", "UI_BUTTON_CLICK");
        this.soundExpired = config.getString("sounds.expired", "ENTITY_PLAYER_LEVELUP");
        this.soundCombatLog = config.getString("sounds.combat-log", "ENTITY_WITHER_SPAWN");
        this.soundBlocked = config.getString("sounds.blocked", "BLOCK_NOTE_BLOCK_BASS");

        this.prefix = config.getString("messages.prefix", "<gradient:#ff3333:#ff7700>[CombatGuard]</gradient> ");
    }

    public String getMessage(String key, String fallback) {
        return this.plugin.getConfig().getString("messages." + key, fallback);
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public boolean isTagOnMobDamage() {
        return tagOnMobDamage;
    }

    public boolean isTagAttacker() {
        return tagAttacker;
    }

    public boolean isTagVictim() {
        return tagVictim;
    }

    public boolean isTagSelfDamage() {
        return tagSelfDamage;
    }

    public boolean isBlockCommands() {
        return blockCommands;
    }

    public boolean isCommandBlocked(String command) {
        if (!blockCommands || command == null) return false;
        String lower = command.trim().toLowerCase(Locale.ROOT);
        if (!lower.startsWith("/")) {
            lower = "/" + lower;
        }
        for (String blocked : blockedCommands) {
            if (lower.equals(blocked) || lower.startsWith(blocked + " ")) {
                return true;
            }
        }
        return false;
    }

    public Set<String> getBlockedCommands() {
        return Collections.unmodifiableSet(blockedCommands);
    }

    public boolean isBlockElytra() {
        return blockElytra;
    }

    public boolean isBlockFlight() {
        return blockFlight;
    }

    public boolean isBlockEnderChest() {
        return blockEnderChest;
    }

    public boolean isBlockPortals() {
        return blockPortals;
    }

    public boolean isBlockTeleport() {
        return blockTeleport;
    }

    public boolean isBossBarEnabled() {
        return bossBarEnabled;
    }

    public String getBossBarTitle() {
        return bossBarTitle;
    }

    public String getBossBarColor() {
        return bossBarColor;
    }

    public String getBossBarStyle() {
        return bossBarStyle;
    }

    public boolean isActionBarEnabled() {
        return actionBarEnabled;
    }

    public String getActionBarMessage() {
        return actionBarMessage;
    }

    public String getPunishMode() {
        return punishMode;
    }

    public boolean isBroadcastDeath() {
        return broadcastDeath;
    }

    public String getDeathMessage() {
        return deathMessage;
    }

    public List<String> getConsoleCommands() {
        return consoleCommands;
    }

    public String getSoundTagged() {
        return soundTagged;
    }

    public String getSoundTick() {
        return soundTick;
    }

    public String getSoundExpired() {
        return soundExpired;
    }

    public String getSoundCombatLog() {
        return soundCombatLog;
    }

    public String getSoundBlocked() {
        return soundBlocked;
    }

    public String getPrefix() {
        return prefix;
    }
}
