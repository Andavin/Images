package com.andavin.images.command;

import com.andavin.images.Images;
import com.andavin.images.image.CustomImage;
import com.andavin.util.ActionBarUtil;
import com.andavin.util.LocationUtil;
import com.andavin.util.MinecraftVersion;
import com.andavin.util.Scheduler;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.andavin.util.MinecraftVersion.v1_13;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on February 14, 2018
 *
 * @author Andavin
 */
final class CreateCommand extends BaseCommand implements Listener {

    private final Map<UUID, CreateImageTask> creating = new HashMap<>();

    CreateCommand() {
        super("create", "images.command.create");
        this.setAliases("new", "add", "load");
        this.setMinimumArgs(1);
        this.setUsage("/image create <image name> [scale percent]");
        this.setDesc("Create and and begin pasting a new custom image");
        Bukkit.getPluginManager().registerEvents(this, Images.getInstance());
    }

    @Override
    public void execute(Player player, String label, String[] args) {

        File imageFile = Images.getImageFile(args[0]);
        double scale;
        if (args.length > 1) {

            try {
                scale = Double.parseDouble(args[1]) / 100;
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid scale §f" + args[1]);
                return;
            }

            if (scale < 0.1) {
                player.sendMessage("§cScale must be more than 1%, but got §f" + scale * 100 + '%');
                return;
            }
        } else {
            scale = 1;
        }

        UUID id = player.getUniqueId();
        this.creating.put(id, new CreateImageTask(imageFile, scale));
        Scheduler.repeatAsyncWhile(() -> ActionBarUtil.sendActionBar(player,
                "§eRight Click to place§7 - §eLeft Click to cancel"),
                5L, 20L, () -> this.creating.containsKey(id));
    }

    @Override
    public void tabComplete(CommandSender sender, String[] args, List<String> completions) {

        if (args.length == 1) {

            Images.getImageFiles().forEach(file -> {

                String name = file.getName();
                if (StringUtils.startsWithIgnoreCase(name, args[0])) {
                    completions.add(name.replace(' ', '_'));
                }
            });
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        CreateImageTask task = this.creating.remove(player.getUniqueId());
        if (task == null) {
            return;
        }

        switch (event.getAction()) {
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:

                event.setCancelled(true);
                BlockFace direction;
                Location location;
                Location playerLocation = player.getLocation();
                if (event.getClickedBlock() != null) {
                    direction = event.getBlockFace();
                    Block block = event.getClickedBlock().getRelative(direction);
                    // Set the yaw and pitch to the players so we have a good direction
                    location = block.getLocation();
                    location.setYaw(playerLocation.getYaw());
                    location.setPitch(playerLocation.getPitch());
                } else {

                    direction = LocationUtil.getDirection(playerLocation,
                            false, true).getOppositeFace();
                    location = playerLocation.clone();
                    switch (direction) {
                        case UP:
                            break;
                        case DOWN:
                            location.add(0, 2, 0);
                            break;
                        default:
                            location.add(0, 1, 0);
                            break;
                    }
                }

                if (direction == BlockFace.SELF || MinecraftVersion.lessThan(v1_13)) {

                    switch (direction) {
                        case UP:
                        case DOWN:
                        case SELF:
                            player.sendMessage("§cUnsupported direction!");
                            return;
                    }
                } else {

                    switch (direction) {
                        case UP:
                        case DOWN:

                            if (LocationUtil.getCardinalDirection(playerLocation) != BlockFace.NORTH) {
                                player.sendMessage("§cFace north to place an image facing up or down");
                                return;
                            }

                            break;
                    }
                }

                Scheduler.async(() -> {

                    player.sendMessage("§aStarting image paste");
                    BufferedImage image = task.readImage();
                    if (image == null) {
                        player.sendMessage("§cInvalid image file! Please choose another.");
                        return;
                    }

                    CustomImage customImage = new CustomImage(player.getUniqueId(),
                            task.imageFile.getName(), location, direction, image);
                    customImage.refresh(player, playerLocation);
                    if (Images.addImage(customImage)) {
                        player.sendMessage("§aSuccessfully created image§f " + customImage.getImageName());
                    } else {
                        player.sendMessage("§cFailed to create image at that location");
                    }
                });

                break;
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                event.setCancelled(true);
                player.sendMessage("§cCreation cancelled");
                break;
        }
    }

    @EventHandler
    public void onAnimate(PlayerAnimationEvent event) {

        Player player = event.getPlayer();
        if (this.creating.remove(player.getUniqueId()) != null) {
            player.sendMessage("§cCreation cancelled");
        }
    }

    private static class CreateImageTask {

        private final double scale;
        private final File imageFile;

        CreateImageTask(File imageFile, double scale) {
            checkArgument(scale > 0,
                    "§cScale must be greater than zero§f %s", scale);
            this.scale = scale;
            this.imageFile = checkNotNull(imageFile);
        }

        /**
         * Read the image file held within this task
         * at the correct scale.
         *
         * @return The read image or {@code null} if the
         *         image failed to read.
         */
        BufferedImage readImage() {

            try {

                BufferedImage image = ImageIO.read(imageFile);
                if (this.scale == 1) {
                    return image;
                }

                Image scaled = image.getScaledInstance(
                        (int) Math.ceil(image.getWidth() * this.scale),
                        (int) Math.ceil(image.getHeight() * this.scale),
                        Image.SCALE_SMOOTH
                );

                BufferedImage other = new BufferedImage(scaled.getWidth(null),
                        scaled.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                // Copy the image over to the new instance
                Graphics2D graphics = other.createGraphics();
                graphics.drawImage(scaled, 0, 0, null);
                graphics.dispose();
                return other;

            } catch (IOException e) {
                return null;
            }
        }
    }
}
