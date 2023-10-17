package de.lolsu.game.events.midgame;

import de.lolsu.game.managers.StatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class Damage implements Listener {

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if(e.getEntity() instanceof Player) StatManager.increaseStat((Player) e.getEntity(), StatManager.Statistic.RECEIVED, (int) e.getFinalDamage());
    }

    @EventHandler
    public void onPlayerDamageByPlayer(EntityDamageByEntityEvent e) {
        if(e.getDamager() instanceof Player) StatManager.increaseStat((Player) e.getDamager(), StatManager.Statistic.GIVEN, (int) e.getFinalDamage());
    }

}
