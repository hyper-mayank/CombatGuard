package com.hydrazz.combatguard.hook;

import com.hydrazz.combatguard.CombatGuard;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {

    private final CombatGuard plugin;
    private Economy economy;
    private boolean available = false;

    public VaultHook(CombatGuard plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    public boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        try {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                this.economy = rsp.getProvider();
                this.available = (this.economy != null);
            }
        } catch (Throwable t) {
            this.available = false;
        }
        return this.available;
    }

    public boolean isAvailable() {
        return this.available && this.economy != null;
    }

    public Economy getEconomy() {
        return this.economy;
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        if (!isAvailable() || player == null || amount <= 0) return false;
        try {
            return this.economy.withdrawPlayer(player, amount).transactionSuccess();
        } catch (Throwable t) {
            return false;
        }
    }
}
