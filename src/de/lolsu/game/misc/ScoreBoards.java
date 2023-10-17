package de.lolsu.game.misc;

import de.lolsu.game.main.Config;
import de.lolsu.game.main.Gamemode;
import de.lolsu.game.managers.StatManager;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Field;
import java.util.HashMap;

public class ScoreBoards {

    static HashMap<Player, Scoreboard> scoreboards = new HashMap<Player, Scoreboard>();

    private static Integer taskId;
    private static void scoreboardUpdater() {
        if(taskId != null && (Bukkit.getScheduler().isCurrentlyRunning(taskId) || Bukkit.getScheduler().isQueued(taskId))) return;

        taskId = new BukkitRunnable() {
            @Override public void run() {
                for(Player p : scoreboards.keySet()) {
                    if(!p.isOnline()) removeScoreBoard(p);

                    Scoreboard board = scoreboards.get(p);
                    board.getTeam("PLAYERS").setSuffix(ChatColor.GRAY + "" + Bukkit.getOnlinePlayers().size());
                    board.getTeam("STAT1").setSuffix(ChatColor.GRAY + "" + StatManager.getStat(p, Config.statsDisplayed[0]));
                    board.getTeam("STAT2").setSuffix(ChatColor.GRAY + "" + StatManager.getStat(p, Config.statsDisplayed[1]));
                    board.getTeam("STAT3").setSuffix(ChatColor.GRAY + "" + StatManager.getStat(p, Config.statsDisplayed[2]));

                    p.setScoreboard(board);
                }
            }
        }.runTaskTimer(Gamemode.plugin, 0L, 20L).getTaskId();
    }

    public static void createScoreBoard(Player p) {
        if(scoreboards.containsKey(p)) removeScoreBoard(p);

        Scoreboard board = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
        Objective sidebar = board.registerNewObjective("sidebar", "dummy");
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
        sidebar.setDisplayName(ChatColor.DARK_PURPLE + "lolsu" + ChatColor.GOLD + Gamemode.singleton.shortName);
        sidebar.getScore("§0").setScore(90);
        sidebar.getScore(ChatColor.RED + "Time left").setScore(85);
        sidebar.getScore("§a").setScore(80); // Time
        sidebar.getScore("§1").setScore(75);
        sidebar.getScore(ChatColor.GREEN + "Players left").setScore(70);
        sidebar.getScore("§b").setScore(65); // Players
        sidebar.getScore("§2").setScore(60);
        sidebar.getScore(ChatColor.AQUA + "Your Stats").setScore(55);
        sidebar.getScore("§c").setScore(50); // Stat 1
        sidebar.getScore("§d").setScore(45); // Stat 2
        sidebar.getScore("§e").setScore(40); // Stat 3
        sidebar.getScore("§3").setScore(35);
        sidebar.getScore(ChatColor.DARK_GRAY + "─────").setScore(30);
        sidebar.getScore(ChatColor.WHITE + "lolsu.de").setScore(25);
        Team t;
        //t = board.registerNewTeam("TIME"); t.addEntry("§a");
        t = board.registerNewTeam("PLAYERS"); t.addEntry("§b");
        t = board.registerNewTeam("STAT1"); t.addEntry("§c"); t.setPrefix(ChatColor.WHITE + Config.statsDisplayed[0].name + ": ");
        t = board.registerNewTeam("STAT2"); t.addEntry("§d"); t.setPrefix(ChatColor.WHITE + Config.statsDisplayed[1].name + ": ");
        t = board.registerNewTeam("STAT3"); t.addEntry("§e"); t.setPrefix(ChatColor.WHITE + Config.statsDisplayed[2].name + ": ");

        scoreboards.put(p, board);
        scoreboardUpdater();
    }

    public static void removeScoreBoard(Player p) {
        p.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
        scoreboards.remove(p);
    }

    public static void broadcastPlayerListHeaderFooter(String header, String footer) {
        IChatBaseComponent icbcHeader = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + header + "\"}");
        IChatBaseComponent icbcFooter = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + footer + "\"}");
        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();

        try {
            Field fieldHeader = packet.getClass().getDeclaredField("a");
            Field fieldFooter = packet.getClass().getDeclaredField("b");
            fieldHeader.setAccessible(true);
            fieldFooter.setAccessible(true);
            fieldHeader.set(packet, icbcHeader);
            fieldFooter.set(packet, icbcFooter);
        } catch (Exception ex) { ex.printStackTrace(); }

        for(Player p : Bukkit.getOnlinePlayers())
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

    public static void setPlayerListHeaderFooter(Player p, String header, String footer) {
        IChatBaseComponent icbcHeader = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + header + "\"}");
        IChatBaseComponent icbcFooter = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + footer + "\"}");
        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();

        try {
            Field fieldHeader = packet.getClass().getDeclaredField("a");
            Field fieldFooter = packet.getClass().getDeclaredField("b");
            fieldHeader.setAccessible(true);
            fieldFooter.setAccessible(true);
            fieldHeader.set(packet, icbcHeader);
            fieldFooter.set(packet, icbcFooter);
        } catch (Exception ex) { ex.printStackTrace(); }

        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

}