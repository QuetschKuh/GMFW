package de.lolsu.game.events.pregame;

import de.lolsu.game.main.Config;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class Lobby implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if(e.getPlayer().getGameMode() != GameMode.CREATIVE) e.setCancelled(true);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if(e.getPlayer().getGameMode() != GameMode.CREATIVE) e.setCancelled(true);
    }

    @EventHandler
    public void onInvInteract(InventoryInteractEvent e) {
        if(e.getWhoClicked().getGameMode() != GameMode.CREATIVE) e.setCancelled(true);
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if(e.getWhoClicked().getGameMode() != GameMode.CREATIVE) e.setCancelled(true);
    }

    @EventHandler
    public void onTakeDamage(EntityDamageEvent e) {
        if(e.getEntity().getWorld().getName().equalsIgnoreCase("Game")) return;
        if(e.getCause() == EntityDamageEvent.DamageCause.VOID) e.getEntity().teleport(Config.getLocation("Spawns.Lobby", Bukkit.getWorld("Lobby")));
        if(!(e instanceof EntityDamageByEntityEvent)) e.setCancelled(true);
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if(e.getDamager() instanceof Player && ((Player) e.getDamager()).isSneaking()) {
            e.setDamage(0); e.getEntity().setVelocity(e.getDamager().getLocation().getDirection().multiply(3).setY(80));
        } else e.setCancelled(true);
    }

}
