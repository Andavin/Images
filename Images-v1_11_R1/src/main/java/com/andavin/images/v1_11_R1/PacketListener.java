/* Copyright (c) 2019 */
package com.andavin.images.v1_11_R1;

import com.andavin.images.image.CustomImageSection;
import com.andavin.util.Logger;
import com.andavin.util.Scheduler;
import net.minecraft.server.v1_11_R1.*;
import net.minecraft.server.v1_11_R1.PacketPlayInUseEntity.EnumEntityUseAction;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.andavin.images.v1_11_R1.MapHelper.DEFAULT_STARTING_ID;
import static com.andavin.reflect.Reflection.findField;
import static com.andavin.reflect.Reflection.getFieldValue;

/**
 * @since September 21, 2019
 * @author Andavin
 */
class PacketListener extends com.andavin.images.PacketListener<PacketPlayInUseEntity, PacketPlayInSetCreativeSlot> {

    private static final Field ENTITY_ID = findField(PacketPlayInUseEntity.class, "a");

    @Override
    protected void setEntityListener(Player player, ImageListener listener) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.networkManager.setPacketListener(new PlayerConnectionProxy(connection, listener, this));
    }

    @Override
    protected void handle(Player player, ImageListener listener, PacketPlayInUseEntity packet) {
        call(player, getFieldValue(ENTITY_ID, packet),
                packet.a() == EnumEntityUseAction.ATTACK ? InteractType.LEFT_CLICK : InteractType.RIGHT_CLICK,
                packet.b() == EnumHand.MAIN_HAND ? Hand.MAIN_HAND : Hand.OFF_HAND, listener);
    }

    @Override
    protected void handle(Player player, PacketPlayInSetCreativeSlot packet) {

        ItemStack item = packet.getItemStack();
        if (item == null) {
            return;
        }

        int mapId = item.getData();
        if (mapId >= DEFAULT_STARTING_ID) {

            CustomImageSection section = getImageSection(mapId);
            if (section != null) {

                AtomicBoolean complete = new AtomicBoolean();
                Scheduler.sync(() -> {

                    WorldMap map = ((ItemWorldMap) item.getItem()).getSavedMap(item,
                            ((CraftPlayer) player).getHandle().getWorld()); // Sets a new ID
                    map.scale = 3;
                    map.track = false;
                    map.unlimitedTracking = true;
                    map.colors = section.getPixels();
                    complete.set(true);
                    synchronized (complete) {
                        complete.notify();
                    }
                });

                synchronized (complete) {

                    while (!complete.get()) {

                        try {
                            complete.wait();
                        } catch (InterruptedException e) {
                            Logger.severe(e);
                        }
                    }
                }
            }
        }
    }
}
