package com.andavin.images.image;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.NumberConversions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created on February 08, 2018
 *
 * @author Andavin
 */
public final class ImageSection implements ConfigurationSerializable {

    private final short id;
    private final int x, y;

    ImageSection(final short id, final int x, final int y) {
        this.x = x;
        this.y = y;
        this.id = id;
    }

    public ImageSection(final Map<String, Object> map) {
        this.id = NumberConversions.toShort(map.get("id"));
        this.x = NumberConversions.toInt(map.get("x"));
        this.y = NumberConversions.toInt(map.get("y"));
    }

    /**
     * Get the ID of this image section and the
     * map that it is being loaded on.
     *
     * @return The ID of the section.
     */
    public short getId() {
        return this.id;
    }

    /**
     * Get the X coordinate of this section relative to
     * the beginning of the {@link Image} it belongs to.
     *
     * @return The X coordinate of this section.
     */
    public int getX() {
        return this.x;
    }

    /**
     * Get the Y coordinate of this section relative to
     * the beginning of the {@link Image} it belongs to.
     *
     * @return The Y coordinate of this section.
     */
    public int getY() {
        return this.y;
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> map = new HashMap<>(3);
        map.put("id", this.id);
        map.put("x", this.x);
        map.put("y", this.y);
        return map;
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof ImageSection)) {
            return false;
        }

        final ImageSection section = (ImageSection) o;
        return this.id == section.id && this.x == section.x && this.y == section.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.x, this.y);
    }
}
