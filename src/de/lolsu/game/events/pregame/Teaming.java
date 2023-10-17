package de.lolsu.game.events.pregame;

import de.lolsu.game.main.Team;
import de.lolsu.game.managers.PlayerManager;
import de.lolsu.game.managers.VoteManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

public class Teaming implements Listener {

	// Open the teaming inventory
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(e.getItem() == null || e.getItem().getType() != Material.BOAT || !(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
		Player p = e.getPlayer();
		p.openInventory(PlayerManager.getInventory(p));
	}

	@EventHandler
	public void onInvInteract(InventoryClickEvent e) {
		if(e.getClickedInventory() == null || !e.getClickedInventory().getName().equalsIgnoreCase(ChatColor.AQUA + "Team with somebody!") ||
				e.getSlot() < 0 || e.getSlot() >= 9 || e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

		String teamItemName = e.getCurrentItem().getItemMeta().getDisplayName();
		int team = Integer.parseInt(teamItemName.substring(teamItemName.length() - 1)) - 1;
		Player p = (Player) e.getWhoClicked();

		Team t = PlayerManager.getTeam(p);
		if(t == null) { // Join team
			PlayerManager.teamList.get(team).addPlayer(p);
		} else if(t.id == team) { // Leave team
			t.removePlayer(p);
		} else { // Switch team
			t.removePlayer(p);
			PlayerManager.teamList.get(team).addPlayer(p);
		}

		// Update the inventories of all players
		for(Player pl : Bukkit.getOnlinePlayers())
			if(pl.getOpenInventory() != null && pl.getOpenInventory().getTitle().equalsIgnoreCase(ChatColor.AQUA + "Team with somebody!"))
				pl.getOpenInventory().getTopInventory().setContents(PlayerManager.getInventory(pl).getContents());
	}

}
