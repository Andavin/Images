/*
 * MIT License
 *
 * Copyright (c) 2020 Mark
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
