package com.andavin.images.v1_8_R3;

import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

/**
 * @since September 21, 2019
 * @author Andavin
 */
class PacketListener extends com.andavin.images.PacketListener {

    @Override
    public void addEntityListener(Player player, BiConsumer<Player, Integer> listener) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.networkManager.a(new PlayerConnectionProxy(connection, listener));
    }
}
