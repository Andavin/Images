package com.andavin.images.v1_10_R1;

import com.andavin.images.PacketListener;
import com.andavin.images.PacketListener.Hand;
import com.andavin.images.PacketListener.ImageListener;
import com.andavin.images.PacketListener.InteractType;
import com.andavin.images.image.CustomImageSection;
import com.andavin.util.Logger;
import com.andavin.util.Scheduler;
import net.minecraft.server.v1_10_R1.*;
import net.minecraft.server.v1_10_R1.PacketPlayInUseEntity.EnumEntityUseAction;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.andavin.images.v1_10_R1.MapHelper.DEFAULT_STARTING_ID;
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
    public void sendPacket(Packet<?> packet) {
        super.sendPacket(packet);
    }

    @Override
    public void a(PacketPlayInUseEntity packet) {
        PacketListener.call(this.player.getBukkitEntity(), getFieldValue(ENTITY_ID, packet),
                packet.a() == EnumEntityUseAction.ATTACK ? InteractType.LEFT_CLICK : InteractType.RIGHT_CLICK,
                packet.b() == EnumHand.MAIN_HAND ? Hand.MAIN_HAND : Hand.OFF_HAND, this.listener);
        super.a(packet);
    }

    @Override
    public void a(PacketPlayInSetCreativeSlot packet) {

        ItemStack item = packet.getItemStack();
        if (item == null) {
            super.a(packet);
            return;
        }

        int mapId = item.getData();
        if (mapId >= DEFAULT_STARTING_ID) {

            CustomImageSection section = PacketListener.getImageSection(mapId);
            if (section != null) {

                AtomicBoolean complete = new AtomicBoolean();
                Scheduler.sync(() -> {

                    WorldMap map = ((ItemWorldMap) item.getItem())
                            .getSavedMap(item, player.getWorld()); // Sets a new ID
                    map.scale = 3;
                    map.track = false;
                    map.colors = section.getPixels();
                    complete.set(true);
                    synchronized (complete) {
                        complete.notify();
                    }
                });

                synchronized (complete) {

                    while (!complete.get()) {

                        try {
                            complete.wait();
                        } catch (InterruptedException e) {
                            Logger.severe(e);
                        }
                    }
                }
            }
        }

        super.a(packet);
    }
}
