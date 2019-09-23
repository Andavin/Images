package com.andavin.images.v1_8_R3;

import com.andavin.images.PacketListener;
import com.andavin.images.PacketListener.Hand;
import com.andavin.images.PacketListener.ImageListener;
import com.andavin.images.PacketListener.InteractType;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity.EnumEntityUseAction;
import net.minecraft.server.v1_8_R3.PlayerConnection;

import java.lang.reflect.Field;

import static com.andavin.reflect.Reflection.findField;
import static com.andavin.reflect.Reflection.getFieldValue;

/**
 * @since September 21, 2019
 * @author Andavin
 */
class PlayerConnectionProxy extends PlayerConnection {

    private static final Field ENTITY_ID = findField(PacketPlayInUseEntity.class, "a");
    private final ImageListener listener;

    PlayerConnectionProxy(PlayerConnection connection, ImageListener listener) {
        super(MinecraftServer.getServer(), connection.networkManager, connection.player);
        this.listener = listener;
    }

    @Override
    public void a(PacketPlayInUseEntity packet) {
        PacketListener.call(this.player.getBukkitEntity(), getFieldValue(ENTITY_ID, packet),
                packet.a() == EnumEntityUseAction.ATTACK ? InteractType.LEFT_CLICK : InteractType.RIGHT_CLICK,
                Hand.MAIN_HAND, this.listener);
        super.a(packet);
    }
}
