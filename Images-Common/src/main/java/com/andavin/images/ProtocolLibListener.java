package com.andavin.images;

import com.andavin.images.PacketListener.ImageListener;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;

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

    /**
     * Register the protocol packet listener with ProtocolLib.
     * <p>
     * This is here only because it cannot be in the main plugin
     * class since the server initializes all references regardless
     * of if they are ever reached during runtime.
     *
     * @param plugin The plugin to register for.
     * @param listenerTasks The listener tasks.
     * @param bridge The bridge {@link PacketListener}
     */
    static void register(Plugin plugin, Map<UUID, ImageListener> listenerTasks, PacketListener bridge) {
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new ProtocolLibListener(plugin, (clicker, image, section, action, hand) -> {

                    ImageListener listener = listenerTasks.remove(clicker.getUniqueId());
                    if (listener != null) {
                        listener.click(clicker, image, section, action, hand);
                    }
                }, bridge));
    }
}
