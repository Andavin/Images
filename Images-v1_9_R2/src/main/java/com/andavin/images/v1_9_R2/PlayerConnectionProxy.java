/*
 * MIT License
 *
 * Copyright (c) 2020 Mark
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.andavin.images.v1_9_R2;

import com.andavin.images.PacketListener.ImageListener;
import net.minecraft.server.v1_9_R2.Packet;
import net.minecraft.server.v1_9_R2.PacketPlayInSetCreativeSlot;
import net.minecraft.server.v1_9_R2.PacketPlayInUseEntity;
import net.minecraft.server.v1_9_R2.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R2.CraftServer;

/**
 * @since September 21, 2019
 * @author Andavin
 */
public class PlayerConnectionProxy extends PlayerConnection {

    private final ImageListener listener;
    private final PacketListener packetListener;

    PlayerConnectionProxy(PlayerConnection connection, ImageListener listener, PacketListener packetListener) {
        super(((CraftServer) Bukkit.getServer()).getServer(), connection.networkManager, connection.player);
        this.listener = listener;
        this.packetListener = packetListener;
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        super.sendPacket(packet);
    }

    @Override
    public void a(PacketPlayInUseEntity packet) {
        packetListener.handle(player.getBukkitEntity(), listener, packet);
        super.a(packet);
    }

    @Override
    public void a(PacketPlayInSetCreativeSlot packet) {
        packetListener.handle(player.getBukkitEntity(), packet);
        super.a(packet);
    }
}
