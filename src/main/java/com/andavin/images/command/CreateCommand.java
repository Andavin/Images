package com.andavin.images.command;

import com.andavin.images.Images;
import com.andavin.images.util.Reflection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.lang3.StringUtils;
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
            final Object comp = Reflection.getInstance(Reflection.getMcClass("ChatComponentText"), "§eClick where the top left corner should be");
            this.packet = Reflection.getInstance(ImageCommand.PACKET, comp, (byte) 2);
        } else {
            this.packet = null;
        }
    }

    @Override
    public void execute(final Player player, final String label, final String[] args) {

        final StringBuilder sb = new StringBuilder();
        for (final String arg : args) {
            sb.append(arg).append(' ');
        }

        final String image = this.appendExtension(sb.substring(0, sb.length() - 1));
        final File file = Images.getImage(image);
        if (file.exists()) {

            player.setMetadata(ImageCommand.CREATE_META, new FixedMetadataValue(Images.getInstance(), file));
            player.sendMessage("§eLeft click any block to cancel.");

            Object connection = null;
            if (Reflection.VERSION_NUMBER < 1120) {
                final Object entityPlayer = Reflection.invokeMethod(ImageCommand.GET_HANDLE, player);
                connection = Reflection.getValue(ImageCommand.CONNECTION, entityPlayer);
            }

            final Object finalConn = connection;
            new BukkitRunnable() {

                @Override
                public void run() {

                    if (player.hasMetadata(ImageCommand.CREATE_META)) {

                        if (Reflection.VERSION_NUMBER >= 1120) {
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, CreateCommand.this.components);
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
    public void tabComplete(final CommandSender sender, final String[] args, final List<String> completions) {

        if (args.length == 1) {

            Images.getImageFiles().forEach(file -> {

                final String name = file.getName();
                if (StringUtils.startsWithIgnoreCase(name, args[0])) {
                    completions.add(name);
                }
            });
        }
    }

    @Override
    public boolean hasPermission(final Player player, final String[] args) {
        return player.hasPermission("image.manage.create");
    }

    private String appendExtension(final String image) {

        for (final String extension : Images.EXTENSIONS) {

            if (image.endsWith(extension)) {
                return image;
            }
        }

        for (final File file : Images.getImageFiles()) {

            final String name = file.getName();
            if (!name.startsWith(image)) {
                continue;
            }

            final String extension = name.substring(name.indexOf('.'));
            for (final String ext : Images.EXTENSIONS) {

                if (extension.equalsIgnoreCase(ext)) {
                    return name;
                }
            }
        }

        return image;
    }
}
