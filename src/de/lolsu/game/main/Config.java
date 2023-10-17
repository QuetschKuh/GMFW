package de.lolsu.game.main;

import de.lolsu.game.managers.StatManager;
import de.lolsu.game.misc.GameRule;
import de.lolsu.game.misc.Messenger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * A simple class containing variables which control the gameplay<br>
 * All values default to either false or 0 unless explicitly indicated in the javadocs<br>
 * Strings usually shouldn't be changed but instructions to do so are in the javadocs as well.
 * */
public class Config {

    // Technical configuration stuff
    public static File configFile;
    public static FileConfiguration config;
    public static String mapDirectory = "maps/";

    /*
     * Hardcoded configurable variables
     */
    public static String startFileLocation = "start.sh";
    /** Closes the bungeecord messaging channel and stops automatically putting players in a team. */
    public static boolean disableExternalParties = true;
    /** Whether the gamemode has a custom reconnect system.<br>
     * By default, the player respawns as a spectator. */
    public static boolean customReconnectSystem = false;
    /** Whether the gamemode has a custom voting system.<br>
     * By default, it goes through the map directory and puts up 5 random maps listed up for vote. */
    public static boolean customVotingSystem = false;
    /** Stops spawning players in a team on the same spot and rather treats them like individual players. */
    public static boolean disableTeamSpawns = false;
    /** Whether the gamemode has a custom respawn system.<br>
     * By default, dead players just respawn as a spectator. */
    public static boolean customRespawnSystem = false;

    // Map stuff
    /** A whitelist for both breakable and placeable blocks incase general breaking and placing is disabled */
    public static List<Material> blocksBreakable = new ArrayList<>(), blocksPlaceable = new ArrayList<>();
    /** Whether the map should be breakable and/or whether blocks should be placeable */
    public static boolean mapBreakable = false, mapPlaceable = false;
    /** The gamemode of the player upon loading into the world<br>Defaults to: {@code GameMode.SURVIVAL} */
    public static GameMode playerGameMode = GameMode.SURVIVAL;
    /** The game rules for the map<br>
     * By default, DAYLIGHT_CYCLE false, MOB_SPAWN false, FIRE false, REDUCED_DEBUG_INFO true */
    public static Map<GameRule, String> mapGameRules = new HashMap<GameRule, String>() {{
        put(GameRule.DAYLIGHT_CYCLE, "false");
        put(GameRule.MOB_SPAWN, "false");
        put(GameRule.FIRE, "false");
        put(GameRule.REDUCED_DEBUG_INFO, "true");
    }};
    /** The weather that should be present on the map.<br>
     * Set to null to have the weather change freely<br>
     * Defaults to: {@code WeatherType.CLEAR} */
    public static WeatherType mapWeather = WeatherType.CLEAR;

    // Warmup and endgame
    /** The time until the players can move freely */
    public static int timeWatch = 10;
    /** The time until PvP is turned on, only relevant if {@code disablePvp = false} */
    public static int timeWarmup = 30;
    /** Whether during the timeWatch there should be a world border active or if the player movement should be completely disabled */
    public static boolean warmupWorldBorder = false;
    /** How big the world border during {@code timeWatch} should be, if there is one */
    public static double warmupWorldBorderDiameter = 20;
    /** Whether a world border should bring all players towards the center of the map after some time<br>
     * If enabled, worldBorderMinDiameter specifies how small it gets in worldBorderShrinkTime seconds after worldBorderStartTime seconds */
    public static boolean wbShrinking = true;
    public static double wbDiameterStart = 1000;
    public static int wbShrinkDelay = 600;
    public static int wbShrinkDuration = 180;
    public static double wbDiameterEnd = 50;
    /** The title that should be displayed when timeWatch is over<br>
     * Defaults to: {@code "F I G H T !"}*/
    public static String warmupEndTitle = "F I G H T !";

    // Game
    /** Whether pvp should be enabled during the game */
    public static boolean disablePvp = false;
    /** How much mushroom soup should heal the player after right-clicking with it.<br>
     * Set to 0 (default) or lower to disable soup as a method of healing */
    public static int soupHeals = 0;
    /** Whether the player needs their own lapislazuli for enchanting at the enchantment table or if it should be already placed in there*/
    public static boolean autoLapis = false;
    /** Whether a player death should drop all items in their inventory<br>
     * if false, all items matching the materials in {@code deathDropsWhitelist} will be dropped<br>! Defaults to TRUE*/
    public static boolean deathDropsEverything = true;
    /** A whitelist for items to drop on death in case {@code deathDropsEverything} is set to {@code false} */
    public static List<Material> deathDropsWhitelist = new ArrayList<>();

