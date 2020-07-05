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
package com.andavin.images.image;

import com.andavin.images.MapHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.emptySet;

/**
 * @since September 20, 2019
 * @author Andavin
 */
public class CustomImage implements Serializable {

    private static final long serialVersionUID = 152608317193555652L;

    public static final UUID UNKNOWN_CREATOR = new UUID(
            -7650016060676093479L, -6847602434101430799L);
    private static final int PIXELS_PER_FRAME = 128;

    private transient int id = -1;
    private transient Location location;

    private final UUID creator;
    private final String imageName;
    private final BlockFace direction;
    private final Map<Integer, CustomImageSection> sections = new HashMap<>();

    public CustomImage(String imageName, Location location, BlockFace direction, BufferedImage image) {
        this(UNKNOWN_CREATOR, imageName, location, direction, image);
    }

    public CustomImage(UUID creator, String imageName, Location location,
                       BlockFace direction, BufferedImage image) {
        this.imageName = imageName;
        this.direction = direction;
        this.location = location;
        this.creator = creator;
        this.update(image);
    }

    /**
     * Get the ID of this image that is used for
     * a database.
     *
     * @return The database ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Set the ID of this image that is used for
     * the database.
     *
     * @param id The ID to set to.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get the unique ID of the player who created
     * this image.
     *
     * @return The image creator or {@link #UNKNOWN_CREATOR} if
     *         there is no creator.
     */
    public UUID getCreator() {
        return creator;
    }

    /**
     * Get the name of the image file.
     *
     * @return The image name.
     */
    public String getImageName() {
        return imageName;
    }

    /**
     * Get the direction in which this image is facing.
     *
     * @return The direction.
     */
    public BlockFace getDirection() {
        return direction;
    }

    /**
     * Get the {@link CustomImageSection} that has the
     * entity item frame ID in this image.
     *
     * @param frameId The frame ID to get the section for.
     * @return The section or {@code null} if the
     *         section is not found.
     */
    public CustomImageSection getSection(int frameId) {
        return this.sections.get(frameId);
    }

    /**
     * Get the {@link CustomImageSection} that has the
     * map ID for this image.
     *
     * @param mapId The map ID to get the section for.
     * @return The section or {@code null} if the
     *         section is not found.
     */
    public CustomImageSection getSectionByMap(int mapId) {

        for (CustomImageSection section : this.sections.values()) {

            if (section.getMapId() == mapId) {
                return section;
            }
        }

        return null;
    }

    /**
     * Show or hide this image for a player if they are within
     * 64 blocks or outside of 128 blocks of any of its sections.
     *
     * @param player The player to show the image to.
     * @param location The location of the player to measure the distance from.
     */
    public void refresh(Player player, Location location) {

        for (CustomImageSection section : this.sections.values()) {

            boolean sameWorld = location != null && section.getLocation().getWorld().equals(location.getWorld());
            if (sameWorld) {

                double distance = section.getLocation().distanceSquared(location);
                if (distance <= 64 * 64) {
                    section.show(player);
                } else if (distance > 128 * 128) {
                    section.hide(player);
                }
            } else {
                section.hide(player);
            }
        }
    }

    /**
     * Remove a player from this image so that it can be
     * refreshed for them in the future.
     *
     * @param player The player to remove from this image.
     * @param online If the player is online and the images
     *               should be hidden from them.
     */
    public void remove(Player player, boolean online) {

        for (CustomImageSection section : this.sections.values()) {

            if (online) {
                section.hide(player);
            } else {
                section.shown.remove(player.getUniqueId());
            }
        }
    }

    /**
     * Destroy all of the sections of this image and hide it
     * from all players that it has been shown to if they are
     * currently online.
     *
     * @return The players that the image was hidden from.
     */
    public Set<Player> destroy() {

        int sections = this.sections.size();
        if (sections != 0) {

            int index = 0;
            int[] ids = new int[sections];
            Set<Player> players = new HashSet<>(sections);
            for (CustomImageSection section : this.sections.values()) {
                ids[index++] = section.getFrameId();
                section.shown.stream().map(Bukkit::getPlayer)
                        .filter(Objects::nonNull).forEach(players::add);
            }

            for (Player player : players) {
                MapHelper.destroyMaps(player, ids);
            }

            this.sections.clear();
            return players;
        }

        return emptySet();
    }

