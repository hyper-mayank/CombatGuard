package com.hydrazz.combatguard;

import com.hydrazz.combatguard.command.CombatGuardCommand;
import com.hydrazz.combatguard.config.ConfigManager;
import com.hydrazz.combatguard.hook.PlaceholderHook;
import com.hydrazz.combatguard.hook.VaultHook;
import com.hydrazz.combatguard.listener.CombatListener;
import com.hydrazz.combatguard.listener.CombatRestrictionListener;
import com.hydrazz.combatguard.listener.PlayerConnectionListener;
import com.hydrazz.combatguard.manager.CombatManager;
import com.hydrazz.combatguard.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class CombatGuard extends JavaPlugin {

    private static CombatGuard instance;
    private ConfigManager configManager;
    private CombatManager combatManager;
    private VaultHook vaultHook;
    private PlaceholderHook placeholderHook;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize and load configurations
        this.configManager = new ConfigManager(this);
        this.configManager.load();

        // Initialize combat state engine
        this.combatManager = new CombatManager(this);
        this.combatManager.start();

        // Register core event listeners
        Bukkit.getPluginManager().registerEvents(new CombatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CombatRestrictionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(this), this);

        // Register command and tab completer
        CombatGuardCommand commandHandler = new CombatGuardCommand(this);
        if (getCommand("combatguard") != null) {
            getCommand("combatguard").setExecutor(commandHandler);
            getCommand("combatguard").setTabCompleter(commandHandler);
        }

        // Initialize soft dependency hooks
        setupHooks();

        // Log startup banner
        String platform = SchedulerUtil.isFolia() ? "Folia (Region Multi-Threading)" : "Paper/Spigot (Synchronous Scheduler)";
        getLogger().info("==================================================");
        getLogger().info("  CombatGuard v" + getDescription().getVersion() + " initialized successfully!");
        getLogger().info("  Platform: " + platform);
        getLogger().info("  Vault Hook: " + (vaultHook != null && vaultHook.isAvailable() ? "ENABLED" : "DISABLED"));
        getLogger().info("  PlaceholderAPI: " + (placeholderHook != null ? "REGISTERED" : "DISABLED"));
        getLogger().info("==================================================");
    }

    @Override
    public void onDisable() {
        if (this.combatManager != null) {
            this.combatManager.stop();
        }

        if (this.placeholderHook != null) {
            try {
                this.placeholderHook.unregister();
            } catch (Throwable ignored) {
            }
            this.placeholderHook = null;
        }

        getLogger().info("CombatGuard v" + getDescription().getVersion() + " disabled cleanly.");
        instance = null;
    }

    public void reloadPlugin() {
        if (this.configManager != null) {
            this.configManager.load();
        }
        if (this.combatManager != null) {
            this.combatManager.start();
        }
        getLogger().info("CombatGuard configuration and combat engine reloaded.");
    }

    private void setupHooks() {
        // Vault Economy
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            try {
                this.vaultHook = new VaultHook(this);
                if (this.vaultHook.isAvailable()) {
                    getLogger().info("Hooked into Vault economy provider successfully.");
                }
            } catch (Throwable t) {
                getLogger().warning("Failed to initialize Vault hook: " + t.getMessage());
            }
        }

        // PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                this.placeholderHook = new PlaceholderHook(this);
                if (this.placeholderHook.register()) {
                    getLogger().info("PlaceholderAPI expansion '%combatguard_*%' registered.");
                }
            } catch (Throwable t) {
                getLogger().warning("Failed to register PlaceholderAPI expansion: " + t.getMessage());
            }
        }
    }

    public static CombatGuard getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public PlaceholderHook getPlaceholderHook() {
        return placeholderHook;
    }
}