    /** The 3 stats that should be displayed in the lobby and during the game */
    public static StatManager.Statistic[] statsDisplayed = {StatManager.Statistic.KILLS, StatManager.Statistic.DEATHS, StatManager.Statistic.GIVEN};
    /** The three stats displayed for the podium in decreasing order, so index 0 is "gold", 1 is "silver", and 2 is "bronze"  */
    public static StatManager.Statistic[] statsDisplayedPodium = {StatManager.Statistic.KILLS, StatManager.Statistic.GIVEN, StatManager.Statistic.RECEIVED};
    public static Player[] overrideWinners = null;

    /** Loads the configuration file and creates one using the default {@code gm_config.yml} if necessary */
    public static void createConfig() {
        String configPath = Gamemode.plugin.getConfig().getString("config_path");
        if(configPath == null) {
            configPath = "gm_config.yml";
            Gamemode.plugin.getConfig().set("config_path", configPath);
            Gamemode.plugin.saveConfig();
        }
        configFile = new File(configPath);
        if(!configFile.exists() || configFile.length() == 0) {
            if(!configFile.getParentFile().exists()) configFile.getParentFile().mkdirs();
            try {
                InputStream in = Gamemode.plugin.getResource("gm_config.yml");
                Files.copy(in, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                in.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        mapDirectory = config.getString("mapdir");
    }

    /** Saves the configuration file */
    public static void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /** Modifies a given String to be nicer to look at<br>
     * Especially good to convert programming stuff into user readable stuff
     * @param string The string to be modified (e.g. 'my-VERY_UglyString')
     * @return The modified/fancier string (e.g. 'My Very Ugly String')
     * */
    public static String fancy(String string) {
        return
                WordUtils.capitalizeFully(string        // Capitalize first letters
                        .replace("-", " ")      // Replace - and _ with spaces
                        .replace("_", " ")
                        .replaceAll("(\\p{Ll})(\\p{Lu})", "$1 $2")) // Add a space between capitalized words if there is none
                ;
    }

    /** Modifies a given Location to be smoother<br>
     * Snaps x,z to middle of block, y to nearest .5, yaw to nearest 45 and sets pitch to 0
     * @param location The location that should be modified (e.g. x: 5.291, y: 2.41, z: -2.744, yaw: 98.512, pitch: 14.213)
     * @return The modified/smoother location (e.g. x: 5.5, y: 2.5, z: -2.5, yaw: 90, pitch: 0)
     * */
    public static Location smoothLocation(Location location) {
        World w =  location.getWorld();
        double x = Math.floor(location.getX()) + 0.5;
        double y = (double) Math.round(location.getY() * 2) / 2;
        double z = Math.floor(location.getZ()) + 0.5;
        float yaw = Math.round(location.getYaw() / 45) * 45;
        float pitch = 0;
        return new Location(w, x, y, z, yaw, pitch);
    }

    /** Saves a list of locations to the config without the world
     * @param path The path in the config it should be saved to
     * @param locs The list of locations that should be saved
     * */
    public static void saveLocationList(String path, List<Location> locs) {
        List<String> sList = new ArrayList<>();
        for(Location l : locs) sList.add(locationToString(l));
        config.set(path, sList);
        saveConfig();
    }

    /** Gets a list of locations from the config without the world
     * @param path The path in the config it should be gotten from
     * @return The list of locations from the {@code path}
     * */
    public static List<Location> getLocationList(String path) {
        List<String> sList = config.getStringList(path);
        List<Location> lList = new ArrayList<>();
        for(String s : sList) lList.add(stringToLocation(s));
        return lList;
    }

    /** Gets a list of locations from the config without the world and applies a given world to every entry
     * @param path The path in the config it should be gotten from
     * @return The list of locations from the {@code path}
     * */
    public static List<Location> getLocationList(String path, World w) {
        List<String> sList = config.getStringList(path);
        List<Location> lList = new ArrayList<>();
        for(String s : sList) lList.add(stringToLocation(s, w));
        return lList;
    }

    /** Saves a location to the config without the world
     * @param path The path in the config it should be saved to
     * @param loc The location that should be saved
     * */
    public static void saveLocation(String path, Location loc) {
        config.set(path, Config.locationToString(loc));
        saveConfig();
    }

    /** Gets a location without the world from the config and applies a given world to it
     * @param path The path in the config it should be gotten from
     * @param world The world that should be applied to the location
     * @return A location from the {@code path} with a given {@code world}
     * */
    public static Location getLocation(String path, World world) {
        Location l = Config.stringToLocation(config.getString(path));
        l.setWorld(world);
        return l;
    }

    /** Serializes a location to a String without the world
     * @param loc The location that should be serialized
     * @return The serialized location
     * */
    public static String locationToString(Location loc) {
        return loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + loc.getYaw() + ":" + loc.getPitch();
    }

    /** Deserializes a string to a location without the world
     * @param str The string that should be deserialized
     * @return The deserialized location
     * */
    public static Location stringToLocation(String str) {
        double[] l = Arrays.stream(str.split(":")).mapToDouble(Double::parseDouble).toArray();
        return new Location(null, l[0], l[1], l[2], (float) l[3], (float) l[4]);
    }

    /** Deserializes a string to a location without the world and applies the given world
     * @param str The string that should be deserialized
     * @return The deserialized location
     * */
    public static Location stringToLocation(String str, World w) {
        double[] l = Arrays.stream(str.split(":")).mapToDouble(Double::parseDouble).toArray();
        return new Location(w, l[0], l[1], l[2], (float) l[3], (float) l[4]);
    }

    /** Gets all directory names from the map directory given in the configuration
     * @return All map names
     * */
    public static String[] getMaps() {
        return new File(mapDirectory).list();
    }

    /** Loads a map from the map directory given in the configuration
     * @param name The file name of the map to load
     * */
    public static void loadMap(String name) throws IOException {
        Bukkit.unloadWorld("Game", false);
        if(Files.exists(Paths.get("Game"))) FileUtils.deleteDirectory(new File("Game"));
        FileUtils.copyDirectory(new File(mapDirectory + name), new File("Game"));
        new WorldCreator("Game").createWorld();
        Gamemode.currentMap = name;
    }

    /** Saves the current map to the map directory given in the configuration */
    public static void saveMap() throws IOException {
        String name = Gamemode.currentMap;
        if(Files.exists(Paths.get(mapDirectory + name))) FileUtils.deleteDirectory(new File(mapDirectory + name));
        Bukkit.unloadWorld("Game", true);
        FileUtils.copyDirectory(new File("Game"), new File(mapDirectory + name));
    }

    public static void saveNew(World world, Player player, String name) throws IOException {
        if(Files.exists(Paths.get(mapDirectory + name))) FileUtils.deleteDirectory(new File(mapDirectory + name));
        player.teleport(getLocation("Spawns.Lobby", Bukkit.getWorld("Lobby")));
        Bukkit.unloadWorld(world, true);
        FileUtils.copyDirectory(new File(world.getName()), new File(mapDirectory + name));
    }

    /** Goes through all maps in the map directory given in the configuration and:<br><ul>
     * <li>Goes through all chunks in a 2048x2048 square around 0:0 and deletes the items/monsters</li>
     * <li>Applies all game rules from the {@code mapGameRules} map</li>
     * <li>Sets the weather to {@code mapWeather}</li></ul>
     * */
    public static void cleanMaps() throws IOException {
        Messenger.broadcast(ChatColor.RED + "Going into map maintenance, may cause a lot of lag...");
        for(File f : new File(mapDirectory).listFiles()) {

            // Load map, set weather, set game rules
            // Iterate through all chunks in a 2048x2048 square around spawn, load, remove monsters and items, save and unload again
            loadMap(f.getName());
            World w = Bukkit.getWorld("Game");
            w.setThundering(mapWeather == WeatherType.DOWNFALL);
            for(Map.Entry<GameRule, String> e : mapGameRules.entrySet()) w.setGameRuleValue(e.getKey().name, e.getValue());
            for(int x = -63; x <= 63; x++) {
                for(int z = -63; z <= 63; z++) {
                    Chunk c = w.getChunkAt(x, z);
                    if(c == null) continue;
                    w.loadChunk(c);
                    for(Entity e : c.getEntities()) {
                        if(e instanceof Monster || e instanceof Item) e.remove();
                    }
                    w.unloadChunk(c);
                }
            }
            Bukkit.unloadWorld(w, true);

            // Remove all unnecessary directories and files to save space
            for(File sf : new File("Game").listFiles()) {
                if(sf.getName().equalsIgnoreCase("region") || sf.getName().contains("level.dat")) continue;
                if(sf.isDirectory()) FileUtils.deleteDirectory(sf);
                else sf.delete();
            }

            FileUtils.deleteDirectory(f);
            FileUtils.copyDirectory(new File("Game"), new File(mapDirectory + f.getName()));

            Messenger.broadcast(ChatColor.RED + "Maintenance done for: " + ChatColor.GOLD + Config.fancy(f.getName()));
        }
    }

    private enum Value {
        ALLOW_EXTERNAL(true),
        AMOUNT_SOUP_HEAL(0d),
        ;

        private boolean state;
        private double amount;
        private String literal;
        Value(boolean state) {
            this.state = state;
            this.amount = state ? 1.0d : 0.0d;
        }
        Value(double amount) {
            this.amount = amount;
            this.state = amount != 0;
        }
        Value(String literal) {
            this.literal = literal;
        }

        public boolean getState() {
            return state;
        }
        public float getAmountI() { return (int) amount; }
        public float getAmountF() { return (float) amount; }
        public double getAmountD() { return amount; }
        public String getLiteral() { return literal; }

    }

}
