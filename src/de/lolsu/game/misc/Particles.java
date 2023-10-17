package de.lolsu.game.misc;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Particles {

    public static void sendParticlesLocal(Player player, Location loc, Effect particle, int r, int g, int b) {
        player.spigot().playEffect(loc, particle, 0, 0, r, g, b, 1, 0, 10);
    }

}
