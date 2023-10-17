package de.lolsu.game.managers;

import org.bukkit.event.entity.EntityDamageEvent;

public interface IDeathManager {

    void onPlayerDeath(EntityDamageEvent e);

}
