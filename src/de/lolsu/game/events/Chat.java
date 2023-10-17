package de.lolsu.game.events;

import de.lolsu.game.main.Config;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.text.Normalizer;

public class Chat implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        e.setCancelled(true);
        String messageFiltered = Normalizer.normalize(e.getMessage().toLowerCase(), Normalizer.Form.NFD).replaceAll("[^a-z]", "");
        for(String s : Config.config.getStringList("Blacklist")) if(messageFiltered.contains(s)) { e.getPlayer().sendMessage(ChatColor.RED + "Whoops, looks like you slipped on your keyboard. Try again"); return; }
        String message = ChatColor.AQUA + e.getPlayer().getDisplayName() + ChatColor.GRAY + " » " + ChatColor.WHITE + e.getMessage();
        for(Player p : e.getRecipients()) p.sendMessage(message);
    }

}
