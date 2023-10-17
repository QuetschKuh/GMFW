package de.lolsu.game.main;

import de.lolsu.game.managers.IPhaseManager;
import de.lolsu.game.managers.PhaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.WorldCreator;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Gamemode {

	public static JavaPlugin plugin;
	public static Gamemode singleton;
	public static PluginManager pm;
	public String name;
	public String shortName;
	
	public int minPlayers;
	public int maxPlayers;
	public int maxTeamSize;
	public int maxTeamAmount;

	public IPhaseManager customManager;
	public Listener[] events;

	static List<Listener> listeners = new ArrayList<>();

	public static String currentMap = "null";
	
	/** The events need to be passed here so that we can register them while in game and not in the pre or after game */
	public Gamemode(JavaPlugin plugin, String name, String shortName, int minPlayers, int maxPlayers, int maxTeamSize, IPhaseManager customManager, Listener... events) {
		Gamemode.plugin = plugin;
		this.name = name;
		this.shortName = shortName;
		this.minPlayers = minPlayers;
		this.maxPlayers = maxPlayers;
		this.maxTeamSize = maxTeamSize;
		this.customManager = customManager;
		this.events = events;

		maxTeamAmount = maxPlayers / maxTeamSize;

		singleton = this;

		Config.createConfig();

		pm = Bukkit.getPluginManager();
		PhaseManager.initialize();

		// Load the worlds
		new WorldCreator("Lobby").createWorld().setDifficulty(Difficulty.PEACEFUL);
	}

	public static void registerEvent(Listener l) {
		listeners.add(l);
		pm.registerEvents(l, plugin);
	}

	/** Unregisters all events from the class matching the given class name
	 * @param name The name of the class with the events that should be unregistered
	 * @deprecated Insecure and might break easily (e.g. renaming a class). Please use {@link #unregisterEvent(Class)} for a more secure method.<br>
	 * Will be removed in the future
	 * */
	public static void unregisterEvent(String name) {
		for(Listener l : listeners) {
			if(!l.getClass().getSimpleName().equalsIgnoreCase(name)) continue;

			HandlerList.unregisterAll(l);
			listeners.remove(l);
			return;
		}
	}

	/** Unregisters all events by the given class that have been previously registered
	 * @param listener The listener class with the events that should be unregistered
	 */
	public static void unregisterEvent(Class<? extends Listener> listener) {
		for(Listener l : listeners) {
			if(!l.getClass().equals(listener)) continue;

			HandlerList.unregisterAll(l);
			listeners.remove(l);
			return;
		}
	}
	
}