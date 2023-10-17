package de.lolsu.game.events;

import de.lolsu.game.main.Config;
import de.lolsu.game.main.Gamemode;
import de.lolsu.game.managers.GamestateManager;
import de.lolsu.game.managers.IDeathManager;
import de.lolsu.game.managers.PlayerManager;
import de.lolsu.game.managers.StatManager;
import de.lolsu.game.misc.Chars;
import de.lolsu.game.misc.Messenger;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class Death implements Listener {

    static Map<Player, Player> lastDamager = new HashMap<>();
    static Map<Player, Long> lastTimeDamaged = new HashMap<>();

    @EventHandler
    public void onPlayerAttack(EntityDamageEvent e) {
        if(GamestateManager.gamestate == GamestateManager.Gamestate.Postgame) {
            e.setCancelled(true);
            return;
        }

        // Return if the damaged entity isn't a player
        if(!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();

        // If the player was damaged by another player through direct combat, a projectile, or tnt, set that player as the last damager
        if(e instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) e;
            if(ev.getDamager() instanceof Player) {
                lastDamager.put(p, (Player) ev.getDamager());
                lastTimeDamaged.put(p, System.currentTimeMillis());
            } else if(ev.getDamager() instanceof TNTPrimed && ((TNTPrimed) ev.getDamager()).getSource() instanceof Player) {
                lastDamager.put(p, (Player) ((TNTPrimed) ev.getDamager()).getSource());
                lastTimeDamaged.put(p, System.currentTimeMillis());
            } else if(ev.getDamager() instanceof Projectile && ((Projectile) ev.getDamager()).getShooter() instanceof Player) {
                lastDamager.put(p, (Player) ((Projectile) ev.getDamager()).getShooter());
                lastTimeDamaged.put(p, System.currentTimeMillis());
            }
        }

        // Player dead? do this
        if(p.getHealth() - e.getFinalDamage() > 0) return;
        if(Config.customRespawnSystem) {
            ((IDeathManager) Gamemode.singleton.customManager).onPlayerDeath(e);
            return;
        }
        e.setCancelled(true);

        killPlayer(p);

        p.setGameMode(GameMode.SPECTATOR);
        if(GamestateManager.gamestate == GamestateManager.Gamestate.Midgame) Messenger.sendTitleBar(p, ChatColor.RED + "You died.", ChatColor.GOLD + Chars.shrug, 5, 15, 20); // Avoid overwriting player won titlebar
        Location l = Config.getLocation("Spawns.Spectator." + Gamemode.currentMap, Bukkit.getWorld("Game"));
        p.teleport(l);
    }

    public static void killPlayer(Player p) {
        // If the player has been damaged by another player in the last 30 seconds, count it as their kill
        if(lastDamager.containsKey(p) && lastDamager.get(p) != p && System.currentTimeMillis() < lastTimeDamaged.get(p) + 30000) {
            Bukkit.broadcastMessage(ChatColor.GOLD + p.getDisplayName() + ChatColor.RESET + " got obliterated by " + ChatColor.RED + lastDamager.get(p).getDisplayName());
            StatManager.increaseStat(lastDamager.get(p), StatManager.Statistic.KILLS);
        } else {
            Bukkit.broadcastMessage(ChatColor.GOLD + p.getDisplayName() + ChatColor.RESET + " committed suicide");
        }
        StatManager.increaseStat(p, StatManager.Statistic.DEATHS);

        // Drop player inventory
        for(ItemStack i : p.getInventory().getContents())
            if(i != null && (Config.deathDropsEverything || Config.deathDropsWhitelist.contains(i.getType()))) p.getWorld().dropItemNaturally(p.getLocation(), i);

        p.getWorld().strikeLightningEffect(p.getLocation());

        PlayerManager.resetPlayer(p);
        PlayerManager.killPlayer(p);
    }

}
