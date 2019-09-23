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
 * Created on February 14, 2018
 *
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
