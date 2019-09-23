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
     * @param pixels The pixels to show on the map.
     */
    public static void createMap(Player player, int frameId, int mapId, Location location,
                                 BlockFace direction, byte[] pixels) {
        BRIDGE.createMap(frameId, mapId, player, location, direction, pixels);
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
     * @param player The player to display the map to.
     * @param frameId The ID of the item frame for the map.
     * @param mapId The ID of the map to display.
     * @param location The location to display the map at.
     * @param direction The direction to face the map.
     * @param pixels The pixels to show on the map.
     */
    protected abstract void createMap(int frameId, int mapId, Player player, Location location,
                                      BlockFace direction, byte[] pixels);

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
