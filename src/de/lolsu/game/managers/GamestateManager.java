package de.lolsu.game.managers;

import de.lolsu.game.events.midgame.Warmup;
import de.lolsu.game.main.Config;
import de.lolsu.game.main.Gamemode;
import de.lolsu.game.misc.Chars;
import de.lolsu.game.misc.Messenger;
import de.lolsu.game.misc.NPC;
import de.lolsu.game.misc.WorldBorders;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GamestateManager {

	public static int countdown;
	public static BukkitTask runnable;
	static BukkitTask waitingRunnable;
	public static BukkitTask infoTexts;

	/** The state of the game - 0 = Voting/Pregame, 1 = Midgame, 2 = Endscreen/Podium */
	public static Gamestate gamestate = Gamestate.Pregame;
	public enum Gamestate {
		Pregame, // Voting/Pregame
		Midgame, // During the match
		Postgame // Endscreen/Podium
	}

	/** Broadcast fun random Messages every 30 seconds */
	public static void infoTexts() {
		infoTexts = new BukkitRunnable() {
			List<String> texts = Config.config.getStringList("InfoTexts");
			final Random r = new Random();
			List<String> last = new ArrayList<>();
			@Override public void run() {
				// Make sure messages don't repeat (maximum repeat every 10 messages)
				String text = texts.get(r.nextInt(texts.size()));
				while(last.contains(text)) {
					text = texts.get(r.nextInt(texts.size()));
				}
				last.add(0, text);
				if(last.size() >= 10) last.remove(9);

				Bukkit.broadcastMessage("[" + ChatColor.GOLD + "Info" + ChatColor.RESET + "] " + text);
			}
		}.runTaskTimerAsynchronously(Gamemode.plugin, 0L, 600L);
	}

	public static void waitingForPlayers() {
		if(waitingRunnable != null && Bukkit.getScheduler().isCurrentlyRunning(waitingRunnable.getTaskId())) return;

		waitingRunnable = new BukkitRunnable() {
			@Override public void run() {
				Messenger.sendActionBars(Bukkit.getOnlinePlayers(), "Waiting for Players...");
			}
		}.runTaskTimerAsynchronously(Gamemode.plugin, 0L, 100L);
	}

	/** This will initialize a 3-minute countdown where after 2:50 minutes the voting is stopped and after another 10 the game is started */
	public static void startGame() {
		if(runnable != null && (Bukkit.getScheduler().isCurrentlyRunning(runnable.getTaskId()) || Bukkit.getScheduler().isQueued(runnable.getTaskId()))) return;

		waitingRunnable.cancel();
		countdown = 180;

		// Countdown
		runnable = new BukkitRunnable() {
			
			@Override public void run() {
			
				// Count down
				countdown--;

				// Send packets
				if(countdown <= 20) {
					ChatColor cc = (countdown > 5 ? ChatColor.GREEN : countdown > 3 ? ChatColor.YELLOW : ChatColor.RED);
					Messenger.sendTitleBars(
							Bukkit.getOnlinePlayers(),
							cc + Chars.blackCircledNumbers[countdown],
							cc + getCountdownMessage(countdown),
							0, 20, 20
					);
				} else {
					Messenger.sendActionBars(Bukkit.getOnlinePlayers(), getCountdownMessage(countdown));
				}

				for(Player p : Bukkit.getOnlinePlayers()) {
					p.setLevel(countdown);
					p.setExp((float) countdown / 180);
				}
				
				// Events
				if(countdown == 10) PhaseManager.endVoting();
				if(countdown == 0) { cancel(); PhaseManager.startGame(); }
			
		}}.runTaskTimer(Gamemode.plugin, 0L, 20L);
		
	}

	private static String getCountdownMessage(int cd) {
		if(cd > 70) return "Waiting for Players...";
		if(cd > 20) return "Voting ends in " + ((int)Math.ceil(cd/5d)*5-10) + " seconds!";
		if(cd > 10) return "Voting ends in " + (cd-10) + " seconds!";
		if(cd == 10) return "Voting ended!";
        return "until game start!";
    }

	public static BukkitTask warmupRunnable;
	public static void warmup() {

		if(Config.warmupWorldBorder)
			for(Player p : Bukkit.getOnlinePlayers()) WorldBorders.sendWorldBorder(p, p.getLocation().getX(), p.getLocation().getZ(), Config.warmupWorldBorderDiameter, 0);

		warmupRunnable = new BukkitRunnable() {
			int counter = -1;
			Sound s = null;
			@Override public void run() {

				counter++;

				if(Config.warmupWorldBorder) {

					if(counter >= Config.timeWarmup -5 && counter < Config.timeWarmup) {
						s = Sound.SUCCESSFUL_HIT;
						Messenger.sendActionBars(Bukkit.getOnlinePlayers(), ChatColor.GREEN + "Game starting in " + ChatColor.RED + (Config.timeWarmup - counter));
					} else if (counter == Config.timeWarmup) {
						WorldBorders.broadcastRemoveWorldBorders();
						Messenger.sendActionBars(Bukkit.getOnlinePlayers(), ChatColor.GREEN + "Game started!");
						s = Sound.GLASS;
						startBars();
					}

				} else {

					if (counter >= Config.timeWatch - 5 && counter < Config.timeWatch) {
						s = Sound.SUCCESSFUL_HIT;
						Messenger.sendActionBars(Bukkit.getOnlinePlayers(), ChatColor.GREEN + "Game starting in " + ChatColor.RED + (Config.timeWatch - counter));
					} else if (counter == Config.timeWatch) {
						Gamemode.unregisterEvent(Warmup.class);
						s = Sound.GLASS;
						Messenger.sendActionBars(Bukkit.getOnlinePlayers(), ChatColor.GREEN + "Game started! " + (Config.disablePvp ? "" : ChatColor.RED + "PVP enabling in " + (Config.timeWarmup - counter)));
						if(!Config.disablePvp) Messenger.broadcast(ChatColor.RED + "PvP enabling in " + ChatColor.DARK_RED + (Config.timeWarmup - counter));
						startBars();
						if(Gamemode.singleton.customManager instanceof IWarmupManager) ((IWarmupManager) Gamemode.singleton.customManager).endWatch();
					} else if (counter >= Config.timeWarmup - 5 && counter < Config.timeWarmup && !Config.disablePvp) {
						s = Sound.SUCCESSFUL_HIT;
						Messenger.sendActionBars(Bukkit.getOnlinePlayers(), ChatColor.RED + "PVP enabling in " + ChatColor.DARK_RED + (Config.timeWarmup - counter));
					} else if (counter == Config.timeWarmup && !Config.disablePvp) {
						Bukkit.getWorld("Game").setPVP(true);
						Messenger.sendActionBars(Bukkit.getOnlinePlayers(), ChatColor.RED + "PVP enabled!");
						s = Sound.GLASS;
						if(Gamemode.singleton.customManager instanceof IWarmupManager) ((IWarmupManager) Gamemode.singleton.customManager).endWarmup();
					} else s = null;

				}

				if(s != null)
					for(Player p : Bukkit.getOnlinePlayers()) {
						p.playSound(p.getLocation(), s, 1, 1);
					}

				if(counter == Config.timeWarmup && counter != Config.timeWatch) {
					Location l = Config.getLocation("Spawns.Spectator." + Gamemode.currentMap, null);
					WorldBorders.setWorldBorder(l.getX(), l.getZ(), Config.wbDiameterStart, 0, 0, 0);
					shrinkBorder();
					cancel();
				}
			}
		}.runTaskTimer(Gamemode.plugin, 0L, 20L);
	}

	public static void shrinkBorder() {
		if(!Config.wbShrinking) return;

		new BukkitRunnable() {
			@Override public void run() {
				Location l = Config.getLocation("Spawns.Spectator." + Gamemode.currentMap, null);
				WorldBorders.setWorldBorder(l.getX(), l.getZ(), Config.wbDiameterEnd, Config.wbShrinkDuration, 20, 20);
			}
		}.runTaskLater(Gamemode.plugin, Config.wbShrinkDelay * 20L);
	}

	public static void startBars() {
		new BukkitRunnable() {
			int countdown = 4;
			final String fight = Config.warmupEndTitle;
			@Override public void run() {
				if(countdown >= 0) {
					String spaces = new String(new char[countdown * 2]).replace("\0", " ");
					String title = fight.replace(" ", spaces);
					Messenger.sendTitleBars(Bukkit.getOnlinePlayers(), ChatColor.DARK_RED + title, ChatColor.BLUE + "May the best player win", 0, 20, 5);
				} else if (countdown >= fight.replace(" ", "").length() * -1) {
					StringBuilder title = new StringBuilder(fight.replace(" ", ""));
					title.insert(Math.abs(countdown), ChatColor.RED);
					title.insert(Math.abs(countdown) - 1, ChatColor.GOLD);
					Messenger.sendTitleBars(Bukkit.getOnlinePlayers(), ChatColor.DARK_RED + title.toString(), ChatColor.BLUE + "May the best player win", 0, 20, 5);
				} else cancel();
				countdown--;
			}
		}.runTaskTimerAsynchronously(Gamemode.plugin, 0L, 2L);
	}
	
	public static void endGame() {

		// Podium locations: Player spawn, NPC Stat 1, NPC Stat 2, NPC Stat 3, NPC Winner
		List<Location> podiumLocs = Config.getLocationList("Spawns.Podium", Bukkit.getWorld("Podium"));

		// Get players of statistics
		OfflinePlayer[] players = {
				Bukkit.getOfflinePlayer(StatManager.getHighestStat(Config.statsDisplayedPodium[0])),
				Bukkit.getOfflinePlayer(StatManager.getHighestStat(Config.statsDisplayedPodium[1])),
				Bukkit.getOfflinePlayer(StatManager.getHighestStat(Config.statsDisplayedPodium[2]))
		};

		// Get winners and calculate winner location
		List<Player> winners = Config.overrideWinners == null ? PlayerManager.teamsAlive.get(0).getPlayers() : Arrays.asList(Config.overrideWinners);
		Location winnerLoc = podiumLocs.get(4).subtract( winners.size() - 1, 0, 0);

		// Construct NPCs: Create list, populate with stats, populate with winners
		NPC[] npcs = new NPC[3 + winners.size()];
		for(int i = 0; i < 3; i++) npcs[i] = new NPC(podiumLocs.get(i + 1), players[i].getName());
		for(int i = 0; i < winners.size(); i++) npcs[i + 3] = new NPC(winnerLoc.add(2, 0, 0), winners.get(i).getDisplayName());

		// Construct armour stands
		PacketPlayOutSpawnEntityLiving[] packetSpawnStands = new PacketPlayOutSpawnEntityLiving[3];
		for(int i = 0; i < 3; i++) {
			EntityArmorStand nmsStand = new EntityArmorStand(((CraftWorld) Bukkit.getWorld("Podium")).getHandle());
			Location l = podiumLocs.get(i + 1);
			nmsStand.setLocation(l.getX(), l.getY() -2, l.getZ() -2, 0, 0);
			nmsStand.setGravity(false);
			nmsStand.setInvisible(true);
			ChatColor color = i == 0 ? ChatColor.GOLD : i == 1 ? ChatColor.WHITE : ChatColor.RED;
			nmsStand.setCustomName(color + Config.statsDisplayedPodium[i].title + ChatColor.GRAY + " (" + color + StatManager.getStat(players[i].getUniqueId(), Config.statsDisplayedPodium[i]) + ChatColor.GRAY + ")");
			nmsStand.setCustomNameVisible(true);
			packetSpawnStands[i] = new PacketPlayOutSpawnEntityLiving(nmsStand);
		}

		// Teleport, change gamemode, send npcs and armour stands
		for(Player p : Bukkit.getOnlinePlayers()) {
			p.teleport(podiumLocs.get(0));
			p.setGameMode(GameMode.ADVENTURE);

			for (NPC npc : npcs)
				npc.send(p);

			for(PacketPlayOutSpawnEntityLiving packetSpawnStand : packetSpawnStands)
				((CraftPlayer) p).getHandle().playerConnection.sendPacket(packetSpawnStand);
		}

		// Fireworks and countdown to restart server
		new BukkitRunnable() {
			FireworkEffect fwEffect = FireworkEffect.builder()
					.trail(true)
					.withColor(Color.YELLOW)
					.with(FireworkEffect.Type.STAR)
					.build();

			int counter = 0;
			@Override
			public void run() {
				Messenger.broadcastActionBars(ChatColor.RED + "Server restarting in " + ChatColor.DARK_RED + (15 - counter) + ChatColor.RED + "...");

				// Spawn 2 fireworks every 3 seconds
				if(counter % 3 == 0) {
					for(int i = 0; i < 2; i++) {
						Firework firework = (Firework) podiumLocs.get(5 + i).getWorld().spawnEntity(podiumLocs.get(5 + i), EntityType.FIREWORK);
						FireworkMeta fireworkMeta = firework.getFireworkMeta();
						fireworkMeta.addEffect(fwEffect);
						fireworkMeta.setPower(1);
						firework.setFireworkMeta(fireworkMeta);
					}
				}

				// Actually restart
				if(counter >= 15) {
					PhaseManager.restart();
					cancel();
					return;
				}

				counter++;
			}
		}.runTaskTimer(Gamemode.plugin, 100L, 20L);

	}
	
}