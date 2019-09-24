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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.andavin.util.MinecraftVersion.v1_13;

/**
 * Created on February 14, 2018
 *
 * @author Andavin
 */
final class CreateCommand extends BaseCommand implements Listener {

    private final Map<UUID, File> creating = new HashMap<>();

    CreateCommand() {
        super("create", "images.command.create");
        this.setAliases("new", "add", "load");
        this.setMinimumArgs(1);
        this.setUsage("/image create <image name>");
        this.setDesc("Create and and begin pasting a new custom image");
        Bukkit.getPluginManager().registerEvents(this, Images.getInstance());
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        String image = StringUtils.join(args, ' ');
        File imageFile = Images.getImageFile(image);
        UUID id = player.getUniqueId();
        this.creating.put(id, imageFile);
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
                    completions.add(name);
                }
            });
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        File imageFile = this.creating.remove(player.getUniqueId());
        if (imageFile == null) {
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
                    location = playerLocation.clone().add(0, 1, 0);
                }

                if (direction == BlockFace.SELF || MinecraftVersion.greaterThanOrEqual(v1_13)) {

                    switch (direction) {
                        case UP:
                        case DOWN:
                        case SELF:
                            player.sendMessage("§cUnsupported direction!");
                            return;
                    }
                }

                Scheduler.async(() -> {

                    player.sendMessage("§aStarting image paste");
                    BufferedImage image;
                    try {
                        image = ImageIO.read(imageFile);
                    } catch (IOException e) {
                        player.sendMessage("§cInvalid image file! Please choose another.");
                        return;
                    }

                    CustomImage customImage = new CustomImage(player.getUniqueId(),
                            imageFile.getName(), location, direction, image);
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
}
