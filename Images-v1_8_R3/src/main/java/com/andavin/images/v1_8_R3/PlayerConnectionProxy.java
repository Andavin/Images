package com.andavin.images.v1_8_R3;

import com.andavin.images.PacketListener.ImageListener;
import net.minecraft.server.v1_8_R3.*;

/**
 * @since September 21, 2019
 * @author Andavin
 */
class PlayerConnectionProxy extends PlayerConnection {

    private final ImageListener listener;
    private final PacketListener packetListener;

    PlayerConnectionProxy(PlayerConnection connection, ImageListener listener, PacketListener packetListener) {
        super(MinecraftServer.getServer(), connection.networkManager, connection.player);
        this.listener = listener;
        this.packetListener = packetListener;
    }

    @Override
    public void sendPacket(Packet packet) {
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
