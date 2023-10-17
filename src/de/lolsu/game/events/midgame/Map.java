package de.lolsu.game.events.midgame;

import de.lolsu.game.main.Config;
import de.lolsu.game.main.Gamemode;
import de.lolsu.game.managers.StatManager;
import de.lolsu.game.misc.Particles;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.WeatherType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class Map implements Listener {

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if(Config.mapBreakable || e.getBlock().hasMetadata("breakable") || Config.blocksBreakable.contains(e.getBlock().getType())) {
			StatManager.increaseStat(e.getPlayer(), StatManager.Statistic.BREAKS);
        } else {
			e.setCancelled(true);
			Location l = e.getBlock().getLocation().add(0.5, 0.5, 0.5);
			for(int i = 0; i < 6; i++) {
				Particles.sendParticlesLocal(
						e.getPlayer(),
						l.clone().add(i == 0 ? 0.6 : i == 1 ? -0.6 : 0, i == 2 ? 0.6 : i == 3 ? -0.6 : 0, i == 4 ? 0.6 : i == 5 ? -0.6 : 0),
						Effect.COLOURED_DUST, 1, 0, 0
				);
			}
			e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.CAT_HISS, 0.7f, 0.7f);
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		if(Config.mapPlaceable || Config.blocksPlaceable.contains(e.getBlock().getType())) {
			e.getBlock().setMetadata("breakable", new FixedMetadataValue(Gamemode.plugin, true));
			StatManager.increaseStat(e.getPlayer(), StatManager.Statistic.PLACES);
		} else {
			e.setCancelled(true);
			Location l = e.getBlock().getLocation().add(0.5, 0.5, 0.5);
			for(int i = 0; i < 6; i++) {
				Particles.sendParticlesLocal(
						e.getPlayer(),
						l.clone().add(i == 0 ? 0.6 : i == 1 ? -0.6 : 0, i == 2 ? 0.6 : i == 3 ? -0.6 : 0, i == 4 ? 0.6 : i == 5 ? -0.6 : 0),
						Effect.COLOURED_DUST, 1, 0, 0
				);
			}
			e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.CAT_HISS, 0.7f, 0.7f);
		}
	}

	@EventHandler
	public void onWeatherChange(WeatherChangeEvent e) {
		if(!e.toWeatherState() || Config.mapWeather == null) return;
		e.setCancelled(true);
		e.getWorld().setWeatherDuration(0);
		e.getWorld().setThundering(Config.mapWeather == WeatherType.DOWNFALL);
	}
	
}