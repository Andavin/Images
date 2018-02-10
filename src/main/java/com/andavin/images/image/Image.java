package com.andavin.images.image;

import com.andavin.images.Images;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.ItemFrame;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created on February 08, 2018
 *
 * @author Andavin
 */
@SuppressWarnings("unchecked")
public final class Image implements ConfigurationSerializable {

    private final File imageFile;
    private final Location location;
    private final Map<Location, ImageSection> sections;

    public Image(final Location location, final File imageFile) {
        this.location = location;
        this.imageFile = imageFile;
        this.sections = new HashMap<>();
    }

    public Image(final Map<String, Object> map) {
        this.location = (Location) map.get("location");
        this.sections = new HashMap((Map<Location, ImageSection>) map.get("sections"));
        this.imageFile = new File(Images.getInstance().getDataFolder(), (String) map.get("file"));
    }

    /**
     * Get the start location of this image.
     *
     * @return The location.
     */
    public Location getLocation() {
        return this.location.clone();
    }

    /**
     * Get the file that stores the actual image
     * for this object.
     *
     * @return The image file.
     */
    public File getImageFile() {
        return this.imageFile;
    }

    /**
     * Add a new section to this image with the given ID and at
     * relative coordinates and the location given.
     *
     * @param id The ID of the new section and its map.
     * @param x The relative X coordinate.
     * @param y The relative Y coordinate.
     * @param loc The full location of the section.
     */
    public void addSection(final short id, final int x, final int y, final Location loc) {
        this.sections.put(loc, new ImageSection(id, x, y));
    }

    /**
     * Get all of the {@link ImageSection}s of this image.
     *
     * @return The sections of this image.
     */
    public Collection<ImageSection> getSections() {
        return this.sections.values();
    }

    /**
     * Destroy the physical blocks of this image.
     */
    public void destroy() {

        Bukkit.getScheduler().runTask(Images.getInstance(), () -> this.sections.forEach((loc, section) ->

                loc.getWorld().getNearbyEntities(loc, 1, 1, 1).forEach(entity -> {

                    if (entity instanceof ItemFrame) {
                        entity.remove();
                    }
                })
        ));
    }

    /**
     * Get a section of this image by it's world {@link Location}.
     *
     * @param loc The location of the section.
     * @return The {@link ImageSection} or null if none was found.
     */
    @Nullable
    public ImageSection getSection(final Location loc) {

        ImageSection section = this.sections.get(loc);
        if (section == null) {
            loc.setX(loc.getBlockX());
            loc.setY(loc.getBlockY());
            loc.setZ(loc.getBlockZ());
            loc.setYaw(0);
            loc.setPitch(0);
            section = this.sections.get(loc);
        }

        return section;
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> map = new HashMap<>(3);
        map.put("location", this.location);
        map.put("sections", this.sections);
        map.put("file", this.imageFile.getName());
        return map;
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof Image)) {
            return false;
        }

        final Image image = (Image) o;
        return Objects.equals(this.imageFile, image.imageFile) &&
               Objects.equals(this.location, image.location) &&
               Objects.equals(this.sections, image.sections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.imageFile, this.location, this.sections);
    }
}
