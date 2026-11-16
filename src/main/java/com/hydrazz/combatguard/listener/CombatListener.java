package com.hydrazz.combatguard.listener;

import com.hydrazz.combatguard.CombatGuard;
import com.hydrazz.combatguard.config.ConfigManager;
import com.hydrazz.combatguard.manager.CombatManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

public class CombatListener implements Listener {

    private final CombatGuard plugin;
    private final CombatManager combatManager;

    public CombatListener(CombatGuard plugin) {
        this.plugin = plugin;
        this.combatManager = plugin.getCombatManager();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }

        Player victim = (Player) entity;
        Entity damager = event.getDamager();
        ConfigManager config = this.plugin.getConfigManager();

        // 1. Direct Player melee combat
        if (damager instanceof Player) {
            Player attacker = (Player) damager;
            if (victim.getUniqueId().equals(attacker.getUniqueId())) {
                if (!config.isTagSelfDamage()) return;
            }
            if (config.isTagVictim()) {
                this.combatManager.tag(victim, attacker);
            }
            if (config.isTagAttacker() && !victim.getUniqueId().equals(attacker.getUniqueId())) {
                this.combatManager.tag(attacker, victim);
            }
            return;
        }

        // 2. Projectile attacks (Arrows, Tridents, Snowballs, Fireworks)
        if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;
            ProjectileSource shooter = projectile.getShooter();

            if (shooter instanceof Player) {
                Player attacker = (Player) shooter;
                if (victim.getUniqueId().equals(attacker.getUniqueId())) {
                    if (!config.isTagSelfDamage()) return;
                }
                if (config.isTagVictim()) {
                    this.combatManager.tag(victim, attacker);
                }
                if (config.isTagAttacker() && !victim.getUniqueId().equals(attacker.getUniqueId())) {
                    this.combatManager.tag(attacker, victim);
                }
                return;
            } else if (shooter instanceof Monster && config.isTagOnMobDamage()) {
                if (config.isTagVictim()) {
                    this.combatManager.tag(victim, (Entity) shooter);
                }
                return;
            }
        }

        // 3. TNT / Explosive attacks
        if (damager instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) damager;
            Entity source = tnt.getSource();
            if (source instanceof Player) {
                Player attacker = (Player) source;
                if (config.isTagVictim()) {
                    this.combatManager.tag(victim, attacker);
                }
                if (config.isTagAttacker() && !victim.getUniqueId().equals(attacker.getUniqueId())) {
                    this.combatManager.tag(attacker, victim);
                }
                return;
            }
        }

        // 4. Hostile mob attacks (configurable)
        if (damager instanceof Monster && config.isTagOnMobDamage()) {
            if (config.isTagVictim()) {
                this.combatManager.tag(victim, damager);
            }
        }
    }
}
