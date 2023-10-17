package de.lolsu.game.netty;

import de.lolsu.game.events.custom.PlayerInteractNPCEvent;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.List;

public class PacketReader {

    Player player;
    Channel channel;

    public PacketReader(Player p) {
        player = p;
        inject();
    }

    public void inject() {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        channel = craftPlayer.getHandle().playerConnection.networkManager.channel;
        channel.pipeline().addAfter("decoder", "PacketInjector", new MessageToMessageDecoder<Packet<?>>() {
            @Override
            protected void decode(ChannelHandlerContext context, Packet<?> packet, List<Object> args) {
                args.add(packet);
                readPacket(packet);
            }
        });
    }

    public void uninject() {
        if(channel.pipeline().get("PacketInjector") != null)
            channel.pipeline().remove("PacketInjector");
    }

    public void readPacket(Packet<?> packet) {
        if(packet instanceof PacketPlayInUseEntity) {
            int id = (Integer) getValue(packet, "a");
            PacketPlayInUseEntity.EnumEntityUseAction action = ((PacketPlayInUseEntity) packet).a();

            PlayerInteractNPCEvent event = new PlayerInteractNPCEvent(player, id, action);
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    public Object getValue(Object obj, String name) {
        try {
            Field f = obj.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(obj);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

}