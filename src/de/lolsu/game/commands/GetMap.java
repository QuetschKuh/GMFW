package de.lolsu.game.commands;

import de.lolsu.game.main.Config;
import de.lolsu.game.main.Gamemode;
import de.lolsu.game.managers.GamestateManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GetMap implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) { sender.sendMessage(ChatColor.RED + "Command may only be executed by a player"); return false; }

        if(GamestateManager.gamestate == GamestateManager.Gamestate.Midgame) sender.sendMessage(ChatColor.AQUA + "This is " + ChatColor.GOLD + Config.fancy(Gamemode.currentMap));
        else sender.sendMessage(ChatColor.AQUA + "This is " + ChatColor.GOLD + Config.fancy(((Player) sender).getWorld().getName()));

        return false;
    }
}
