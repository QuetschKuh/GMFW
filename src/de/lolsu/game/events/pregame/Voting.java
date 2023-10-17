package de.lolsu.game.events.pregame;

import de.lolsu.game.managers.VoteManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;

public class Voting implements Listener {
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(!((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getItem() != null && e.getItem().getType() == Material.DIAMOND)) return;
		e.setCancelled(true);
		
		e.getPlayer().openInventory(VoteManager.getInventory(e.getPlayer()));
	}
	
	@EventHandler
	public void onInvInteract(InventoryClickEvent e) {
		if(e.getClickedInventory() == null || !e.getClickedInventory().getName().equalsIgnoreCase(ChatColor.AQUA + "Vote for a map!") || e.getSlot() < 0 || e.getSlot() >= 54) return;

		// Calculate and set player vote
		int index = (int) Math.floor(e.getSlot() / 9f);
		VoteManager.setPlayerVote((Player) e.getWhoClicked(), index);

		// Update the inventories of all players
		for(Player p : Bukkit.getOnlinePlayers())
			if(p.getOpenInventory() != null && p.getOpenInventory().getTitle().equalsIgnoreCase(ChatColor.AQUA + "Vote for a map!"))
				p.getOpenInventory().getTopInventory().setContents(VoteManager.getInventory(p).getContents());
	}
	
}
