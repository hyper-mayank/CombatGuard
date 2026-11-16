package com.hydrazz.combatguard.command;

import com.hydrazz.combatguard.CombatGuard;
import com.hydrazz.combatguard.config.ConfigManager;
import com.hydrazz.combatguard.manager.CombatManager;
import com.hydrazz.combatguard.manager.CombatTag;
import com.hydrazz.combatguard.util.ColorUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CombatGuardCommand implements CommandExecutor, TabCompleter {

    private final CombatGuard plugin;
    private final CombatManager combatManager;

    public CombatGuardCommand(CombatGuard plugin) {
        this.plugin = plugin;
        this.combatManager = plugin.getCombatManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        ConfigManager config = this.plugin.getConfigManager();

        if (args.length == 0 || args[0].equalsIgnoreCase("status") || args[0].equalsIgnoreCase("time")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ColorUtil.parse(config.getPrefix() + "<red>Only in-game players can check combat status.</red>"));
                return true;
            }
            Player player = (Player) sender;
            if (this.combatManager.isInCombat(player)) {
                CombatTag tag = this.combatManager.getTag(player.getUniqueId());
                int seconds = tag != null ? tag.getRemainingSeconds() : 0;
                String oppName = tag != null ? tag.getOpponentName() : "Unknown";
                String msg = config.getMessage("status", "<yellow>You are in combat with <white>%opponent%</white> for <gold>%time%s</gold>.</yellow>")
                        .replace("%opponent%", oppName)
                        .replace("%time%", String.valueOf(seconds));
                player.sendMessage(ColorUtil.parse(config.getPrefix() + msg));
            } else {
                String msg = config.getMessage("not-in-combat", "<green>You are currently safe and not in combat.</green>");
                player.sendMessage(ColorUtil.parse(config.getPrefix() + msg));
            }
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("reload")) {
            if (!sender.hasPermission("combatguard.admin")) {
                sender.sendMessage(ColorUtil.parse(config.getPrefix() + "<red>You lack permission to reload CombatGuard.</red>"));
                return true;
            }
            this.plugin.reloadPlugin();
            String msg = config.getMessage("config-reloaded", "<green>CombatGuard configuration reloaded successfully.</green>");
            sender.sendMessage(ColorUtil.parse(config.getPrefix() + msg));
            return true;
        }

        if (sub.equals("tag")) {
            if (!sender.hasPermission("combatguard.admin")) {
                sender.sendMessage(ColorUtil.parse(config.getPrefix() + "<red>You lack permission to tag players.</red>"));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ColorUtil.parse(config.getPrefix() + "<yellow>Usage: /cg tag <player> [seconds]</yellow>"));
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(ColorUtil.parse(config.getPrefix() + "<red>Player not found or offline.</red>"));
                return true;
            }
            int duration = config.getDurationSeconds();
            if (args.length >= 3) {
                try {
                    duration = Integer.parseInt(args[2]);
                } catch (NumberFormatException ignored) {
                }
            }
            this.combatManager.tag(target, sender instanceof Player ? (Player) sender : null);
            String msg = config.getMessage("admin-tagged", "<green>Forcefully tagged <white>%player%</white> into combat for <white>%time%s</white>.</green>")
                    .replace("%player%", target.getName())
                    .replace("%time%", String.valueOf(duration));
            sender.sendMessage(ColorUtil.parse(config.getPrefix() + msg));
            return true;
        }

        if (sub.equals("untag")) {
            if (!sender.hasPermission("combatguard.admin")) {
                sender.sendMessage(ColorUtil.parse(config.getPrefix() + "<red>You lack permission to untag players.</red>"));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ColorUtil.parse(config.getPrefix() + "<yellow>Usage: /cg untag <player></yellow>"));
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(ColorUtil.parse(config.getPrefix() + "<red>Player not found.</red>"));
                return true;
            }
            this.combatManager.untag(target.getUniqueId(), false);
            String msg = config.getMessage("admin-untagged", "<green>Forcefully untagged <white>%player%</white> from combat.</green>")
                    .replace("%player%", target.getName());
            sender.sendMessage(ColorUtil.parse(config.getPrefix() + msg));
            return true;
        }

        // Help menu
        sender.sendMessage(ColorUtil.parse("<gold><bold>CombatGuard</bold></gold> <gray>v" + this.plugin.getDescription().getVersion() + " by Hydrazz</gray>"));
        sender.sendMessage(ColorUtil.parse("<yellow>/cg status</yellow> <gray>- Check your active combat status</gray>"));
        if (sender.hasPermission("combatguard.admin")) {
            sender.sendMessage(ColorUtil.parse("<yellow>/cg tag <player> [seconds]</yellow> <gray>- Force tag a player</gray>"));
            sender.sendMessage(ColorUtil.parse("<yellow>/cg untag <player></yellow> <gray>- Remove a player's combat tag</gray>"));
            sender.sendMessage(ColorUtil.parse("<yellow>/cg reload</yellow> <gray>- Reload plugin configuration</gray>"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>(Arrays.asList("status", "time", "help"));
            if (sender.hasPermission("combatguard.admin")) {
                options.add("tag");
                options.add("untag");
                options.add("reload");
            }
            List<String> result = new ArrayList<>();
            for (String s : options) {
                if (s.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(s);
                }
            }
            return result;
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("tag") || args[0].equalsIgnoreCase("untag"))) {
            if (sender.hasPermission("combatguard.admin")) {
                List<String> players = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        players.add(p.getName());
                    }
                }
                return players;
            }
        }

        return Collections.emptyList();
    }
}
