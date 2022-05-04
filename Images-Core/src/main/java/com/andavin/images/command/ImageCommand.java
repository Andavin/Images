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

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import org.bukkit.entity.Player;

import java.util.HashSet;

/**
 * @since September 23, 2019
 * @author Andavin
 */
public class ImageCommand extends BaseCommand {

    ImageCommand() {
        super("nft", "images.command.manage");
        this.setAliases("customimage", "images", "img");
        this.setDesc("Manage custom images.");
        this.setUsage("/nft [create|delete|list|import|transfer]");
        this.addChild(new CreateCommand());
        this.addChild(new DeleteCommand());
        this.addChild(new ListCommand());
        this.addChild(new ImportCommand());
        this.addChild(new TransferCommand());
//        this.addChild(new SizeCommand());
//        this.addChild(new ResizeCommand());
    }

    @Override
    public void execute(Player player, String label, String[] args) {

        if (args.length != 0) {
            player.sendMessage("§cUnknown command§f " + args[0]);
        }

        for (BaseCommand command : new HashSet<>(this.getChildren().values())) {
            player.spigot().sendMessage(new ComponentBuilder("§e§l" + command.getUsage())
                    .event(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder(
                            "§a" + command.getDescription()).create()))
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command.getUsage())).create());
        }
    }
}
