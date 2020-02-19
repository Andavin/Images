/* Copyright (c) 2019 */
package com.andavin.images.command;

import org.bukkit.entity.Player;

/**
 * @since September 23, 2019
 * @author Andavin
 */
public class SizeCommand extends BaseCommand {

    SizeCommand() {
        super("size", "image.command.size");
        this.setDesc("Show the size of an image");
        this.setUsage("/image size");
    }

    @Override
    public void execute(Player player, String label, String[] args) {

    }
}
