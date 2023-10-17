package de.lolsu.game.managers;

import de.lolsu.game.main.Gamemode;
import de.lolsu.game.main.Team;
import de.lolsu.game.misc.ItemStacks;
import de.lolsu.game.misc.Messenger;
import de.lolsu.game.misc.Skulls;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public class PlayerManager {

    public static HashMap<Player, Team> playerTeamMap = new HashMap<>();
    public static List<Team> teamList = new ArrayList<>();
    public static List<Team> teamsAlive = new ArrayList<>();

    public static Team getTeam(Player p) {
        return playerTeamMap.getOrDefault(p, null);
    }

    public static void initialize() {
        for(int i = 0; i < Gamemode.singleton.maxTeamAmount; i++)
            teamList.add(new Team());
    }

    public static void registerPlayer(Player p) {

    }

    public static void removePlayer(Player p) {
        Team t = getTeam(p);
        if(t != null) t.removePlayer(p);
        VoteManager.removePlayerVote(p);
    }

    /**
     * Assigns players which haven't chosen a team to a random team
     * */
    public static void assignTeams() {
        for(Player p : Bukkit.getOnlinePlayers()) {
            if(playerTeamMap.containsKey(p)) continue;
            teamList.stream().min(Comparator.comparingInt(Team::size)).get().addPlayer(p);
            if(Gamemode.singleton.maxTeamSize > 1)
                Messenger.sendLines(p, ChatColor.AQUA + "Since you haven't chosen a team,\n" + ChatColor.AQUA + "you have been automatically assigned to one.");
        }
    }

    public static String[] slots = {"", "5", "2:6", "2:4:6", "1:3:5:7", "0:2:4:6:8", "1:2:3:5:6:7", "1:2:3:4:5:6:7", "0:1:2:3:5:6:7:8"};

    public static Inventory getInventory(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.AQUA + "Team with somebody!");

        String[] slots = PlayerManager.slots[Gamemode.singleton.maxTeamAmount].split(":");
        for(int i = 0; i < Gamemode.singleton.maxTeamAmount; i++) {
            boolean teamFull = teamList.get(i).isFull();
            boolean teamOfPlayer = getTeam(p) != null && getTeam(p).id == i;
            List<String> lore = new ArrayList<>(PlayerManager.teamList.get(i).names(ChatColor.GRAY + ""));
            lore.add(0, teamOfPlayer ? ChatColor.RED + "Left click to leave." : teamFull ? ChatColor.RED + "Team is already full!" : ChatColor.GREEN + "Left click to join!");
            ItemStack item = ItemStacks.create(teamList.get(i).isFull() ? Material.MINECART : Material.BOAT, ChatColor.WHITE + "Team " + (i + 1), lore);
            if(teamOfPlayer) ItemStacks.addGlint(item);
            inv.setItem(Integer.parseInt(slots[i]), item);
        }

        ItemStack divider = ItemStacks.create(Material.STAINED_GLASS_PANE, ChatColor.GRAY + "Players not assigned");
        for(int i = 9; i < 18; i++)
            inv.setItem(i, divider);

        List<Player> playersRemaining = new ArrayList<>(Bukkit.getOnlinePlayers());
        playersRemaining.removeAll(playerTeamMap.keySet());
        for(int i = 18; i < 27; i++) {
            if((i - 18) >= playersRemaining.size()) break;
            Player rp = playersRemaining.get(i - 18);
            inv.setItem(i, Skulls.getPlayerSkull(ChatColor.WHITE + rp.getDisplayName(), Collections.singletonList(ChatColor.GRAY + "This player has not yet chosen a team"), rp.getName()));
        }

        return inv;
    }

    public static void killPlayer(Player p) {
        Team t = getTeam(p);
        t.killPlayer(p);
        if(t.isDead()) teamsAlive.remove(t);

        if(teamsAlive.size() <= 1)
            PhaseManager.endGame();
    }

    /**
     * Resets a player by clearing inventory, potion, health, firetick and xp
     * */
    public static void resetPlayer(Player p) {
        resetPlayer(p, null);
    }

    /**
     * Resets a player by clearing inventory, potion, health, firetick, xp and sets them to the given gamemode
     * */
    public static void resetPlayer(Player p, GameMode gamemode) {
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);
        for(PotionEffect pe : p.getActivePotionEffects()) p.removePotionEffect(pe.getType());
        p.setFireTicks(0);

        p.setMaxHealth(20);
        p.setSaturation(20);
        p.setHealth(20);

        p.setExp(0);
        p.setLevel(0);

        if(gamemode != null)
            p.setGameMode(gamemode);
    }

    /**
     * A friendly replacement for death lol
     * */
    public static void undie(Player p) {
        for(PotionEffect pe : p.getActivePotionEffects()) p.removePotionEffect(pe.getType());
        p.setFireTicks(0);

        p.setMaxHealth(20);
        p.setHealth(20);
        p.setSaturation(20);
    }

}
