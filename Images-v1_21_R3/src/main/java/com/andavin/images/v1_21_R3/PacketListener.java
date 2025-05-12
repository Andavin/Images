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
package com.andavin.images.v1_21_R3;

import com.andavin.images.image.CustomImageSection;
import com.andavin.reflect.FieldMatcher;
import com.andavin.reflect.MethodMatcher;
import com.andavin.util.Scheduler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket.Handler;
import net.minecraft.network.protocol.game.ServerboundPickItemFromEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.andavin.reflect.Reflection.*;

/**
 * @since March 19, 2023
 * @author Andavin
 */
class PacketListener extends com.andavin.images.PacketListener<ServerboundInteractPacket, ServerboundPickItemFromEntityPacket> {

    private static final Field ENTITY_ID = findField(ServerboundInteractPacket.class, new FieldMatcher(int.class));
    private static final Field CONNECTION = findField(ServerCommonPacketListenerImpl.class, new FieldMatcher(Connection.class));
    private static final Method TRY_PICK_ITEM = findMethod(ServerGamePacketListenerImpl.class,
            new MethodMatcher(void.class, ItemStack.class));

    @Override
    protected void setEntityListener(Player player, ImageListener listener) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        Connection internal = getFieldValue(CONNECTION, connection);
        internal.channel.pipeline().addBefore("packet_handler", "image_handler",
                new PlayerConnectionProxy(connection, listener, this));
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
    protected void handle(Player player, ServerboundPickItemFromEntityPacket packet) {

        CustomImageSection section = getImageSectionByEntityId(packet.id());
        if (section == null) {
            return;
        }

        MapId mapId = new MapId(section.getMapId());
        ItemStack item = new ItemStack(Items.FILLED_MAP);
        item.set(DataComponents.MAP_ID, mapId);
        Scheduler.sync(() -> {

            ServerLevel world = ((CraftPlayer) player).getHandle().serverLevel();
            MapItemSavedData map = MapItem.getSavedData(mapId, world);
            if (map == null) {
                ItemStack newItem = MapItem.create(world, 0, 0, (byte) 3, false, false);
                MapId newMapId = newItem.get(DataComponents.MAP_ID);
                item.set(DataComponents.MAP_ID, newMapId); // Transfer the ID
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

            tryPickItem(((CraftPlayer) player).getHandle().connection, item);
        });
    }

    private static void tryPickItem(ServerGamePacketListenerImpl connection, ItemStack item) { // Access to the private method
        invokeMethod(TRY_PICK_ITEM, connection, item);
    }
}
