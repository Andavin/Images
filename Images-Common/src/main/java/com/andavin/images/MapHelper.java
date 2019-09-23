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

    public static MapView getMap(int id) {
        return BRIDGE.getWorldMap(id);
    }

    public static int getNextMapId(World world) {
        return BRIDGE.nextMapId(world);
    }

    public static void createMap(Player player, int frameId, int mapId, Location location,
                                 BlockFace direction, byte[] pixels) {
        BRIDGE.createMap(frameId, mapId, player, location, direction, pixels);
    }

    public static void destroyMaps(Player player, int... frameIds) {
        BRIDGE.destroyMap(player, frameIds);
    }

    public static byte[] getPixels(BufferedImage image) {
        return BRIDGE.createPixels(image);
    }

    protected abstract MapView getWorldMap(int id);

    protected abstract int nextMapId(World world);

    protected abstract void createMap(int frameId, int mapId, Player player, Location location,
                                      BlockFace direction, byte[] pixels);

    protected abstract void destroyMap(Player player, int[] frameIds);

    protected abstract byte[] createPixels(BufferedImage image);
}
