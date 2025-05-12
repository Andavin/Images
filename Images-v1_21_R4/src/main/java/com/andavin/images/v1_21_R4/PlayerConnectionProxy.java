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
package com.andavin.images.v1_21_R4;

import com.andavin.images.PacketListener.ImageListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromEntityPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

/**
 * @since March 19, 2023
 * @author Andavin
 */
public class PlayerConnectionProxy extends ChannelInboundHandlerAdapter {

    private long lastInteract;
    private final ImageListener listener;
    private final PacketListener packetListener;
    private final ServerGamePacketListenerImpl connection;

    PlayerConnectionProxy(ServerGamePacketListenerImpl connection,
                          ImageListener listener, PacketListener packetListener) {
        this.connection = connection;
        this.listener = listener;
        this.packetListener = packetListener;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof ServerboundInteractPacket packet) {
                System.out.println("Interact packet");
                packetListener.handle(connection.player.getBukkitEntity(), listener, packet);
            } else if (msg instanceof ServerboundPickItemFromEntityPacket packet) {
                System.out.println("Pick item packet");
                long now = System.currentTimeMillis();
                if (now - lastInteract > 10) {
                    lastInteract = now;
                    System.out.println("handling Pick item packet");
                    packetListener.handle(connection.player.getBukkitEntity(), packet);
                }
            }
        } finally {
            super.channelRead(ctx, msg);
        }
    }
}
