package com.andavin.images.legacy.command;

import com.andavin.images.legacy.Images;
import com.andavin.images.legacy.util.Reflection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.List;

/**
 * Created on February 14, 2018
 *
 * @author Andavin
 */
final class CreateCommand extends BaseCommand {

    private final Object packet;
    private final BaseComponent[] components = new ComponentBuilder("Click where the top left corner should be")
            .color(ChatColor.YELLOW).create();

    CreateCommand() {

        super("create");
        this.setAliases("new", "add", "load");
        this.setMinimumArgs(1);
        this.setUsage("/image create <image name>");
        this.setDesc("Create or load a new image into the game (name must not contain spaces).");

        if (Reflection.VERSION_NUMBER < 1120) {
            Object comp = Reflection.getInstance(Reflection.getMcClass("ChatComponentText"), "§eClick where the top left corner should be");
            this.packet = Reflection.getInstance(ImageCommand.PACKET, comp, (byte) 2);
        } else {
            this.packet = null;
        }
    }

    @Override
    public void execute(Player player, String label, String[] args) {

        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            sb.append(arg).append(' ');
        }

        String image = this.appendExtension(sb.substring(0, sb.length() - 1));
        File file = Images.getImage(image);
        if (file.exists()) {

            player.setMetadata(ImageCommand.CREATE_META, new FixedMetadataValue(Images.getInstance(), file));
            player.sendMessage("§eLeft click any block to cancel.");

            Object connection = null;
            if (Reflection.VERSION_NUMBER < 1120) {
                Object entityPlayer = Reflection.invokeMethod(ImageCommand.GET_HANDLE, player);
                connection = Reflection.getValue(ImageCommand.CONNECTION, entityPlayer);
            }

            Object finalConn = connection;
            new BukkitRunnable() {

                @Override
                public void run() {

                    if (player.hasMetadata(ImageCommand.CREATE_META)) {

                        if (Reflection.VERSION_NUMBER >= 1120) {
//                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, CreateCommand.this.components);
                        } else {
                            Reflection.invokeMethod(ImageCommand.SEND_PACKET, finalConn, CreateCommand.this.packet);
                        }
                    } else {
                        this.cancel();
                    }
                }
            }.runTaskTimerAsynchronously(Images.getInstance(), 5L, 20L);
        } else {
            player.sendMessage("§cThe file §l" + image + "§c does not exist!");
        }
    }

    @Override
    public void tabComplete(CommandSender sender, String[] args, List<String> completions) {

        if (args.length == 1) {

            Images.getImageFiles().forEach(file -> {

                String name = file.getName();
                if (StringUtils.startsWithIgnoreCase(name, args[0])) {
                    completions.add(name);
                }
            });
        }
    }

    @Override
    public boolean hasPermission(Player player, String[] args) {
        return player.hasPermission("image.manage.create");
    }

    private String appendExtension(String image) {

        for (String extension : Images.EXTENSIONS) {

            if (image.endsWith(extension)) {
                return image;
            }
        }

        for (File file : Images.getImageFiles()) {

            String name = file.getName();
            if (!name.startsWith(image)) {
                continue;
            }

            String extension = name.substring(name.indexOf('.'));
            for (String ext : Images.EXTENSIONS) {

                if (extension.equalsIgnoreCase(ext)) {
                    return name;
                }
            }
        }

        return image;
    }
}
