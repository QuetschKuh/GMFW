package de.lolsu.game.managers;

import de.lolsu.game.main.Config;
import de.lolsu.game.misc.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.Map.Entry;

public class VoteManager {

	static String[] maps;
	/** A Hashmap containing every map up for vote and the amount of votes for that map */
	public static Map<String, Integer> mapVotes = new LinkedHashMap<>();
	/** A Hashmap containing every player that voted and the index of the map they voted for */
	public static Map<UUID, Integer> playerVotes = new HashMap<>();
	
	/** An initializer that sets what maps should be up for vote */
	public static void initialize() {
		maps = Config.getMaps();
		List<String> mapList = Arrays.asList(maps);
		Collections.shuffle(mapList);
		mapList.toArray(maps);
		for(int i = 0; i < 5; i++) {
			mapVotes.put(mapList.get(i), 0);
		}
		mapVotes.put("RANDOM", 0);
	}
	
	/** Gets the map that is most voted for */
	public static String getMostVoted() {
		// What Map is currently the highest voted and how many votes that map has
		List<String> mostVotedList = new ArrayList<>();
		int voteCount = 0;
		
		// Iterate through every map change the voted var depending on if it was the highest voted 
		for(Entry<String, Integer> voted : mapVotes.entrySet()) {
			if(voted.getValue() < voteCount) continue;
			if(voted.getValue() > voteCount) mostVotedList.clear();
			mostVotedList.add(voted.getKey());
			voteCount = voted.getValue();
		}

		Random r = new Random();
		String mostVoted = mostVotedList.get(r.nextInt(mostVotedList.size()));
		if(mostVoted.equals("RANDOM")) return maps[r.nextInt(maps.length)];
		else return mostVoted;
	}
	
	/** A method to get the voting inventory for a specific player */
	public static Inventory getInventory(Player p) {
		
		// The inventory
		Inventory inv = Bukkit.createInventory(null, 54, ChatColor.AQUA + "Vote for a map!");
		int totalVotes = mapVotes.values().stream().mapToInt(Integer::intValue).sum();
		
		// Iterate through all maps for the main panes
		int i = 0;
		for(Entry<String, Integer> e : mapVotes.entrySet()) {

			// If the player voted for this
			boolean playerVoted = playerVotes.containsKey(p.getUniqueId()) && playerVotes.get(p.getUniqueId()) == i;
			
			// Set the main item
			ItemStack is = new ItemStack(i == 5 ? Material.REDSTONE_BLOCK : playerVoted ? Material.MAP : Material.EMPTY_MAP, e.getValue());
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(ChatColor.GOLD + Config.fancy(e.getKey()));
			im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			List<String> lore = new ArrayList<>();
			lore.add(playerVoted ? ChatColor.GOLD + "You already voted for this map!" : ChatColor.AQUA + "Vote for this Map!");
			lore.add(ChatColor.GRAY + "Votes: " + ChatColor.GREEN + e.getValue());
			im.setLore(lore);
			is.setItemMeta(im);
			if(playerVoted) is.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
			inv.setItem(i*9, is);

			// The panes for votes or not votes
			ItemStack voteItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
			ItemMeta voteMeta = voteItem.getItemMeta();
			voteMeta.setDisplayName(playerVoted ? ChatColor.GOLD + "You already voted for this map!" : ChatColor.AQUA + "Vote for this Map!");
			voteMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			voteItem.setItemMeta(voteMeta);
			if(playerVoted) voteItem.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
			ItemStack bgItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
			ItemMeta bgMeta = bgItem.getItemMeta();
			bgMeta.setDisplayName(playerVoted ? ChatColor.GOLD + "You already voted for this map!" : ChatColor.DARK_AQUA + "Vote for this Map!");
			bgItem.setItemMeta(bgMeta);

			// Calculate how many panes should be coloured (and avoid division by zero)
			int a = (int) (((float) e.getValue() / (totalVotes <= 0 ? 1f : (float) totalVotes)) * 8f);

			// Iterate through every slot where a pane should be
			for(int c = 1; c < 9; c++) {
				if(c <= a) inv.setItem(i*9 + c, voteItem);
				else inv.setItem(i*9 + c, bgItem);
			}

			i++;
		}
		
		return inv;
	}

	public static void setPlayerVote(Player p, int index) {
		UUID u = p.getUniqueId();
		String map = index == 5 ? "RANDOM" : maps[index];
		if (!playerVotes.containsKey(u) || playerVotes.get(u) != index) {
			if(playerVotes.containsKey(u)) {
				String lastMap = playerVotes.get(u) == 5 ? "RANDOM" : maps[playerVotes.get(u)];
				mapVotes.replace(lastMap, mapVotes.get(lastMap) - 1);
			}
			mapVotes.replace(map, mapVotes.get(map) +1);
			playerVotes.put(u, index);
		} else {
			Messenger.send(p, ChatColor.RED + "You already voted for this Map!");
		}
	}

	public static void removePlayerVote(Player p) {
		UUID u = p.getUniqueId();
		if(!playerVotes.containsKey(u)) return;
		String map = playerVotes.get(u) == 5 ? "RANDOM" : maps[playerVotes.get(u)];
		mapVotes.replace(map, mapVotes.get(map) - 1);
		playerVotes.remove(u);
	}
	
}
