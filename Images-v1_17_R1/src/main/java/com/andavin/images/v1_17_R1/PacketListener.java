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
package com.andavin.images.v1_17_R1;

import com.andavin.images.image.CustomImageSection;
import com.andavin.util.Logger;
import com.andavin.util.Scheduler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket.Handler;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.andavin.reflect.Reflection.findField;
import static com.andavin.reflect.Reflection.getFieldValue;

/**
 * @since September 21, 2019
 * @author Andavin
 */
class PacketListener extends com.andavin.images.PacketListener<ServerboundInteractPacket, ServerboundSetCreativeModeSlotPacket> {

    private static final Field ENTITY_ID = findField(ServerboundInteractPacket.class, "a");

    @Override
    protected void setEntityListener(Player player, ImageListener listener) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        connection.connection.setListener(new PlayerConnectionProxy(connection, listener, this));
    }

    @Override
    protected void handle(Player player, ImageListener listener, ServerboundInteractPacket packet) {
        int entityId = getFieldValue(ENTITY_ID, packet);
        packet.dispatch(new Handler() {
            @Override
            public void onInteraction(InteractionHand hand) {
                call(player, entityId, InteractType.RIGHT_CLICK,
                        hand == InteractionHand.MAIN_HAND ? Hand.MAIN_HAND : Hand.OFF_HAND, listener);
            }

            @Override
            public void onInteraction(InteractionHand hand, Vec3 vec3) {
                call(player, entityId, InteractType.RIGHT_CLICK,
                        hand == InteractionHand.MAIN_HAND ? Hand.MAIN_HAND : Hand.OFF_HAND, listener);
            }

            @Override
            public void onAttack() {
                call(player, entityId, InteractType.LEFT_CLICK, Hand.MAIN_HAND, listener);
            }
        });
    }

    @Override
    protected void handle(Player player, ServerboundSetCreativeModeSlotPacket packet) {

        ItemStack item = packet.getItem();
        CompoundTag tag = item.getTag();
        if (tag != null) {

            int mapId = tag.getInt("map");
            if (mapId >= MapHelper.DEFAULT_STARTING_ID) {

                CustomImageSection section = getImageSection(mapId);
                if (section != null) {

                    AtomicBoolean complete = new AtomicBoolean();
                    Scheduler.sync(() -> {

                        try {
                            MapItemSavedData map = MapItem.getSavedData(item,
                                    ((CraftPlayer) player).getHandle().getLevel()); // Sets a new ID
                            map.locked = true;
                            map.scale = 3;
                            map.trackingPosition = false;
                            map.unlimitedTracking = true;
                            map.colors = section.getPixels();
                        } finally {

                            complete.set(true);
                            synchronized (complete) {
                                complete.notify();
                            }
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
}
