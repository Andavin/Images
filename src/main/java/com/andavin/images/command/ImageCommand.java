package com.andavin.images.command;

import com.andavin.images.Images;
import com.andavin.images.util.Reflection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created on February 08, 2018
 *
 * @author Andavin
 */
public final class ImageCommand extends BaseCommand {

    public static final String CREATE_META = "image-create", DELETE_META = "image-delete";

    private static final Field CONNECTION = Reflection.getField(Reflection.getMcClass("EntityPlayer"), "playerConnection");
    private static final Method SEND_PACKET = Reflection.getMethod(Reflection.getMcClass("PlayerConnection"), "sendPacket", Reflection.getMcClass("Packet"));
    private static final Method GET_HANDLE = Reflection.getMethod(Reflection.getCraftClass("entity.CraftPlayer"), "getHandle");
    private static final Constructor<?> PACKET = Reflection.getConstructor(Reflection.getMcClass("PacketPlayOutChat"),
            Reflection.getMcClass("IChatBaseComponent"), byte.class);

    ImageCommand() {
        super("image");
        this.setAliases("images", "img");
        this.setUsage("/image <create|delete|options> [image name]");
        this.setDesc("List all available command for images.");
        this.addChild(new Create());
        this.addChild(new Delete());
        this.addChild(new Options());
    }

    @Override
    public void execute(final Player player, final String label, final String[] args) {

        if (args.length > 0) {
            player.sendMessage("§cUnrecognized command!");
        }

        player.sendMessage("§e§l/image create <image name> §7- §aTo create a new image.");
        player.sendMessage("§e§l/image delete §7- §aTo delete an existing image.");
        player.sendMessage("§e§l/image options §7- §aTo see the options of images that can be added.");
    }

    @Override
    public boolean hasPermission(final Player player, final String[] args) {
        return player.hasPermission("image.manage");
    }

    private class Create extends BaseCommand {

        private final Object packet;
        private final BaseComponent[] components = new ComponentBuilder("Click where the top left corner should be")
                .color(ChatColor.YELLOW).create();

        private Create() {

            super("create");
            this.setAliases("new", "add", "load");
            this.setMinimumArgs(1);
            this.setUsage("/image create <image name>");
            this.setDesc("Create or load a new image into the game (name must not contain spaces).");

            if (Reflection.VERSION_NUMBER < 1120) {
                final Object comp = Reflection.getInstance(Reflection.getMcClass("ChatComponentText"), "§eClick where the top left corner should be");
                this.packet = Reflection.getInstance(PACKET, comp, (byte) 2);
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

                player.setMetadata(CREATE_META, new FixedMetadataValue(Images.getInstance(), file));
                player.sendMessage("§eLeft click any block to cancel.");

                Object connection = null;
                if (Reflection.VERSION_NUMBER < 1120) {
                    final Object entityPlayer = Reflection.invokeMethod(GET_HANDLE, player);
                    connection = Reflection.getValue(CONNECTION, entityPlayer);
                }

                final Object finalConn = connection;
                new BukkitRunnable() {

                    @Override
                    public void run() {

                        if (player.hasMetadata(CREATE_META)) {

                            if (Reflection.VERSION_NUMBER >= 1120) {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, Create.this.components);
                            } else {
                                Reflection.invokeMethod(SEND_PACKET, finalConn, Create.this.packet);
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

        private void preloadImage(final File file) {

        }
    }

    private class Delete extends BaseCommand {

        private final Object packet;
        private final BaseComponent[] components = new ComponentBuilder("Click on an image to delete").color(ChatColor.RED).create();

        private Delete() {

            super("delete");
            this.setAliases("remove", "unload");
            this.setUsage("/image delete");
            this.setDesc("Delete an existing image by clicking on it.");

            if (Reflection.VERSION_NUMBER < 1120) {
                final Object comp = Reflection.getInstance(Reflection.getMcClass("ChatComponentText"), "§cClick on an image to delete");
                this.packet = Reflection.getInstance(PACKET, comp, (byte) 2);
            } else {
                this.packet = null;
            }
        }

        @Override
        public void execute(final Player player, final String label, final String[] args) {

            player.setMetadata(DELETE_META, new FixedMetadataValue(Images.getInstance(), 0));
            player.sendMessage("§eLeft click any block to cancel.");

            Object connection = null;
            if (Reflection.VERSION_NUMBER < 1120) {
                final Object entityPlayer = Reflection.invokeMethod(GET_HANDLE, player);
                connection = Reflection.getValue(CONNECTION, entityPlayer);
            }

            final Object finalConn = connection;
            new BukkitRunnable() {

                @Override
                public void run() {

                    if (player.hasMetadata(DELETE_META)) {

                        if (Reflection.VERSION_NUMBER >= 1120) {
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, Delete.this.components);
                        } else {
                            Reflection.invokeMethod(SEND_PACKET, finalConn, Delete.this.packet);
                        }
                    } else {
                        this.cancel();
                    }
                }
            }.runTaskTimerAsynchronously(Images.getInstance(), 5L, 20L);
        }

        @Override
        public boolean hasPermission(final Player player, final String[] args) {
            return player.hasPermission("image.manage.delete");
        }
    }

    private class Options extends BaseCommand {

        private Options() {
            super("options");
            this.setAliases("list");
            this.setUsage("/image options");
            this.setDesc("Show all image file options in the image directory available to be created.");
        }

        @Override
        public void execute(final Player player, final String label, final String[] args) {

            player.sendMessage("§a§lImage Options");
            Images.getImageFiles().forEach(file -> {
                final String name = file.getName();
                player.spigot().sendMessage(new ComponentBuilder(" - ").color(ChatColor.GRAY).append(name).color(ChatColor.YELLOW)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/image create " + name))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to create ")
                                .color(ChatColor.GREEN).append(name).color(ChatColor.YELLOW).bold(true).create())).create());
            });
        }

        @Override
        public boolean hasPermission(final Player player, final String[] args) {
            return player.hasPermission("image.manage.options");
        }
    }
}
