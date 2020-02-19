/* Copyright (c) 2019 */
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
        super("image", "images.command.manage");
        this.setAliases("customimage", "images", "img");
        this.setDesc("Manage custom images.");
        this.setUsage("/image [create|delete|list|import]");
        this.addChild(new CreateCommand());
        this.addChild(new DeleteCommand());
        this.addChild(new ListCommand());
        this.addChild(new ImportCommand());
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
