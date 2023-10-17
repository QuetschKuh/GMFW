package de.lolsu.game.events.midgame;

import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class Warmup implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if(e.getFrom().getX() != e.getTo().getX() || e.getFrom().getZ() != e.getTo().getZ()) {
            e.getPlayer().teleport(e.getFrom().setDirection(e.getTo().getDirection()));
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.CAT_HISS, 0.25f, 1);
            for (int i = 0; i < 4; i++) for (int y = 0; y < 5; y++)
                e.getPlayer().spigot().playEffect(
                        e.getPlayer().getLocation().add(i == 0 ? -1 : i == 3 ? 1 : 0, y * 0.4, i == 1 ? 1 : i == 2 ? -1 : 0),
                        Effect.COLOURED_DUST, 0, 0,
                        1, 0, 0,
                        1, 0, 2);
        }
    }

}
