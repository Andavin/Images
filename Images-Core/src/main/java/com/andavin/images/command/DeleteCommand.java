/*
 * MIT License
 *
 * Copyright (c) 2020 Mark
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.andavin.images.command;

import com.andavin.images.Images;
import com.andavin.images.PacketListener.InteractType;
import com.andavin.util.ActionBarUtil;
import com.andavin.util.MinecraftVersion;
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
import static com.andavin.util.MinecraftVersion.v1_15;

/**
 * @since February 14, 2018
 * @author Andavin
 */
final class DeleteCommand extends BaseCommand implements Listener {

    private final Set<UUID> deleting = new HashSet<>();

    DeleteCommand() {
        super("delete", "images.command.delete");
        this.setAliases("del", "remove", "unload");
        this.setUsage("/nft delete");
        this.setDesc("Delete an existing image by clicking on it");
        this.addChild(new DeleteNearCommand());
        Bukkit.getPluginManager().registerEvents(this, Images.getInstance());
    }

    @Override
    public void execute(Player player, String label, String[] args) {

        if (this.cancel(player)) {
            return;
        }

        UUID id = player.getUniqueId();
        this.deleting.add(id);
        Scheduler.repeatAsyncWhile(() -> {

            if (MinecraftVersion.lessThan(v1_15)) {
                ActionBarUtil.sendActionBar(player, "§eRight Click to delete§7 - §eLeft Click to cancel");
            } else {
                ActionBarUtil.sendActionBar(player, "§eRight Click to delete§7 - §eRerun Command to cancel");
            }
        }, 5L, 20L, () -> this.deleting.contains(id));

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
                        image.destroy();
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

                if (MinecraftVersion.lessThan(v1_15) && this.cancel(event.getPlayer())) {
                    event.setCancelled(true);
                }

                break;
        }
    }

    @EventHandler
    public void onAnimate(PlayerAnimationEvent event) {

        if (MinecraftVersion.lessThan(v1_15)) {
            this.cancel(event.getPlayer());
        }
    }

    private boolean cancel(Player player) {

        if (this.deleting.remove(player.getUniqueId())) {
            player.sendMessage("§cDeletion cancelled");
            return true;
        }

        return false;
    }
}
