package com.andavin.images.command;

import org.bukkit.entity.Player;

/**
 * @since September 23, 2019
 * @author Andavin
 */
public class ResizeCommand extends BaseCommand {

    ResizeCommand() {
        super("resize", "image.command.resize");
        this.setDesc("Reset the size of an image to a scaled version");
        this.setUsage("/image resize <percent>");
    }

    @Override
    public void execute(Player player, String label, String[] args) {

    }
}
