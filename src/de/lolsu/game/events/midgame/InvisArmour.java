package de.lolsu.game.events.midgame;

import de.lolsu.game.main.Gamemode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InvisArmour implements Listener {

    Map<UUID, ItemStack[]> playerArmourMap = new HashMap<>();

    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent e) {
        if(e.getItem().getType() != Material.POTION) return;

        Potion p = Potion.fromItemStack(e.getItem());
        if(p.getType() != PotionType.INVISIBILITY) return;
        for(PotionEffect pe : p.getEffects()) if(pe.getType() == PotionEffectType.INVISIBILITY) return;

        playerArmourMap.put(e.getPlayer().getUniqueId(), e.getPlayer().getInventory().getArmorContents());
        e.getPlayer().getInventory().setArmorContents(null);

        new BukkitRunnable() {
            @Override public void run() {
                if(playerArmourMap.containsKey(e.getPlayer().getUniqueId()))
                    e.getPlayer().getInventory().setArmorContents(playerArmourMap.get(e.getPlayer().getUniqueId()));
            }
        }.runTaskLater(Gamemode.plugin, p.getEffects().iterator().next().getDuration());
    }

}
