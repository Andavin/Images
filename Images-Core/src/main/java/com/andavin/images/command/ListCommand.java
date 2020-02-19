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
package com.andavin.images.command;

import com.andavin.images.Images;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;

/**
 * @since February 14, 2018
 * @author Andavin
 */
final class ListCommand extends BaseCommand {

    ListCommand() {
        super("list", "images.command.list");
        this.setAliases("options");
        this.setUsage("/image list");
        this.setDesc("Show a list of the options of images that can be added");
    }

    @Override
    public void execute(Player player, String label, String[] args) {

        player.sendMessage("§a§lImage Options");
        List<File> images = Images.getImageFiles();
        if (images.isEmpty()) {
            player.sendMessage("§cNo images available");
            return;
        }

        for (File image : images) {
            String name = image.getName();
            player.spigot().sendMessage(new ComponentBuilder(" - ").color(ChatColor.GRAY).append(name).color(ChatColor.YELLOW)
                    .event(new ClickEvent(Action.RUN_COMMAND, "/image create " + name))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to create ")
                            .color(ChatColor.GREEN).append(name).color(ChatColor.YELLOW).bold(true).create())).create());
        }
    }
}
