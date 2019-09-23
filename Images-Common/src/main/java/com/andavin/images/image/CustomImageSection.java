package com.andavin.images.image;

import com.andavin.images.MapHelper;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.andavin.images.image.CustomImage.readLocation;
import static com.andavin.images.image.CustomImage.writeLocation;

/**
 * @since September 20, 2019
 * @author Andavin
 */
public class CustomImageSection implements Serializable {

    private static final AtomicInteger ID_COUNTER = new AtomicInteger(Integer.MAX_VALUE / 4);

    private final int x, y;
    private final byte[] pixels;
    private final BlockFace direction;

    transient Set<UUID> shown = new HashSet<>();
    private transient Location location;
    private transient int frameId, mapId; // Should not be

    CustomImageSection(int x, int y, Location location, BlockFace direction, BufferedImage image) {
        this.frameId = ID_COUNTER.getAndIncrement();
        this.mapId = MapHelper.getNextMapId(location.getWorld());
        this.x = x;
        this.y = y;
        this.location = location;
        this.direction = direction;
        this.pixels = MapHelper.getPixels(image);
    }

    public int getFrameId() {
        return frameId;
    }

    public int getMapId() {
        return mapId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Location getLocation() {
        return location;
    }

    public BlockFace getDirection() {
        return direction;
    }

    public void show(Player player) {

        if (this.shown.add(player.getUniqueId())) {
            MapHelper.createMap(player, this.frameId, this.mapId, this.location, this.direction, this.pixels);
        }
    }

    public void hide(Player player) {

        if (this.shown.remove(player.getUniqueId())) {
            MapHelper.destroyMaps(player, this.frameId);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        writeLocation(out, this.location);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.location = readLocation(in);
        this.frameId = ID_COUNTER.getAndIncrement();
        this.mapId = MapHelper.getNextMapId(location.getWorld());
    }
}
