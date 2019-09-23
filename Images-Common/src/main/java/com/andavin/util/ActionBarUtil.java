/*
 * MIT License
 *
 * Copyright (c) 2018 Andavin
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

package com.andavin.util;

import com.andavin.images.Versioned;
import org.bukkit.entity.Player;

/**
 * @author Andavin
 * @since April 19, 2018
 */
public abstract class ActionBarUtil implements Versioned {

    private static final ActionBarUtil BRIDGE = Versioned.getInstance(ActionBarUtil.class);

    /**
     * Send an action bar message to a player. This is the message
     * that appears above their hot bar.
     *
     * @param player The player to send the message to.
     * @param message The message to send.
     */
    public static void sendActionBar(Player player, String message) {
        BRIDGE.sendMessage(player, message);
    }

    /**
     * Send an action bar message to a player. This is the message
     * that appears above their hot bar.
     *
     * @param player The player to send the message to.
     * @param message The message to send.
     */
    protected abstract void sendMessage(Player player, String message);
}
