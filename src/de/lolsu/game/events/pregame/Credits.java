package de.lolsu.game.events.pregame;

import de.lolsu.game.main.Config;
import de.lolsu.game.misc.ItemStacks;
import de.lolsu.game.misc.Messenger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Credits implements Listener {

    public List<String> credits = new ArrayList<>();

    Map<Player, Integer> creditsMap = new HashMap<>();
    Map<Player, Long> lastPlayerTime = new HashMap<>();

    public static ItemStack creditsItem;

    public Credits() {
        credits.add(ChatColor.GOLD + "" + ChatColor.BOLD + "The Credits");
        credits.add(ChatColor.GRAY + "Click again to continue the credits");
        credits.add(ChatColor.RED + "Programming by...");
        credits.add(ChatColor.GOLD + "Leo Harter (QuetschKuh)");
        credits.addAll(Config.config.getStringList("Credits"));
        credits.add(ChatColor.GOLD + "" + ChatColor.BOLD + "Credits over!");
        credits.add(ChatColor.GRAY + "Click again to restart the credits");

        // Credits Item Credits
        List<String> cic = new ArrayList<>(credits);
        cic.remove(0);
        cic.remove(0);
        cic.remove(cic.size() - 1);
        cic.remove(cic.size() - 1);
        String lastCC = "";
        for(int i = 0; i < cic.size(); i++) {
            if(i % 2 == 0) lastCC = cic.get(i).replace(ChatColor.stripColor(cic.get(i)), "");
            else cic.set(i, lastCC + ChatColor.stripColor(cic.get(i)));
        }
        for(int i = cic.size() - 2; i >= 2; i--) {
            if(i % 2 == 0)
                cic.add(i, "");
        }
        creditsItem = ItemStacks.create(Material.NAME_TAG, ChatColor.GOLD + "" + ChatColor.BOLD + "Credits", cic);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if(p.getItemInHand() == null || p.getItemInHand().getType() != Material.NAME_TAG) return;



        if((lastPlayerTime.containsKey(p) && lastPlayerTime.get(p) + 10000 < System.currentTimeMillis()) ||
                (creditsMap.containsKey(p) && creditsMap.get(p) * 2 + 1 >= credits.size())
                || !creditsMap.containsKey(p))
            creditsMap.put(p, 0);
        lastPlayerTime.put(p, System.currentTimeMillis());

        int index = creditsMap.get(p);

        Messenger.sendTitleBar(p, credits.get(index * 2), credits.get(index * 2 + 1), 5, 15, 80);
        Messenger.sendActionBar(p, ChatColor.GRAY + "Click again to continue the credits");

        creditsMap.put(p, index + 1);
    }

}
