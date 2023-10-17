package de.lolsu.game.commands;

import de.lolsu.game.main.Config;
import de.lolsu.game.main.Gamemode;
import de.lolsu.game.managers.GamestateManager;
import de.lolsu.game.misc.GameRule;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Admin implements CommandExecutor {

    protected static String exceptionMessage = ChatColor.RED +
            "Something went wrong!\n" + ChatColor.GREEN +
            "Available commands are:\n" + ChatColor.RESET + "- '" + ChatColor.GOLD +
            "/gmsetup create {World Name}" + ChatColor.RESET + "' - Creates an empty World\n- '" + ChatColor.GOLD +
            "/gmsetup tp {Optional: World Name}" + ChatColor.RESET + "' - Teleports you between the Lobby/Game World\n- '" + ChatColor.GOLD +
            "/gmsetup getmaps" + ChatColor.RESET + "' - Returns a list of all in-game maps\n- '" + ChatColor.GOLD +
            "/gmsetup loadmap {Map Name}" + ChatColor.RESET + "' - Loads a map into the Game World\n- '" + ChatColor.GOLD +
            "/gmsetup savemap [Confirm]" + ChatColor.RESET + "' - Saves the currently loaded map\n- '" + ChatColor.GOLD +
            "/gmsetup savenew {Map Name}" + ChatColor.RESET + "' - Saves the world you're currently in as a map\n- '" + ChatColor.GOLD +
            "/gmsetup cleanmaps [Confirm]" + ChatColor.RESET + "' - Removes all irregularities from the maps\n- '" + ChatColor.GOLD +
            "/gmsetup start" + ChatColor.RESET + "' - Quick-starts the game\n- '" + ChatColor.GOLD +
            "/gmsetup stop" + ChatColor.RESET + "' - Stops the game start\n- '" + ChatColor.GOLD +
            "/gmsetup setspawn [Lobby/Game/Spectator/Podium] {Optional: Index}" + ChatColor.RESET + "' - Sets a spawn for a team or player\n- '" + ChatColor.GOLD +
            "/gmsetup rmspawn [Game/Podium] {Index}" + ChatColor.RESET + "' - Removes a spawn previously set for a team or player\n- '" + ChatColor.GOLD +
            "/gmsetup visspawns [Lobby/Game/Spectator/Podium]" + ChatColor.RESET + "' - Visualizes every spawn with a local Armor Stand and the spawns index\n- '" + ChatColor.GOLD +
            "/gmsetup moditem {Display Name} {Lore...}" + ChatColor.RESET + "' - Sets name and lore of the held item with mc formatting codes (&)" + ChatColor.GOLD +
            "/gmsetup remmobs" + ChatColor.RESET + "' - Removes all mobs except for armour stands, ... from the world you're currently in";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.isOp()) { sender.sendMessage(ChatColor.DARK_RED + "Access denied."); return false; }

        try {

            // Main commands
            switch (args[0]) {

                case "create": {
                        WorldCreator wc = new WorldCreator(args[1]);
                        wc.type(WorldType.FLAT);
                        wc.generatorSettings("2;0;1;");
                        World w = wc.createWorld();
                        w.getBlockAt(0, 95, 0).setType(Material.SEA_LANTERN);
                        w.setDifficulty(Difficulty.PEACEFUL);
                        for(Map.Entry<GameRule, String> e : Config.mapGameRules.entrySet()) w.setGameRuleValue(e.getKey().name, e.getValue());
                        w.setTime(6000);
                        sender.sendMessage(ChatColor.GOLD + "Created World!");
                    break; }

                case "tp":
                    if(!Files.exists(Paths.get(args[1]))) { sender.sendMessage(ChatColor.RED + "World doesn't exist"); break; }
                    ((Player) sender).teleport(new Location(new WorldCreator(args[1]).createWorld(), 0.5, 196, 0.5));
                    sender.sendMessage(ChatColor.GOLD + "Teleported to World!");
                    break;

                case "getmaps":
                    sender.sendMessage(ChatColor.GOLD + "Here are the Maps:");
                    sender.sendMessage(Config.getMaps());
                    break;

                case "loadmap":
                    Config.loadMap(args[1]);
                    ((Player) sender).teleport(new Location(Bukkit.getWorld("Game"), 0.5, 196, 0.5));
                    sender.sendMessage(ChatColor.GOLD + "Loaded Map!");
                    break;

                case "savemap":
                    if (!args[1].equalsIgnoreCase("confirm")) {
                        sender.sendMessage(ChatColor.RED + "This command is highly dangerous and could cause major loss of data, proceed with caution by entering 'confirm' as the second argument");
                        break;
                    }
                    Config.saveMap();
                    sender.sendMessage(ChatColor.GOLD + "Saved Map!");
                    break;

                case "savenew":
                    Config.saveNew(((Player) sender).getWorld(), (Player) sender, args[1]);
                    sender.sendMessage(ChatColor.GOLD + "Saved world to game maps as " + ChatColor.AQUA + args[1]);
                    break;

                case "cleanmaps":
                    if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
                        sender.sendMessage(ChatColor.RED + "This command is highly dangerous and could cause major loss of data, proceed with caution by entering 'confirm' as a second argument");
                        break;
                    }
                    sender.sendMessage(ChatColor.GOLD + "Cleaning Maps...");
                    Config.cleanMaps();
                    sender.sendMessage(ChatColor.GOLD + "Cleaned Maps!");
                    break;

                case "start":
                    GamestateManager.startGame();
                    GamestateManager.countdown = 20;
                    sender.sendMessage(ChatColor.GOLD + "Quick-starting Game!");
                    break;

                case "stop":
                    if (GamestateManager.runnable != null) GamestateManager.runnable.cancel();
                    sender.sendMessage(ChatColor.GOLD + "Stopped Game-start!");
                    break;

                case "setspawn": {
                    Player p = (Player) sender;
                    Location loc = Config.smoothLocation(p.getLocation());

                    boolean singleSpawn = args[1].equalsIgnoreCase("Lobby") || args[1].equalsIgnoreCase("Spectator");
                    boolean saveMap = args[1].equalsIgnoreCase("Game") || args[1].equalsIgnoreCase("Spectator");
                    String path = "Spawns." + args[1] + (saveMap ? "." + Gamemode.currentMap : "");
                    if(!singleSpawn) {
                        List<Location> spawnLocs = Config.getLocationList(path);
                        spawnLocs.add(loc);
                        Config.saveLocationList(path, spawnLocs);
                        sender.sendMessage(ChatColor.GOLD + "Saved spawn to list");
                    } else {
                        Config.saveLocation(path, loc);
                        sender.sendMessage(ChatColor.GOLD + "Saved spawn");
                    }
                }
                    break;

                case "rmspawn": {
                    boolean saveMap = args[1].equalsIgnoreCase("Game") || args[1].equalsIgnoreCase("Spectator");
                    String path = "Spawns." + args[1] + (saveMap ? "." + Gamemode.currentMap : "");
                    List<Location> spawnLocs = Config.getLocationList(path);
                    spawnLocs.remove(Integer.parseInt(args[2]));
                    Config.saveLocationList(path, spawnLocs);
                    sender.sendMessage(ChatColor.GOLD + "Removed spawn, if spawns are visualized please rejoin the server and enter the command again to update the indexes");
                }
                    break;

                case "visspawns": {
                    boolean singleSpawn = args[1].equalsIgnoreCase("Lobby") || args[1].equalsIgnoreCase("Spectator");
                    boolean saveMap = args[1].equalsIgnoreCase("Game") || args[1].equalsIgnoreCase("Spectator");
                    String path = "Spawns." + args[1] + (saveMap ? "." + Gamemode.currentMap : "");
                    List<Location> spawnLocs = new ArrayList<>();
                    if(singleSpawn)
                        spawnLocs.add(Config.getLocation(path, ((Player) sender).getWorld()));
                    else spawnLocs = Config.getLocationList(path);

                    for (int i = 0; i < spawnLocs.size(); i++) {
                        WorldServer world = ((CraftWorld) Bukkit.getWorld("Game")).getHandle();
                        EntityArmorStand stand = new EntityArmorStand(world);
                        Location l = spawnLocs.get(i);
                        stand.setLocation(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
                        stand.setCustomName("Spawn Number " + i);
                        stand.setCustomNameVisible(true);
                        stand.setGravity(false);
                        stand.setArms(true);
                        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(stand);
                        PacketPlayOutEntityEquipment packetEquipment = new PacketPlayOutEntityEquipment(stand.getId(), 0, CraftItemStack.asNMSCopy(new ItemStack(Material.DIAMOND_SWORD)));
                        ((CraftPlayer) sender).getHandle().playerConnection.sendPacket(packet);
                        ((CraftPlayer) sender).getHandle().playerConnection.sendPacket(packetEquipment);
                        sender.sendMessage(ChatColor.GRAY + "Visualized spawn " + i);
                    }
                    sender.sendMessage(ChatColor.GOLD + "Visualized all spawns " + ChatColor.GRAY + "(" + spawnLocs.size() + ")");
                }
                    break;

                case "moditem":
                    ItemStack it = ((Player) sender).getItemInHand();
                    ItemMeta im = it.getItemMeta();
                    im.setDisplayName(String.join(" ", args).replace("moditem ", "").replace("&", "§"));
                    List<String> il = new ArrayList<>();
                    for(int i = 2; i < args.length; i++) il.add(args[i]);
                    im.setLore(il);
                    it.setItemMeta(im);
                    sender.sendMessage(ChatColor.GOLD + "Updated item name and lore");
                    break;

                case "remmobs":
                    for(Entity e : ((Player) sender).getWorld().getEntities())
                        if(e instanceof LivingEntity && !(e instanceof Player)) { e.remove(); sender.sendMessage(ChatColor.GRAY + "Removed " + e.getName()); }
                    break;

                default:
                    sender.sendMessage(exceptionMessage);
                    break;
            }

        } catch (Exception ex) {
            sender.sendMessage(ex.getMessage());
            sender.sendMessage(exceptionMessage);
        } return false;
    }

}
