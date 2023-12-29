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
package com.andavin.images.v1_20_R3;

import com.andavin.images.image.CustomImageSection;
import com.andavin.reflect.FieldMatcher;
import com.andavin.util.Logger;
import com.andavin.util.Scheduler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket.Handler;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.andavin.reflect.Reflection.findField;
import static com.andavin.reflect.Reflection.getFieldValue;

/**
 * @since March 19, 2023
 * @author Andavin
 */
class PacketListener extends com.andavin.images.PacketListener<ServerboundInteractPacket, ServerboundSetCreativeModeSlotPacket> {

    private static final Field ENTITY_ID = findField(ServerboundInteractPacket.class, new FieldMatcher(int.class));
    private static final Field CONNECTION = findField(ServerCommonPacketListenerImpl.class, new FieldMatcher(Connection.class));

    @Override
    protected void setEntityListener(Player player, ImageListener listener) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        Connection internal = getFieldValue(CONNECTION, connection);
        internal.setListener(new PlayerConnectionProxy(
                connection, internal, listener,
                this, new CommonListenerCookie(null, 0, null)
        ));
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

                            ServerLevel world = ((CraftPlayer) player).getHandle().serverLevel();
                            MapItemSavedData map = MapItem.getSavedData(mapId, world);
                            if (map == null) {
                                ItemStack newItem = MapItem.create(world, 0, 0, (byte) 3, false, false);
                                int newMapId = newItem.getOrCreateTag().getInt("map");
                                tag.putInt("map", newMapId); // Transfer the ID
                                map = MapItem.getSavedData(newMapId, world);
                            }

                            if (map != null) {
                                map.locked = true;
                                map.scale = 3;
                                map.trackingPosition = false;
                                map.unlimitedTracking = true;
                                map.colors = section.getPixels();
                            } else {
                                player.sendMessage("§cCannot create map. Unknown map data...");
                            }
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
