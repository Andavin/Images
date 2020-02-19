/* Copyright (c) 2019 */
package com.andavin.images.legacy.image;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.NumberConversions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created on February 08, 2018
 *
 * @author Andavin
 */
@SerializableAs("com.andavin.images.image.ImageSection")
public final class LegacyImageSection implements ConfigurationSerializable {

    private final short id;
    private final int x, y;

    LegacyImageSection(short id, int x, int y) {
        this.x = x;
        this.y = y;
        this.id = id;
    }

    public LegacyImageSection(Map<String, Object> map) {
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
     * the beginning of the {@link LegacyImage} it belongs to.
     *
     * @return The X coordinate of this section.
     */
    public int getX() {
        return this.x;
    }

    /**
     * Get the Y coordinate of this section relative to
     * the beginning of the {@link LegacyImage} it belongs to.
     *
     * @return The Y coordinate of this section.
     */
    public int getY() {
        return this.y;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>(3);
        map.put("id", this.id);
        map.put("x", this.x);
        map.put("y", this.y);
        return map;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof LegacyImageSection)) {
            return false;
        }

        LegacyImageSection section = (LegacyImageSection) o;
        return this.id == section.id && this.x == section.x && this.y == section.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.x, this.y);
    }
}
