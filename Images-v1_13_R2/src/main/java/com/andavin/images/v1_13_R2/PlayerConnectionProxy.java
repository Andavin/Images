package com.andavin.images.v1_13_R2;

import com.andavin.images.PacketListener;
import com.andavin.images.PacketListener.Hand;
import com.andavin.images.PacketListener.ImageListener;
import com.andavin.images.PacketListener.InteractType;
import com.andavin.images.image.CustomImageSection;
import net.minecraft.server.v1_13_R2.*;
import net.minecraft.server.v1_13_R2.PacketPlayInUseEntity.EnumEntityUseAction;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;

import java.lang.reflect.Field;

import static com.andavin.images.v1_13_R2.MapHelper.DEFAULT_STARTING_ID;
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
        super(((CraftServer) Bukkit.getServer()).getServer(), connection.networkManager, connection.player);
        this.listener = listener;
    }

    @Override
    public void a(PacketPlayInUseEntity packet) {
        PacketListener.call(this.player.getBukkitEntity(), getFieldValue(ENTITY_ID, packet),
                packet.b() == EnumEntityUseAction.ATTACK ? InteractType.LEFT_CLICK : InteractType.RIGHT_CLICK,
                packet.c() == EnumHand.MAIN_HAND ? Hand.MAIN_HAND : Hand.OFF_HAND, this.listener);
        super.a(packet);
    }

    @Override
    public void a(PacketPlayInSetCreativeSlot packet) {

        ItemStack item = packet.getItemStack();
        NBTTagCompound tag = item.getTag();
        if (tag != null) {

            int mapId = tag.getInt("map");
            if (mapId >= DEFAULT_STARTING_ID) {

                CustomImageSection section = PacketListener.getImageSection(mapId);
                if (section != null) {
                    WorldMap map = ItemWorldMap.getSavedMap(item, player.getWorld()); // Sets a new ID
                    map.scale = (byte) 3;
                    map.track = false;
                    map.colors = section.getPixels();
                }
            }
        }

        super.a(packet);
    }
}
