/* Copyright (c) 2019 */
package com.andavin.util;

import org.bukkit.entity.Player;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class TimeoutMetadata extends LazyMetadataValue {

    private long expiry;
    private static Plugin instance;

    /**
     * Create a new timeout and specify how long
     * it will be (in milliseconds) before it expires.
     *
     * @param timeout The time before this metadata will expire.
     */
    public TimeoutMetadata(long timeout) {
        super(instance);
        this.expiry = System.currentTimeMillis() + timeout;
    }

    /**
     * Create a new timeout and specify how long
     * it will be before it expires.
     *
     * @param units The amount of the given units of time
     *         before this metadata will expire.
     * @param unit The {@link TimeUnit} to multiply by the units.
     */
    public TimeoutMetadata(long units, TimeUnit unit) {
        super(instance);
        this.expiry = System.currentTimeMillis() + unit.toMillis(units);
    }

    /**
     * Tell if this metadata has timed out
     * or expired.
     *
     * @return If the metadata has expired.
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > this.expiry;
    }

    /**
     * Get the time when this metadata will expire;
     *
     * @return The expiry time.
     */
    public long getExpiry() {
        return this.expiry;
    }

    @Override
    public synchronized void invalidate() {
        this.expiry = System.currentTimeMillis();
    }

    @Override
    public Object value() {
        return this.expiry;
    }

    /**
     * Tell if the {@link TimeoutMetadata meta} under the given tag
     * is currently expired. If the meta does not exist on the player,
     * then it will be counted as expired and return {@code true}.
     *
     * @param player The player to test the metadata on.
     * @param meta The metadata that the timeout meta is on.
     * @return If the meta does not exist or is expired.
     */
    public static boolean isExpired(Player player, String meta) {

        for (MetadataValue value : player.getMetadata(meta)) {

            if (value instanceof TimeoutMetadata) {
                return ((TimeoutMetadata) value).isExpired();
            }
        }

        return true;
    }
}
