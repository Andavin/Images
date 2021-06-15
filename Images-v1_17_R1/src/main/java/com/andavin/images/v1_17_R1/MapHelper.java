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

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData.MapPatch;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapView;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.andavin.reflect.Reflection.*;
import static java.util.Collections.emptyList;

/**
 * @since September 20, 2019
 * @author Andavin
 */
class MapHelper extends com.andavin.images.MapHelper {

    static final int DEFAULT_STARTING_ID = 8000;
    private static final Field ENTITY_ID = findField(Entity.class, "id");
    private static final EntityDataAccessor<Integer> ROTATION =
            getFieldValue(ItemFrame.class, null, "g");
    private static final Map<UUID, AtomicInteger> MAP_IDS = new HashMap<>(4);

    @Override
    protected MapView getWorldMap(int id) {
        return Bukkit.getMap(id);
    }

    @Override
    protected int nextMapId(org.bukkit.World world) {
        return MAP_IDS.computeIfAbsent(world.getUID(), __ ->
                new AtomicInteger(DEFAULT_STARTING_ID)).getAndIncrement();
    }

    @Override
    protected void createMap(int frameId, int mapId, Player player, Location location,
                             BlockFace direction, int rotation, byte[] pixels) {

        ItemStack item = new ItemStack(Items.FILLED_MAP);
        item.getOrCreateTag().putInt("map", mapId);

        ItemFrame frame = new ItemFrame(((CraftWorld) player.getWorld()).getHandle(),
                new BlockPos(location.getX(), location.getY(), location.getZ()),
                CraftBlock.blockFaceToNotch(direction));
        frame.setItem(item, false, false);
        frame.setInvisible(invisible);
        setFieldValue(ENTITY_ID, frame, frameId);
        if (rotation != 0) {
            frame.getEntityData().set(ROTATION, rotation);
        }

        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(new ClientboundAddEntityPacket(frame, EntityType.ITEM_FRAME,
                frame.getDirection().get3DDataValue(), frame.getOnPos()));
        connection.send(new ClientboundSetEntityDataPacket(frame.getId(), frame.getEntityData(), true));
        connection.send(new ClientboundMapItemDataPacket(mapId, (byte) 3, false,
                emptyList(), new MapPatch(0, 0, 128, 128, pixels)));
    }

    @Override
    protected void destroyMap(Player player, int[] frameIds) {

        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        for (int frameId : frameIds) {
            connection.send(new ClientboundRemoveEntityPacket(frameId));
        }
    }

    @Override
    protected byte[] createPixels(BufferedImage image) {

        int pixelCount = image.getWidth() * image.getHeight();
        int[] pixels = new int[pixelCount];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        byte[] colors = new byte[pixelCount];
        for (int i = 0; i < pixelCount; i++) {
            colors[i] = MapPalette.matchColor(new Color(pixels[i], true));
        }

        return colors;
    }
}
