package de.lolsu.game.misc;

import de.lolsu.game.main.Gamemode;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class BossBar extends BukkitRunnable {

    private String title;
    private float progress;

    private final HashMap<Player, EntityWither> witherMap = new HashMap<>();

    /**
     * Creates a new boss bar
     * @param title The title of the boss bar
     * @param progress The progress of the boss bar (from 0.0f to 1.0f)
     * @param players The players that should be added to it
     * */
    public BossBar(String title, float progress, Player... players) {
        this.title = title;
        this.progress = progress;
        runTaskTimer(Gamemode.plugin, 5L, 5L);   // Not sure why it needs to be synchronous but seems better this way
        for(Player p : players) addPlayer(p);
    }

    public void addPlayer(Player p) {
        // Assemble wither and spawn packet and send
        EntityWither wither = new EntityWither(((CraftWorld) p.getWorld()).getHandle());
        Location loc = p.getLocation().add(p.getLocation().getDirection().multiply(50));
        wither.setCustomName(title);
        wither.setHealth(progress * wither.getMaxHealth());
        wither.setInvisible(true);
        wither.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(wither);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        witherMap.put(p, wither);
    }

    public void addPlayers(Player... players) {
        for(Player p : players) addPlayer(p);
    }

    public void removePlayer(Player p) {
        if(!p.isOnline()) { witherMap.remove(p); return; }  // Make sure player hasn't disconnected

        EntityWither wither = witherMap.remove(p);
        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(wither.getId());
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

    public void modify(String newTitle, float newProgress) {
        title = newTitle;
        progress = newProgress;

        for(Map.Entry<Player, EntityWither> entry : witherMap.entrySet()) {
            Player p = entry.getKey();
            if(!p.isOnline()) { witherMap.remove(p); continue; }    // Make sure player hasn't disconnected

            EntityWither wither = entry.getValue();
            wither.setCustomName(title);
            wither.setHealth(progress * wither.getMaxHealth());
            PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(wither.getId(), wither.getDataWatcher(), true);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public void destroy() {
        cancel();
        for(Player p : witherMap.keySet()) removePlayer(p);
    }

    /**
     * Updates the location of the withers for continuous display of the boss bar
     * */
    @Override
    public void run() {
        for(Map.Entry<Player, EntityWither> entry : witherMap.entrySet()) {
            Player p = entry.getKey();
            if(!p.isOnline()) { witherMap.remove(p); continue; }    // Make sure player hasn't disconnected

            EntityWither wither = entry.getValue();
            Location loc = p.getLocation().add(p.getLocation().getDirection().multiply(50));
            wither.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
            PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport(wither);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }
    }
}
