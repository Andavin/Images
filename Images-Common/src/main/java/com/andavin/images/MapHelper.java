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
package com.andavin.images;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;

/**
 * @since September 20, 2019
 * @author Andavin
 */
public abstract class MapHelper implements Versioned {

    protected static boolean invisible = true;
    public static int showDistance = 64;
    public static int hideDistance = 128;
    private static final MapHelper BRIDGE = Versioned.getInstance(MapHelper.class);

    /**
     * Get the {@link MapView} for the given ID.
     *
     * @param id The ID of the map to get.
     * @return The map view.
     */
    public static MapView getMap(int id) {
        return BRIDGE.getWorldMap(id);
    }

    /**
     * Get the next available ID for maps in the given world.
     *
     * @param world The world to get the next ID for.
     * @return The next available ID.
     */
    public static int getNextMapId(World world) {
        return BRIDGE.nextMapId(world);
    }

    /**
     * Create and send a new map to the given player to be displayed
     * with the given information.
     *
     * @param player The player to display the map to.
     * @param frameId The ID of the item frame for the map.
     * @param mapId The ID of the map to display.
     * @param location The location to display the map at.
     * @param direction The direction to face the map.
     * @param rotation The rotation to put the map in the display at.
     * @param pixels The pixels to show on the map.
     */
    public static void createMap(Player player, int frameId, int mapId, Location location,
                                 BlockFace direction, int rotation, byte[] pixels) {
        BRIDGE.createMap(frameId, mapId, player, location, direction, rotation, pixels);
    }

    /**
     * Destroy all of the maps with the given IDs for
     * the item frames.
     *
     * @param player The player to destroy the maps for.
     * @param frameIds The IDs of the item frames to destroy.
     */
    public static void destroyMaps(Player player, int... frameIds) {
        BRIDGE.destroyMap(player, frameIds);
    }

    /**
     * Transition the given {@link BufferedImage} into
     * bytes that each represent a pixel color.
     *
     * @param image The image to get pixels for.
     * @return The pixels for the image.
     */
    public static byte[] getPixels(BufferedImage image) {
        return BRIDGE.createPixels(image);
    }

    /**
     * Get the {@link MapView} for the given ID.
     *
     * @param id The ID of the map to get.
     * @return The map view.
     */
    protected abstract MapView getWorldMap(int id);

    /**
     * Get the next available ID for maps in the given world.
     *
     * @param world The world to get the next ID for.
     * @return The next available ID.
     */
    protected abstract int nextMapId(World world);

    /**
     * Create and send a new map to the given player to be displayed
     * with the given information.
     *
     * @param frameId The ID of the item frame for the map.
     * @param mapId The ID of the map to display.
     * @param player The player to display the map to.
     * @param location The location to display the map at.
     * @param direction The direction to face the map.
     * @param rotation The rotation to put the map in the display at.
     * @param pixels The pixels to show on the map.
     */
    protected abstract void createMap(int frameId, int mapId, Player player, Location location,
                                      BlockFace direction, int rotation, byte[] pixels);

    /**
     * Destroy all of the maps with the given IDs for
     * the item frames.
     *
     * @param player The player to destroy the maps for.
     * @param frameIds The IDs of the item frames to destroy.
     */
    protected abstract void destroyMap(Player player, int[] frameIds);

    /**
     * Transition the given {@link BufferedImage} into
     * bytes that each represent a pixel color.
     *
     * @param image The image to get pixels for.
     * @return The pixels for the image.
     */
    protected abstract byte[] createPixels(BufferedImage image);
}
