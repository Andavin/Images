/* Copyright (c) 2019 */
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

    private static final long serialVersionUID = 572225588300845874L;

    /**
     * The starting ID of the item frames.
     */
    public static final int DEFAULT_STARTING_ID = Integer.MAX_VALUE / 4;
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(DEFAULT_STARTING_ID);

    private final byte[] pixels;
    private final BlockFace direction;
    private final int rotation;

    private transient Location location;
    private transient int frameId, mapId; // Should not be
    transient Set<UUID> shown = new HashSet<>();

    CustomImageSection(Location location, BlockFace direction, int rotation, BufferedImage image) {
        this.rotation = rotation;
        this.frameId = ID_COUNTER.getAndIncrement();
        this.mapId = MapHelper.getNextMapId(location.getWorld());
        this.location = location;
        this.direction = direction;
        this.pixels = MapHelper.getPixels(image);
    }

    /**
     * Get the ID of the item frame that this section
     * of the image is located in.
     *
     * @return The frame ID.
     */
    public int getFrameId() {
        return frameId;
    }

    /**
     * Get the ID of the map that is located within the
     * the item frame.
     *
     * @return The map ID.
     */
    public int getMapId() {
        return mapId;
    }

    /**
     * Get the {@link Location} of this item frame section.
     *
     * @return The frame location.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Get the direction that this image section is facing.
     *
     * @return The direction.
     */
    public BlockFace getDirection() {
        return direction;
    }

    /**
     * Get the amount of rotation that this section is within
     * it's own space (0-8).
     *
     * @return The rotation.
     */
    public int getRotation() {
        return rotation;
    }

    /**
     * Get a copy of the pixel color array for
     * this image section.
     *
     * @return The pixels.
     */
    public byte[] getPixels() {
        return pixels.clone();
    }

    /**
     * Show this image section to the given player.
     *
     * @param player The player to show to.
     */
    public void show(Player player) {

        if (this.shown.add(player.getUniqueId())) {
            MapHelper.createMap(player, this.frameId, this.mapId, this.location, this.direction, this.rotation, this.pixels);
        }
    }

    /**
     * Hide this image section from the given player.
     *
     * @param player The player to hide from.
     */
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
        this.shown = new HashSet<>();
    }
}
