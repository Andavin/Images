package com.andavin.images.command;

import com.andavin.images.Images;
import com.andavin.images.PacketListener.InteractType;
import com.andavin.util.ActionBarUtil;
import com.andavin.util.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.andavin.images.image.CustomImage.UNKNOWN_CREATOR;

/**
 * Created on February 14, 2018
 *
 * @author Andavin
 */
final class DeleteCommand extends BaseCommand implements Listener {

    private final Set<UUID> deleting = new HashSet<>();

    DeleteCommand() {
        super("delete", "images.command.delete");
        this.setAliases("remove", "unload");
        this.setUsage("/image delete");
        this.setDesc("Delete an existing image by clicking on it");
        Bukkit.getPluginManager().registerEvents(this, Images.getInstance());
    }

    @Override
    public void execute(Player player, String label, String[] args) {

        UUID id = player.getUniqueId();
        this.deleting.add(id);
        Scheduler.repeatAsyncWhile(() -> ActionBarUtil.sendActionBar(player,
                "§eRight Click to delete§7 - §eLeft Click to cancel"),
                5L, 20L, () -> this.deleting.contains(id));
        Images.addListenerTask(player, (clicker, image, section, action, hand) -> {

            if (!image.getCreator().equals(UNKNOWN_CREATOR) &&
                    Images.getInstance().getConfig().getBoolean("permissions.creator-restricted")) {

                if (!player.getUniqueId().equals(image.getCreator()) &&
                        !player.hasPermission("images.restricted.bypass")) {
                    player.sendMessage("§cInsufficient permission to modify that image");
                    return;
                }
            }

            if (action == InteractType.RIGHT_CLICK) {

                Scheduler.async(() -> {

                    if (this.deleting.remove(player.getUniqueId()) && Images.removeImage(image)) {
                        image.hide();
                        player.sendMessage("§aImage successfully deleted");
                    } else {
                        player.sendMessage("§cFailed to delete image");
                    }
                });
            }
        });
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        switch (event.getAction()) {
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:

                if (this.cancel(event.getPlayer())) {
                    event.setCancelled(true);
                }

                break;
        }
    }

    @EventHandler
    public void onAnimate(PlayerAnimationEvent event) {
        this.cancel(event.getPlayer());
    }

    private boolean cancel(Player player) {

        if (this.deleting.remove(player.getUniqueId())) {
            player.sendMessage("§cDeletion cancelled");
            return true;
        }

        return false;
    }
}
