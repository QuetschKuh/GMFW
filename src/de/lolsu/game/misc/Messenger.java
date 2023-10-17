package de.lolsu.game.misc;

import de.lolsu.game.main.Gamemode;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;

public class Messenger {

    public static final String chatPrefix = ChatColor.DARK_PURPLE + "lolsu" + ChatColor.GOLD + Gamemode.singleton.shortName + ChatColor.GRAY + ChatColor.BOLD + " | " + ChatColor.RESET;

    public static void broadcast(String message) {
        Bukkit.broadcastMessage(chatPrefix + message);
    }

    public static void broadcastLines(String message) {
        broadcast(message.replace("\n", "\n" + chatPrefix));
    }

    public static void send(Player p, String message) {
        p.sendMessage(chatPrefix + message);
    }

    public static void sendLines(Player p, String message) {
        send(p, message.replace("\n", "\n" + chatPrefix));
    }

    public static void sendGroup(Collection<? extends Player> ps, String message) {
        for(Player p : ps) p.sendMessage(chatPrefix + message);
    }

    public static void sendTitleBar(Player p, String textTitle, String textMessage, int fadeIn, int fadeOut, int length) {
        IChatBaseComponent icbcTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + textTitle + "\"}");
        IChatBaseComponent icbcMessage = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + textMessage + "\"}");

        PacketPlayOutTitle ppotTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, icbcTitle);
        PacketPlayOutTitle ppotMessage = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, icbcMessage);
        PacketPlayOutTitle ppotLength = new PacketPlayOutTitle(fadeIn, length, fadeOut);

        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(ppotTitle);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(ppotMessage);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(ppotLength);
    }

    public static void sendTitleBars(Collection<? extends Player> ps, String textTitle, String textMessage, int fadeIn, int fadeOut, int length) {
        IChatBaseComponent icbcTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + textTitle + "\"}");
        IChatBaseComponent icbcMessage = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + textMessage + "\"}");

        PacketPlayOutTitle ppotTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, icbcTitle);
        PacketPlayOutTitle ppotMessage = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, icbcMessage);
        PacketPlayOutTitle ppotLength = new PacketPlayOutTitle(fadeIn, length, fadeOut);

        for(Player p : ps) {
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(ppotTitle);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(ppotMessage);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(ppotLength);
        }
    }

    public  static void broadcastTitleBar(String textTitle, String textMessage, int fadeIn, int fadeOut, int length) {
        sendTitleBars(Bukkit.getOnlinePlayers(), textTitle, textMessage, fadeIn, fadeOut, length);
    }

    public static void sendActionBar(Player p, String message) {
        IChatBaseComponent icbc = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + message + "\"}");
        PacketPlayOutChat ppoc = new PacketPlayOutChat(icbc, (byte) 2);

        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(ppoc);
    }

    public static void sendActionBars(Collection<? extends Player> ps, String message) {
        IChatBaseComponent icbc = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + message + "\"}");
        PacketPlayOutChat ppoc = new PacketPlayOutChat(icbc, (byte) 2);

        for(Player p : ps) ((CraftPlayer) p).getHandle().playerConnection.sendPacket(ppoc);
    }

    public  static void broadcastActionBars(String message) {
        sendActionBars(Bukkit.getOnlinePlayers() , message);
    }

}
