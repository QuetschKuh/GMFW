package de.lolsu.game.events;

import de.lolsu.game.events.pregame.Credits;
import de.lolsu.game.main.Config;
import de.lolsu.game.main.Gamemode;
import de.lolsu.game.managers.GamestateManager;
import de.lolsu.game.managers.StatManager;
import de.lolsu.game.managers.PlayerManager;
import de.lolsu.game.misc.ItemStacks;
import de.lolsu.game.misc.Messenger;
import de.lolsu.game.misc.ScoreBoards;
import de.lolsu.game.misc.Skulls;
import de.lolsu.game.netty.PacketReader;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerJoinLeave implements Listener {

	Map<Player, PacketReader> pprMap = new HashMap<>();

	ItemStack teamBoat;
	ItemStack voteDiamond;
	ItemStack ruleBook;

	// Create lobby items
	public PlayerJoinLeave() {

		teamBoat = ItemStacks.create(Material.BOAT, ChatColor.BLUE + "" + ChatColor.BOLD + "Team up!");
		voteDiamond = ItemStacks.create(Material.DIAMOND, ChatColor.AQUA + "" + ChatColor.BOLD + "Vote!");

		// Rule book
		ruleBook = ItemStacks.create(Material.WRITTEN_BOOK, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Rules");
		BookMeta ruleMeta = (BookMeta) ruleBook.getItemMeta();
		ruleMeta.setTitle(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Rules");
		ruleMeta.setAuthor("lolsu.de");
		// For some reason reading the config will interpret \n differently than when hardcoded, so we need to do it like this to actually write any line breaks (???)
		List<String> rules = Config.config.getStringList("Rules");
		for(String s : rules) rules.set(rules.indexOf(s), s.replace("<br>", "\n"));
		ruleMeta.setPages(rules);
		ruleBook.setItemMeta(ruleMeta);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		if(p.getGameMode() == GameMode.CREATIVE) return;

		PlayerManager.resetPlayer(p);
		pprMap.put(p, new PacketReader(p));

		switch(GamestateManager.gamestate) {
			case Pregame:
				if(Bukkit.getOnlinePlayers().size() >= Gamemode.singleton.maxPlayers) p.kickPlayer("The Lobby is full already");

				p.teleport(Config.getLocation("Spawns.Lobby", Bukkit.getWorld("Lobby")));
				p.setGameMode(GameMode.ADVENTURE);

				PlayerManager.registerPlayer(p);
				StatManager.registerPlayer(p);

				// Fill players inventory with lobby items
				//ItemStack profileSkull = Skulls.getPlayerSkull(ChatColor.GREEN + "" + ChatColor.BOLD + "Inventory", new ArrayList<>(), p.getName());
				//p.getInventory().setItem(0, profileSkull);			// Put the profile/inventory skull in the players hot bar
				if(Gamemode.singleton.maxTeamSize > 1)
					p.getInventory().setItem(2, teamBoat);			// Put the teaming boat in the players hot bar
				p.getInventory().setItem(4, voteDiamond); 			// Put the voting diamond in the players hot bar
				p.getInventory().setItem(6, ruleBook);				// Put the rulebook in the players hot bar
				p.getInventory().setItem(8, Credits.creditsItem);	// Put the credits watcher in the players hot bar

				if(Bukkit.getOnlinePlayers().size() >= Gamemode.singleton.minPlayers)
					GamestateManager.startGame();

				e.setJoinMessage(ChatColor.AQUA + p.getDisplayName() + ChatColor.BLUE + " is ready to rumble!");
			break;

			case Midgame:
				if(Config.customReconnectSystem) break;
				Location l = Config.getLocation("Spawns.Spectator." + Gamemode.currentMap, Bukkit.getWorld("Game"));
				p.teleport(l);
				p.setGameMode(GameMode.SPECTATOR);

				e.setJoinMessage(ChatColor.GRAY + p.getDisplayName() + ChatColor.DARK_GRAY + " is spectating the game.");
			break;

			case Postgame:
				p.kickPlayer("The round is over. Please wait for the server to restart!");
			break;
		}

		ScoreBoards.createScoreBoard(p);
		ScoreBoards.setPlayerListHeaderFooter(p, ChatColor.WHITE + "lolsu.de\n" + ChatColor.AQUA + "Where fun is had\n", ChatColor.GREEN + "\n" + Gamemode.singleton.name);

	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		Player p = e.getPlayer();

		pprMap.get(p).uninject();
		pprMap.remove(p);
		
		switch (GamestateManager.gamestate) {
			case Pregame:
				// Checks if there are not enough players anymore in the Lobby and cancels the Game start if that's the case; Uses <= because at the time of the event the player is still technically online
				if(Bukkit.getOnlinePlayers().size() < Gamemode.singleton.minPlayers) {
					if(GamestateManager.runnable != null) GamestateManager.runnable.cancel();
					Messenger.sendTitleBars(Bukkit.getOnlinePlayers(), ChatColor.RED + "Canceled!", ChatColor.DARK_RED + "Waiting for players...", 5, 15, 20);
					for(Player op : Bukkit.getOnlinePlayers()) {
						op.setLevel(0);
						op.setExp(0);
					}
					GamestateManager.waitingForPlayers();
				}
				// Removes the players team and stats
				PlayerManager.removePlayer(p);
				StatManager.removePlayer(p);
			break;

			case Midgame:
				Death.killPlayer(p);
			break;

			case Postgame:

			break;
		}

		ScoreBoards.removeScoreBoard(p);

		e.setQuitMessage(ChatColor.DARK_AQUA + p.getDisplayName() + ChatColor.DARK_BLUE + " just didn't feel like it anymore :(");
	}
	
}
