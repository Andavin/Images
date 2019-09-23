package com.andavin.images.v1_11_R1;

import net.minecraft.server.v1_11_R1.ChatComponentText;
import net.minecraft.server.v1_11_R1.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * @since September 23, 2019
 * @author Andavin
 */
class ActionBarUtil extends com.andavin.util.ActionBarUtil {

    @Override
    protected void sendMessage(Player player, String message) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(
                new PacketPlayOutChat(new ChatComponentText(message), (byte) 2));
    }
}
