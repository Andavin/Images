package com.andavin.images.image;

import com.andavin.images.MapHelper;
import com.andavin.util.LocationUtil;
import com.andavin.util.Logger;
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
import java.util.List;
import java.util.*;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.emptySet;

/**
 * @since September 20, 2019
 * @author Andavin
 */
public class CustomImage implements Serializable {

    public static final UUID UNKNOWN_CREATOR = new UUID(
            -7650016060676093479L, -6847602434101430799L);
    private static final int PIXELS_PER_FRAME = 128;

    private transient Location location;
    private int id = -1;
    private final UUID creator;
    private final String imageName;
    private final BlockFace direction;
    private final List<CustomImageSection> sections = new ArrayList<>();

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UUID getCreator() {
        return creator;
    }

    public String getImageName() {
        return imageName;
    }

    public BlockFace getDirection() {
        return direction;
    }

    /**
     * Show or hide this image for a player if they are within
     * 64 blocks or outside of 128 blocks of any of its sections.
     *
     * @param player The player to show the image to.
     * @param location The location of the player to measure the distance from.
     */
    public void refresh(Player player, Location location) {

        Logger.info("refreshing");
        for (CustomImageSection section : this.sections) {

            boolean sameWorld = section.getLocation().getWorld().equals(location.getWorld());
            if (sameWorld) {

                double distance = section.getLocation().distanceSquared(location);
                if (distance <= 64 * 64) {
                    Logger.info("Showing");
                    section.show(player);
                } else if (distance > 128 * 128) {
                    Logger.info("hiding");
                    section.hide(player);
                }
            } else {
                Logger.info("hiding");
                section.hide(player);
            }
        }

        Logger.info("hmm");
    }

    /**
     * Hide this image from all players that it has been shown to
     * if they are currently online.
     *
     * @return The players that the image was hidden from.
     */
    public Set<Player> hide() {

        int sections = this.sections.size();
        if (sections != 0) {

            int[] ids = new int[sections];
            Set<Player> players = new HashSet<>(sections);
            for (int i = 0; i < sections; i++) {
                CustomImageSection section = this.sections.get(i);
                ids[i] = section.getFrameId();
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

        Set<Player> players = this.hide();
        BlockFace face;
        switch (this.direction) {
            case UP:
            case DOWN:
                face = LocationUtil.getCardinalDirection(location).getOppositeFace();
                break;
            case NORTH:
                face = BlockFace.SOUTH;
                break;
            case SOUTH:
                face = BlockFace.NORTH;
                break;
            case EAST:
                face = BlockFace.WEST;
                break;
            case WEST:
                face = BlockFace.EAST;
                break;
            default:
                throw new IllegalStateException("Invalid direction " + this.direction);
        }

        int xSections = image.getWidth() / PIXELS_PER_FRAME;
        int ySections = image.getHeight() / PIXELS_PER_FRAME;
        image = resize(image, xSections, ySections);
        for (int x = 0; x < xSections; x++) {

            for (int y = 0; y < ySections; y++) {

                Location loc = location.clone();
                switch (face) {
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

                this.sections.add(new CustomImageSection(x, y, loc, this.direction,
                        image.getSubimage(x * PIXELS_PER_FRAME, y * PIXELS_PER_FRAME,
                                PIXELS_PER_FRAME, PIXELS_PER_FRAME)));
            }
        }

        for (Player player : players) {

            for (CustomImageSection section : this.sections) {
                section.show(player);
            }
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        writeLocation(out, this.location);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.location = readLocation(in);
    }

    static void writeLocation(ObjectOutputStream out, Location location) throws IOException {
        out.writeObject(location.getWorld().getUID());
        out.writeInt(location.getBlockX());
        out.writeInt(location.getBlockY());
        out.writeInt(location.getBlockZ());
        out.writeFloat(location.getYaw());
        out.writeFloat(location.getPitch());
    }

    static Location readLocation(ObjectInputStream in) throws IOException, ClassNotFoundException {

        UUID id = (UUID) in.readObject();
        World world = Bukkit.getWorld(id);
        checkState(world != null, "unknown world with ID %s", id);
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
        Image img = image.getScaledInstance(xSections * PIXELS_PER_FRAME, ySections * PIXELS_PER_FRAME, 1);
        image = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        // Copy the image over to the new instance
        Graphics2D g2D = image.createGraphics();
        g2D.drawImage(img, 0, 0, null);
        g2D.dispose();
        return image;
    }
}
