package de.lolsu.game.events.midgame;

import de.lolsu.game.main.Config;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class Soup implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if(!(
                (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                e.getItem() != null && e.getItem().getType() == Material.MUSHROOM_SOUP &&
                p.getHealth() != p.getMaxHealth() &&
                !(e.getClickedBlock() != null && e.getClickedBlock().getState() instanceof InventoryHolder)
        )) return;

        p.setHealth(Math.min(p.getHealth() + Config.soupHeals, p.getMaxHealth()));
        p.setItemInHand(null);
    }

    @EventHandler
    public void onPlayerEat(PlayerItemConsumeEvent e) {
        if(e.getItem().getType() == Material.MUSHROOM_SOUP) e.setCancelled(true);
    }

}
