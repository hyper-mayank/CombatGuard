package com.hydrazz.combatguard.listener;

import com.hydrazz.combatguard.CombatGuard;
import com.hydrazz.combatguard.config.ConfigManager;
import com.hydrazz.combatguard.manager.CombatManager;
import com.hydrazz.combatguard.manager.CombatTag;
import com.hydrazz.combatguard.util.ColorUtil;
import com.hydrazz.combatguard.util.SoundUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class CombatRestrictionListener implements Listener {

    private final CombatGuard plugin;
    private final CombatManager combatManager;

    public CombatRestrictionListener(CombatGuard plugin) {
        this.plugin = plugin;
        this.combatManager = plugin.getCombatManager();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("combatguard.bypass.commands") || player.hasPermission("combatguard.bypass")) {
            return;
        }

        if (this.combatManager.isInCombat(player)) {
            ConfigManager config = this.plugin.getConfigManager();
            String command = event.getMessage();

            if (config.isCommandBlocked(command)) {
                event.setCancelled(true);
                CombatTag tag = this.combatManager.getTag(player.getUniqueId());
                int remaining = tag != null ? tag.getRemainingSeconds() : config.getDurationSeconds();

                String msg = config.getMessage("blocked-command", "<red>You cannot use commands while in combat! <white>(%time%s left)</white></red>")
                        .replace("%time%", String.valueOf(remaining))
                        .replace("%command%", command);
                player.sendMessage(ColorUtil.parse(config.getPrefix() + msg));
                SoundUtil.playSound(player, config.getSoundBlocked(), 0.7f, 0.8f);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGlideToggle(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (player.hasPermission("combatguard.bypass.elytra") || player.hasPermission("combatguard.bypass")) {
            return;
        }

        ConfigManager config = this.plugin.getConfigManager();
        if (config.isBlockElytra() && event.isGliding() && this.combatManager.isInCombat(player)) {
            event.setCancelled(true);
            player.setGliding(false);
            String msg = config.getMessage("blocked-elytra", "<red>Elytra flight is disabled during active combat!</red>");
            player.sendMessage(ColorUtil.parse(config.getPrefix() + msg));
            SoundUtil.playSound(player, config.getSoundBlocked(), 0.7f, 0.8f);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFlightToggle(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        if (player.hasPermission("combatguard.bypass")) return;

        ConfigManager config = this.plugin.getConfigManager();
        if (config.isBlockFlight() && this.combatManager.isInCombat(player)) {
            event.setCancelled(true);
            player.setAllowFlight(false);
            player.setFlying(false);
            SoundUtil.playSound(player, config.getSoundBlocked(), 0.7f, 0.8f);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        if (player.hasPermission("combatguard.bypass")) return;

        ConfigManager config = this.plugin.getConfigManager();
        if (config.isBlockEnderChest() && event.getInventory().getType() == InventoryType.ENDER_CHEST) {
            if (this.combatManager.isInCombat(player)) {
                event.setCancelled(true);
                String msg = config.getMessage("blocked-ender-chest", "<red>Opening Ender Chests is prohibited while in combat!</red>");
                player.sendMessage(ColorUtil.parse(config.getPrefix() + msg));
                SoundUtil.playSound(player, config.getSoundBlocked(), 0.7f, 0.8f);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractEnderChest(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clicked = event.getClickedBlock();
        if (clicked == null || clicked.getType() != Material.ENDER_CHEST) return;

        Player player = event.getPlayer();
        if (player.hasPermission("combatguard.bypass")) return;

        ConfigManager config = this.plugin.getConfigManager();
        if (config.isBlockEnderChest() && this.combatManager.isInCombat(player)) {
            event.setCancelled(true);
            String msg = config.getMessage("blocked-ender-chest", "<red>Opening Ender Chests is prohibited while in combat!</red>");
            player.sendMessage(ColorUtil.parse(config.getPrefix() + msg));
            SoundUtil.playSound(player, config.getSoundBlocked(), 0.7f, 0.8f);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("combatguard.bypass")) return;

        ConfigManager config = this.plugin.getConfigManager();
        if (config.isBlockPortals() && this.combatManager.isInCombat(player)) {
            event.setCancelled(true);
            String msg = config.getMessage("blocked-portal", "<red>Portals are sealed while in combat!</red>");
            player.sendMessage(ColorUtil.parse(config.getPrefix() + msg));
            SoundUtil.playSound(player, config.getSoundBlocked(), 0.7f, 0.8f);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("combatguard.bypass")) return;

        ConfigManager config = this.plugin.getConfigManager();
        if (config.isBlockTeleport() && this.combatManager.isInCombat(player)) {
            PlayerTeleportEvent.TeleportCause cause = event.getCause();
            if (cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL ||
                cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT ||
                cause == PlayerTeleportEvent.TeleportCause.COMMAND ||
                cause == PlayerTeleportEvent.TeleportCause.PLUGIN) {
                event.setCancelled(true);
                String msg = config.getMessage("blocked-teleport", "<red>Teleportation is prohibited while in combat!</red>");
                player.sendMessage(ColorUtil.parse(config.getPrefix() + msg));
                SoundUtil.playSound(player, config.getSoundBlocked(), 0.7f, 0.8f);
            }
        }
    }
}
