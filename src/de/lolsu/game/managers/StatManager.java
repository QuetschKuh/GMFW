package de.lolsu.game.managers;

import org.bukkit.entity.Player;

import java.util.*;

public class StatManager {

    static Map<UUID, Map<Statistic, Integer>> stats = new HashMap<>();

    public static void registerPlayer(Player p) {
        Map<Statistic, Integer> initialStats = new HashMap<>();
        for(Statistic s : Statistic.values()) initialStats.put(s, 0);
        stats.put(p.getUniqueId(), initialStats);
    }

    public static void removePlayer(Player p) {
        stats.remove(p.getUniqueId());
    }

    public static int getStat(UUID id, Statistic stat) {
        return stats.get(id).get(stat);
    }

    public static int getStat(Player p, Statistic stat) {
        return stats.get(p.getUniqueId()).get(stat);
    }

    public static void increaseStat(Player p, Statistic stat) {
        stats.get(p.getUniqueId()).replace(stat, getStat(p, stat) + 1);
    }

    public static void increaseStat(Player p, Statistic stat, int increase) {
        stats.get(p.getUniqueId()).replace(stat, getStat(p, stat) + increase);
    }

    public static void setStat(Player p, Statistic stat, int value) {
        stats.get(p.getUniqueId()).replace(stat, value);
    }

    public static UUID getHighestStat(Statistic stat) {
        List<UUID> maxUUIDs = new ArrayList<>();
        int maxValue = 0;
        for(Map.Entry<UUID, Map<Statistic, Integer>> entry : stats.entrySet()) {
            int currentValue = entry.getValue().get(stat);
            if(currentValue > maxValue) {
                maxUUIDs.clear();
                maxUUIDs.add(entry.getKey());
                maxValue = currentValue;
            } else if(currentValue == maxValue) {
                maxUUIDs.add(entry.getKey());
            }
        }
        return maxUUIDs.get(new Random().nextInt(maxUUIDs.size()));
    }

    public enum Statistic {
        KILLS("Kills", "Killed the most players"),
        DEATHS("Deaths", "Died the most often"),
        GIVEN("Dmg given", "Dealt the most damage"),
        RECEIVED("Dmg taken", "Received the most damage"),
        CHESTS("Chests", "Opened the most chests"),
        PLACES("Placed", "Placed the most blocks"),
        BREAKS("Broken", "Broke the most blocks"),
        CUSTOM_1("Custom 1", "This is a custom stat"),
        CUSTOM_2("Custom 2", "This is a custom stat"),
        CUSTOM_3("Custom 3", "This is a custom stat"),
        CUSTOM_4("Custom 4", "This is a custom stat"),
        CUSTOM_5("Custom 5", "This is a custom stat"),
        ;

        public String name;
        public String title;

        Statistic(String name, String title) {
            this.name = name;
            this.title = title;
        }

        public void rename(String name, String title) {
            this.name = name;
            this.title = title;
        }

    }

}
