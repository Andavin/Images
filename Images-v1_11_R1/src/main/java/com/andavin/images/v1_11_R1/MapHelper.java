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

import net.minecraft.server.v1_11_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
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
    private static final DataWatcherObject<Integer> ROTATION =
            getFieldValue(EntityItemFrame.class, null, "d");
    private static final Map<UUID, AtomicInteger> MAP_IDS = new HashMap<>(4);

    @Override
    protected MapView getWorldMap(int id) {
        return Bukkit.getMap((short) id);
    }

    @Override
    protected int nextMapId(org.bukkit.World world) {
        return MAP_IDS.computeIfAbsent(world.getUID(), __ ->
                new AtomicInteger(DEFAULT_STARTING_ID)).getAndIncrement();
    }

    @Override
    protected void createMap(int frameId, int mapId, Player player, Location location, BlockFace direction, int rotation, byte[] pixels) {

        ItemStack item = new ItemStack(Items.FILLED_MAP);
        item.setData(mapId);

        EntityItemFrame frame = new EntityItemFrame(((CraftWorld) player.getWorld()).getHandle());
        frame.setSilent(true); // Don't send the sound packet
        frame.setItem(item); // Must set this first to avoid updating surrounding blocks
        frame.setLocation(location.getX(), location.getY(), location.getZ(), 0, 0);
        frame.setDirection(CraftBlock.blockFaceToNotch(direction));
        setFieldValue(ENTITY_ID, frame, frameId);
        if (rotation != 0) {
            frame.getDataWatcher().set(ROTATION, rotation);
        }

        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutSpawnEntity(frame, 71,
                frame.direction.get2DRotationValue(), frame.getBlockPosition()));
        connection.sendPacket(new PacketPlayOutEntityMetadata(frame.getId(), frame.getDataWatcher(), true));
        connection.sendPacket(new PacketPlayOutMap(mapId, (byte) 3, false, emptyList(), pixels, 0, 0, 128, 128));
    }

    @Override
    protected void destroyMap(Player player, int[] frameIds) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(frameIds));
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