    /**
     * Update this image to the new given {@link BufferedImage}.
     *
     * @param image The image to update to.
     */
    public void update(BufferedImage image) {

        Set<Player> players = this.destroy();
        BlockFace face;
        int rotation;
        switch (this.direction) {
            case UP:
            case DOWN:
                face = this.direction;
                rotation = 0;
                break;
            case NORTH:
                face = BlockFace.SOUTH;
                rotation = 0;
                break;
            case SOUTH:
                face = BlockFace.NORTH;
                rotation = 0;
                break;
            case EAST:
                face = BlockFace.WEST;
                rotation = 0;
                break;
            case WEST:
                face = BlockFace.EAST;
                rotation = 0;
                break;
            default:
                throw new IllegalStateException("Invalid direction " + this.direction);
        }

        int xSections = Math.max(image.getWidth() / PIXELS_PER_FRAME, 1);
        int ySections = Math.max(image.getHeight() / PIXELS_PER_FRAME, 1);
        image = resize(image, xSections, ySections);
        for (int x = 0; x < xSections; x++) {

            for (int y = 0; y < ySections; y++) {

                Location loc = location.clone();
                switch (face) {
                    case UP:
                        loc.add(x, 0, y);
                        break;
                    case DOWN:
                        loc.add(x, 0, -y);
                        break;
                    case SOUTH:
                        loc.add(-x, -y, 0);
                        break;
                    case NORTH:
                        loc.add(x, -y, 0);
                        break;
                    case WEST:
                        loc.add(0, -y, -x);
                        break;
                    case EAST:
                        loc.add(0, -y, x);
                        break;
                }

                CustomImageSection section = new CustomImageSection(loc, this.direction,
                        rotation, image.getSubimage(x * PIXELS_PER_FRAME, y * PIXELS_PER_FRAME,
                        PIXELS_PER_FRAME, PIXELS_PER_FRAME));
                this.sections.put(section.getFrameId(), section);
            }
        }

        for (Player player : players) {
            this.refresh(player, player.getLocation());
        }
    }

    /**
     * Tell if this image or any of its sections
     * are within the specified range of a {@link Location}.
     *
     * @param location The location to test within range of.
     * @param range The range to test with.
     * @return If the location is in range of this image.
     */
    public boolean isInRange(Location location, int range) {

        int rangeSqrd = range * range;
        if (this.location.distanceSquared(location) <= rangeSqrd) {
            return true;
        }

        for (CustomImageSection section : this.sections.values()) {

            if (section.getLocation().distanceSquared(location) <= rangeSqrd) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof CustomImage)) {
            return false;
        }

        CustomImage image = (CustomImage) obj;
        return this.direction == image.direction &&
                this.imageName.equals(image.imageName) &&
                this.creator.equals(image.creator) &&
                this.location.equals(image.location);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        writeLocation(out, this.location);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {

        in.defaultReadObject();
        this.location = readLocation(in);
        CustomImageSection[] sections = this.sections.values().toArray(new CustomImageSection[0]);
        this.sections.clear();
        for (CustomImageSection section : sections) {
            this.sections.put(section.getFrameId(), section);
        }
    }

    static void writeLocation(ObjectOutputStream out, Location location) throws IOException {
        out.writeObject(location.getWorld().getName());
        out.writeInt(location.getBlockX());
        out.writeInt(location.getBlockY());
        out.writeInt(location.getBlockZ());
        out.writeFloat(location.getYaw());
        out.writeFloat(location.getPitch());
    }

    static Location readLocation(ObjectInputStream in) throws IOException, ClassNotFoundException {

        String name = (String) in.readObject();
        World world = Bukkit.getWorld(name);
        checkState(world != null, "unknown world with ID %s", name);
        return new Location(
                world,
                in.readInt(), in.readInt(), in.readInt(),
                in.readFloat(), in.readFloat()
        );
    }

    private static BufferedImage resize(BufferedImage image, int xSections, int ySections) {

        if (image.getWidth() % PIXELS_PER_FRAME == 0 && image.getHeight() % PIXELS_PER_FRAME == 0) {
            return image;
        }
        // Get a scaled version of the image
        Image img = image.getScaledInstance(xSections * PIXELS_PER_FRAME,
                ySections * PIXELS_PER_FRAME, Image.SCALE_DEFAULT);
        image = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        // Copy the image over to the new instance
        Graphics2D g2D = image.createGraphics();
        g2D.drawImage(img, 0, 0, null);
        g2D.dispose();
        return image;
    }
}
