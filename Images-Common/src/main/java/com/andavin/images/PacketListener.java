package com.andavin.images;

import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

/**
 * @since September 21, 2019
 * @author Andavin
 */
public abstract class PacketListener implements Versioned {

    // TODO add more info about the packet here
    public abstract void addEntityListener(Player player, BiConsumer<Player, Integer> listener);
}
