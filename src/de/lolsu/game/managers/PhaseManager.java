package de.lolsu.game.managers;

import de.lolsu.game.events.Chat;
import de.lolsu.game.events.Death;
import de.lolsu.game.events.PlayerJoinLeave;
import de.lolsu.game.events.midgame.AutoLapis;
import de.lolsu.game.events.midgame.Map;
import de.lolsu.game.events.midgame.Soup;
import de.lolsu.game.events.midgame.Warmup;
import de.lolsu.game.events.pregame.Credits;
import de.lolsu.game.events.pregame.Lobby;
import de.lolsu.game.events.pregame.Teaming;
import de.lolsu.game.events.pregame.Voting;
import de.lolsu.game.main.Config;
import de.lolsu.game.main.Gamemode;
import de.lolsu.game.main.Team;
import de.lolsu.game.misc.Chars;
import de.lolsu.game.misc.Messenger;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PhaseManager {

	/** Register all Lobby and Voting Events */
	public static void initialize() {
		// Register Lobby Events
		Gamemode.registerEvent(new Lobby());
		Gamemode.registerEvent(new PlayerJoinLeave());
		Gamemode.registerEvent(new Chat());
		Gamemode.registerEvent(new Teaming());
		Gamemode.registerEvent(new Credits());

		PlayerManager.initialize();
		VoteManager.initialize();
		Gamemode.registerEvent(new Voting());

		GamestateManager.waitingForPlayers();
		GamestateManager.infoTexts();

		Gamemode.singleton.customManager.initialize();
	}
	
	/** Ends the voting and teaming phase */
	public static void endVoting() {
		// Unregister event, remove items and close the inventories
		Gamemode.unregisterEvent(Voting.class);
		for(Player p : Bukkit.getOnlinePlayers()) {
			p.getInventory().remove(Material.DIAMOND);
			p.getInventory().remove(Material.BOAT);
			p.closeInventory();
		}

		PlayerManager.assignTeams();

		// Load the most voted map and announce it
		String mostVoted = VoteManager.getMostVoted();
		try { Config.loadMap(mostVoted); }
		catch (IOException ex) { ex.printStackTrace(); }
		Messenger.broadcastLines(Chars.divider + "\n" +
				ChatColor.RED + "Voting ended!\n" +
				ChatColor.LIGHT_PURPLE + "The map " + ChatColor.GOLD + ChatColor.BOLD + Config.fancy(mostVoted) + ChatColor.LIGHT_PURPLE + " won!\n" +
				Chars.divider
		);

		Gamemode.singleton.customManager.endVoting();
	}

	/** Teleport everybody into the game and stuff */
	public static void startGame() {
		// Change gamestate and stop info texts
		GamestateManager.infoTexts.cancel();
		GamestateManager.gamestate = GamestateManager.Gamestate.Midgame;

		// Unregister and register mandatory events
		Gamemode.unregisterEvent(Lobby.class);
		Gamemode.unregisterEvent(Teaming.class);
		Gamemode.unregisterEvent(Credits.class);
		Gamemode.registerEvent(new Map());
		Gamemode.registerEvent(new Death());

		// Register optional events
		if(Config.soupHeals > 0) Gamemode.registerEvent(new Soup());
		if(!Config.warmupWorldBorder) Gamemode.registerEvent(new Warmup());
		if(Config.autoLapis) Gamemode.registerEvent(new AutoLapis());
		for(Listener l : Gamemode.singleton.events) Gamemode.registerEvent(l);

		// Initialize teams
		PlayerManager.teamsAlive = new ArrayList<>(PlayerManager.teamList);
        PlayerManager.teamsAlive.removeIf(Team::isEmpty);
		for(Team t : PlayerManager.teamList) t.initialize();

		// Get spawn locations and teleport players
		if(Gamemode.singleton.customManager instanceof IPhaseExtended) { ((IPhaseExtended) Gamemode.singleton.customManager).teleportPlayers(); }
		else {
			List<Location> spawns = Config.getLocationList("Spawns.Game." + Gamemode.currentMap, Bukkit.getWorld("Game"));
			int spawnsNeeded = Config.disableTeamSpawns ? PlayerManager.playerTeamMap.size() : PlayerManager.teamList.size();
			for (int i = 0; i < spawnsNeeded; i++) {
				Location l = spawns.get((int) Math.floor(i * (float) ((Config.disableTeamSpawns ? Gamemode.singleton.maxPlayers : Gamemode.singleton.maxPlayers / Gamemode.singleton.maxTeamSize) / spawnsNeeded)));
				if (Config.disableTeamSpawns) new ArrayList<>(PlayerManager.playerTeamMap.keySet()).get(i).teleport(l);
				else for (Player p : PlayerManager.teamList.get(i).getPlayers()) p.teleport(l);
			}
		}

		// Reset the players
		for(Player p : Bukkit.getOnlinePlayers()) {
			p.setGameMode(Config.playerGameMode);
			PlayerManager.resetPlayer(p);
		}

		// Start warmup
		Bukkit.getWorld("Game").setPVP(Config.warmupWorldBorder);
		Messenger.sendTitleBars(Bukkit.getOnlinePlayers(), ChatColor.GOLD + Gamemode.singleton.name, ChatColor.GRAY + "A " + ChatColor.WHITE + "lolsu.de" + ChatColor.GRAY + " Creation", 10, 30, 40);
		Messenger.broadcast(ChatColor.GREEN + "Game starting in " + ChatColor.DARK_GREEN + (Config.warmupWorldBorder ? Config.timeWarmup : Config.timeWatch));
		GamestateManager.warmup();

		// Send over to whatever plugin based on this framework
		Gamemode.singleton.customManager.startGame();
	}

	/**  */
	public static void endGame() {
		// Change gamestate and broadcast the winners
		GamestateManager.gamestate = GamestateManager.Gamestate.Postgame;
		List<String> names = PlayerManager.teamsAlive.get(0).names();
		String players = names.size() > 1 ?
				String.join(", ", names.subList(0, names.size() - 1))
						.concat(String.format("%s and ", names.size() > 2 ? "," : ""))
						.concat(names.get(names.size() - 1))
				: names.get(0);
		Messenger.broadcastTitleBar(ChatColor.RED + "Game Over!", ChatColor.GOLD + players + " won!", 10, 20, 70);

		// Load the podium world
		World w = new WorldCreator("Podium").createWorld();
		w.setDifficulty(Difficulty.PEACEFUL);
		w.setPVP(false);
		w.setThundering(false);

		GamestateManager.endGame();

		Gamemode.singleton.customManager.endGame();
	}
	
	/**  */
	public static void restart() {
		Gamemode.singleton.customManager.restart();

		for(Player p : Bukkit.getOnlinePlayers()) p.kickPlayer(ChatColor.RED + "Server is restarting...");
		try {
			File f = new File(Config.startFileLocation);
			if(!f.exists()) {
				f.createNewFile();
				Writer output = new BufferedWriter(new FileWriter(Config.startFileLocation));
				/** Combines the name of the two parent directories to make the tmux session so for a structure like this:
				 * -> Servers
				 * 		-> survival-games
				 * 			-> server-1
				 * 			-> server-2
				 * 		-> skywars
				 * 			-> solo-1
				 * 			-> duos-1
				 * The resulting names would be: survival-games-server-1, skywars-solo-1, ... */
				String[] sNameSplit = f.getAbsolutePath().split("/");
				String sessionName = sNameSplit[sNameSplit.length - 3] + "-" + sNameSplit[sNameSplit.length - 2];
				output.write("#!/bin/bash\nsleep 5;\ntmux new-session -d -s \"" + sessionName + "\";\ntmux send-keys -t \"" + sessionName + "\" \"java -Xms512M -Xmx512M -jar spigot.jar nogui\" ENTER;");
				output.close();
				Runtime.getRuntime().exec("chmod u+x" + Config.startFileLocation);
			}
			Runtime.getRuntime().exec("sh " + Config.startFileLocation);
		} catch (Exception ex) { ex.printStackTrace(); }
		Bukkit.shutdown();
	}
	
}
