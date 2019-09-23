package com.andavin.images.v1_8_R3;

import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import net.minecraft.server.v1_8_R3.PlayerConnection;
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
        super(MinecraftServer.getServer(), connection.networkManager, connection.player);
        this.listener = listener;
    }

    @Override
    public void a(PacketPlayInUseEntity packet) {
        this.listener.accept(this.player.getBukkitEntity(), getFieldValue(ENTITY_ID, packet));
        super.a(packet);
    }
}
