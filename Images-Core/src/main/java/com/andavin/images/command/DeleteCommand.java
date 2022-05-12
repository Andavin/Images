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
import com.andavin.images.image.CustomImage;
import com.andavin.util.ActionBarUtil;
import com.andavin.util.MinecraftVersion;
import com.andavin.util.Scheduler;
import com.github.puregero.multilib.MultiLib;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.json.JSONObject;
import xyz.critterz.core.http.PostRequest;
import xyz.critterz.core.variables.Variable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.util.Base64;
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

            if(getRegion(player.getLocation()).getOwners().getUniqueIds().toArray().length == 0) {
                player.sendMessage(ChatColor.RED + "This plot needs to have an owner!");
                return;
            }
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

                    this.deleting.remove(player.getUniqueId());
                    JSONObject json = new JSONObject();
                    json.put("plotOwnerUuid", getRegion(clicker.getLocation()).getOwners().getUniqueIds().toArray()[0].toString());
                    json.put("tokenAddress", image.getContract());
                    json.put("tokenId", image.getTokenId());

                    PostRequest postRequest = new PostRequest(Variable.NODE_BACKEND_ENDPOINT.getValue() + "/nft-art/delete")
                            .withJsonVariables(json).withSubject(player.getUniqueId().toString());

                    postRequest.send().whenComplete((response, throwable) -> {
                        if (response.statusCode() == 200) {
                            if (!Images.removeImage(image)) {
                                image.destroy();
                                MultiLib.notify("images:deleteimage", Base64.getEncoder().encodeToString(toByteArray(image)));
                                player.sendMessage("§aNFT successfully deleted");
                            } else
                                player.sendMessage("§cFailed to delete NFT");
                        } else {
                            player.sendMessage(ChatColor.RED + String.valueOf(response.statusCode()) + " - delete failed - " + response.body());
                        }
                    }).exceptionally(throwable -> {
                        player.sendMessage(ChatColor.RED + throwable.getMessage());
                        return null;
                    });
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

    private ProtectedRegion getRegion(Location location) {
        ProtectedRegion protectedRegion = null;

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(location.getWorld()));

        for (ProtectedRegion region : regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(location))) {
            protectedRegion = region;
        }
        return protectedRegion;
    }

    private boolean isMemberOfRegion(Location location, Player player) {
        boolean isMember = false;

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(location.getWorld()));

        for (ProtectedRegion region : regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(location))) {
            if(region.isMember(WorldGuardPlugin.inst().wrapPlayer(player))) {
                isMember = true;
            }
        }
        return isMember;
    }

    private byte[] toByteArray(CustomImage image) {

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(); // Doesn't need to be closed
        try (ObjectOutputStream stream = new ObjectOutputStream(byteStream)) {
            stream.writeObject(image);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return byteStream.toByteArray();
    }

    private boolean cancel(Player player) {

        if (this.deleting.remove(player.getUniqueId())) {
            player.sendMessage("§cDeletion cancelled");
            return true;
        }

        return false;
    }
}
