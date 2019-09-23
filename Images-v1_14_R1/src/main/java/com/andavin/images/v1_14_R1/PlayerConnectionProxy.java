package com.andavin.images.v1_14_R1;

import net.minecraft.server.v1_14_R1.PacketPlayInUseEntity;
import net.minecraft.server.v1_14_R1.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;

import static com.andavin.reflect.Reflection.findField;
import static com.andavin.reflect.Reflection.getFieldValue;

/**
 * @since September 21, 2019
 * @author Andavin
 */
class PlayerConnectionProxy extends PlayerConnection {

    private static final Field ENTITY_ID = findField(PacketPlayInUseEntity.class, "a");
    private final BiConsumer<Player, Integer> listener;

    PlayerConnectionProxy(PlayerConnection connection, BiConsumer<Player, Integer> listener) {
        super(((CraftServer) Bukkit.getServer()).getServer(), connection.networkManager, connection.player);
        this.listener = listener;
    }

    @Override
    public void a(PacketPlayInUseEntity packet) {
        this.listener.accept(this.player.getBukkitEntity(), getFieldValue(ENTITY_ID, packet));
        super.a(packet);
    }
}
