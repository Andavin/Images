package com.andavin.images.v1_14_R1;

import com.andavin.images.PacketListener.ImageListener;
import net.minecraft.server.v1_14_R1.Packet;
import net.minecraft.server.v1_14_R1.PacketPlayInSetCreativeSlot;
import net.minecraft.server.v1_14_R1.PacketPlayInUseEntity;
import net.minecraft.server.v1_14_R1.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;

/**
 * @since September 21, 2019
 * @author Andavin
 */
class PlayerConnectionProxy extends PlayerConnection {

    private final ImageListener listener;
    private final PacketListener packetListener;

    PlayerConnectionProxy(PlayerConnection connection, ImageListener listener, PacketListener packetListener) {
        super(((CraftServer) Bukkit.getServer()).getServer(), connection.networkManager, connection.player);
        this.listener = listener;
        this.packetListener = packetListener;
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        super.sendPacket(packet);
    }

    @Override
    public void a(PacketPlayInUseEntity packet) {
        packetListener.handle(player.getBukkitEntity(), listener, packet);
        super.a(packet);
    }

    @Override
    public void a(PacketPlayInSetCreativeSlot packet) {
        packetListener.handle(player.getBukkitEntity(), packet);
        super.a(packet);
    }
}
