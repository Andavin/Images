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
import com.andavin.images.image.CustomImage;
import com.andavin.util.*;
import com.github.puregero.multilib.MultiLib;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.json.JSONObject;
import xyz.critterz.core.http.PostRequest;
import xyz.critterz.core.variables.Variable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.andavin.util.MinecraftVersion.v1_13;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since February 14, 2018
 * @author Andavin
 */
final class CreateCommand extends BaseCommand implements Listener {

    private static final Predicate<String> URL_TEST = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]").asPredicate();
    private final Map<UUID, CreateImageTask> creating = new HashMap<>();

    CreateCommand() {
        super("create", "images.command.create");
        this.setAliases("new", "add", "load");
        this.setMinimumArgs(2);
        this.setUsage("/nft create <contract address> <token id>");
        this.setDesc("Create and begin pasting a new custom nft");
        Bukkit.getPluginManager().registerEvents(this, Images.getInstance());
    }

    @Override
    public void execute(Player player, String label, String[] args) {

        ImageSupplier imageSupplier;
        Supplier<String> nameSupplier;
        String address = args[0];
        int tokenId;
        try {
            tokenId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Token ID must be a number!");
            return;
        }
        String imageNameArg = Variable.NFT_RENDER_ENDPOINT.getValue() + "/?contractAddress="+address+"&tokenId="+tokenId+"&size=500";
        if (URL_TEST.test(imageNameArg)) {

            AtomicReference<String> fileName = new AtomicReference<>();
            imageSupplier = () -> {
                URI uri = new URI(imageNameArg);
                int slash = imageNameArg.lastIndexOf('/');
                fileName.set(slash == -1 ? imageNameArg :
                        imageNameArg.substring(slash + 1));
                return ImageIO.read(uri.toURL());
            };

            nameSupplier = fileName::get;
        } else {
            File imageFile = Images.getImageFile(imageNameArg);
            imageSupplier = () -> ImageIO.read(imageFile);
            nameSupplier = imageFile::getName;
        }

        double scale;
        /*if (args.length > 1) {

            try {
                scale = Double.parseDouble(args[1]) / 100;
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid scale §f" + args[1]);
                return;
            }

            if (scale < 0.01) {
                player.sendMessage("§cScale must be more than 1%, but got §f" + scale * 100 + '%');
                return;
            }
        } else {*/
            scale = 1;
        //}

        UUID id = player.getUniqueId();
        this.creating.put(id, new CreateImageTask(scale, imageSupplier, nameSupplier, address, tokenId));
        player.sendMessage(ChatColor.YELLOW + "You are trying to paste NFT token " + ChatColor.GOLD + tokenId + ChatColor.YELLOW + " from contract " + ChatColor.GOLD + address);
        player.sendMessage(ChatColor.YELLOW  + "Right click the top left corner. The image will then paste from left to right in a 3x3 square!");
        player.sendMessage(ChatColor.YELLOW + "You need to own this NFT in order to import it!");
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
        if(!player.hasPermission("critterz.admin") && !isMemberOfRegion(event.getClickedBlock().getLocation(), event.getPlayer())) {
            player.sendMessage(ChatColor.RED + "You need to be a member of this plot!");
            return;
        }
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
                }

                Scheduler.async(() -> {

                    player.sendMessage(ChatColor.YELLOW + "Starting image paste..");
                    BufferedImage image = task.readImage();
                    if (image == null) {
                        player.sendMessage("§cInvalid image file! Please make sure the contact address and token id are correct.");
                        return;
                    }
                    player.sendMessage(ChatColor.YELLOW + "Attempting to mint NFT..");

                    CustomImage customImage = new CustomImage(player.getUniqueId(),

                            task.nameSupplier.get(), location, direction, image);
                    JSONObject json = new JSONObject();
                    json.put("assetUuid", customImage.getUuid());
                    json.put("tokenAddress", task.contract);
                    json.put("tokenId", task.tokenId);

                    PostRequest postRequest = new PostRequest(Variable.NODE_BACKEND_ENDPOINT.getValue() + "/nft-art/create")
                            .withJsonVariables(json).withSubject(player.getUniqueId().toString());

                    postRequest.send().whenComplete((response, throwable) -> {
                        if(response.statusCode() == 200) {
                            customImage.refresh(player, playerLocation);
                            if (Images.addImage(customImage)) {
                            } else {
                                player.sendMessage("§cFailed to create image at that location");
                            }
                            player.sendMessage("§aSuccessfully created image");
                            MultiLib.notify("images:syncimage", Base64.getEncoder().encodeToString(toByteArray(customImage)));
                        } else {
                            player.sendMessage(ChatColor.RED + String.valueOf(response.statusCode()) + " - import failed - " + response.body());
                        }
                    }).exceptionally(throwable -> {
                        player.sendMessage(ChatColor.RED + throwable.getMessage());
                        return null;
                    });
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
        private final ImageSupplier imageSupplier;
        private final Supplier<String> nameSupplier;
        private final String contract;
        private final int tokenId;

        CreateImageTask(double scale, ImageSupplier supplier, Supplier<String> nameSupplier, String contract, int tokenId) {
            checkArgument(scale > 0,
                    "§cScale must be greater than zero§f %s", scale);
            this.scale = scale;
            this.nameSupplier = nameSupplier;
            this.imageSupplier = checkNotNull(supplier);
            this.contract = contract;
            this.tokenId = tokenId;
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

                BufferedImage image = imageSupplier.get();
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

            } catch (Exception e) {
                Logger.debug(e);
                return null;
            }
        }
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

    private interface ImageSupplier {

        BufferedImage get() throws Exception;
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
}
