package com.andavin.images;

import com.andavin.images.PacketListener.ImageListener;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.plugin.Plugin;

/**
 * @since December 07, 2019
 * @author Andavin
 */
class ProtocolLibListener<T, U> extends PacketAdapter {

    private final ImageListener listener;
    private final PacketListener packetListener;

    ProtocolLibListener(Plugin plugin, ImageListener listener, PacketListener packetListener) {
        super(plugin, Client.USE_ENTITY, Client.SET_CREATIVE_SLOT);
        this.listener = listener;
        this.packetListener = packetListener;
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {

        PacketType type = event.getPacketType();
        if (type == Client.USE_ENTITY) {
            packetListener.handle(event.getPlayer(), listener,
                    event.getPacket().getHandle());
        } else if (type == Client.SET_CREATIVE_SLOT) {
            packetListener.handle(event.getPlayer(),
                    event.getPacket().getHandle());
        }
    }
}
