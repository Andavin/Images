package com.andavin.images;

import com.andavin.images.command.ImageCommand;
import com.andavin.images.image.Image;
import com.andavin.images.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.File;
import java.io.IOException;

/**
 * Created on February 08, 2018
 *
 * @author Andavin
 */
public final class ImageListener implements Listener {

    private static final BlockFace[] FACES = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };

    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {

        final Action action = event.getAction();
        if (action == Action.LEFT_CLICK_AIR || action == Action.RIGHT_CLICK_AIR) {
            return;
        }

        final Player player = event.getPlayer();
        final boolean create = player.hasMetadata(ImageCommand.CREATE_META);
        final boolean delete = !create && player.hasMetadata(ImageCommand.DELETE_META);
        if (!create && !delete) {
            return;
        }

        event.setCancelled(true);
        // They want to cancel if they left clicked a block
        if (action == Action.LEFT_CLICK_BLOCK) {
            player.removeMetadata(create ? ImageCommand.CREATE_META : ImageCommand.DELETE_META, Images.getInstance());
            player.sendMessage("§a" + (create ? "Creation" : "Deletion") + " cancelled.");
            return;
        }

        if (delete) {
            return;
        }

        // Can only be a right click at this point
        final File imageFile = (File) player.getMetadata(ImageCommand.CREATE_META).get(0).value();
        if (!imageFile.exists()) {
            player.sendMessage("§cThe file §l" + imageFile.getName() + "§c does not exist anymore!");
            return;
        }

        player.removeMetadata(ImageCommand.CREATE_META, Images.getInstance());
        final Image image = Images.createAndStoreImage(imageFile, event.getClickedBlock().getRelative(event.getBlockFace()), event.getBlockFace());
        if (image == null) {
            player.sendMessage("§cFailed to create the image §l" + imageFile.getName() + "§c at that location.");
            return;
        }

        player.sendMessage("§aSuccessfully created image §l" + imageFile.getName() + "§a.");
        Bukkit.getScheduler().runTaskAsynchronously(Images.getInstance(), () -> {

            player.sendMessage("§aSaving...");

            try {
                Images.save();
            } catch (IOException e) {
                Logger.severe(e, "Failed to save images.");
            }

            player.sendMessage("§aFinished!");
        });
    }

    @EventHandler
    public void onInteract(final PlayerInteractEntityEvent event) {

        final Entity entity = event.getRightClicked();
        if (entity instanceof ItemFrame) {

            final Image image = Images.getImage(entity.getLocation());
            if (image != null) {

                event.setCancelled(true);
                final Player player = event.getPlayer();
                if (player.hasMetadata(ImageCommand.DELETE_META)) {
                    player.sendMessage("§aDeleting image §l" + image.getImageFile().getName() + "§a.");
                    player.removeMetadata(ImageCommand.DELETE_META, Images.getInstance());
                    Images.delete(player, image);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAttack(final EntityDamageByEntityEvent event) {

        final Entity entity = event.getEntity();
        if (entity instanceof ItemFrame && Images.getImage(entity.getLocation()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(final HangingBreakEvent event) {

        final Hanging hanging = event.getEntity();
        if (hanging instanceof ItemFrame && Images.getImage(hanging.getLocation()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(final HangingBreakByEntityEvent event) {
        this.onBreak((HangingBreakEvent) event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(final BlockPlaceEvent event) {

        final Location loc = event.getBlock().getLocation();
        if (Images.getImage(loc) != null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cCannot place that there!");
        }
    }
}
