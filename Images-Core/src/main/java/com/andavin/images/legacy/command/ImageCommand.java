package com.andavin.images.legacy.command;

import com.andavin.images.legacy.util.Reflection;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created on February 08, 2018
 *
 * @author Andavin
 */
public final class ImageCommand extends BaseCommand {

    public static final String CREATE_META = "image-create", DELETE_META = "image-delete";

    static final Field CONNECTION = Reflection.getField(Reflection.getMcClass("EntityPlayer"), "playerConnection");
    static final Method SEND_PACKET = Reflection.getMethod(Reflection.getMcClass("PlayerConnection"), "sendPacket", Reflection.getMcClass("Packet"));
    static final Method GET_HANDLE = Reflection.getMethod(Reflection.getCraftClass("entity.CraftPlayer"), "getHandle");
    static final Constructor<?> PACKET = Reflection.getConstructor(Reflection.getMcClass("PacketPlayOutChat"),
            Reflection.getMcClass("IChatBaseComponent"), byte.class);

    ImageCommand() {
        super("image");
        this.setAliases("images", "img");
        this.setUsage("/image <create|delete|options> [image name]");
        this.setDesc("List all available command for images.");
        this.addChild(new CreateCommand());
        this.addChild(new DeleteCommand());
        this.addChild(new OptionsCommand());
    }

    @Override
    public void execute(Player player, String label, String[] args) {

        if (args.length > 0) {
            player.sendMessage("§cUnrecognized command!");
        }

        player.sendMessage("§e§l/image create <image name> §7- §aTo create a new image.");
        player.sendMessage("§e§l/image delete §7- §aTo delete an existing image.");
        player.sendMessage("§e§l/image options §7- §aTo see the options of images that can be added.");
    }

    @Override
    public boolean hasPermission(Player player, String[] args) {
        return player.hasPermission("image.manage");
    }
}
