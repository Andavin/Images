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
package com.andavin.images;

import com.andavin.images.PacketListener.ImageListener;
import com.andavin.images.command.CommandRegistry;
import com.andavin.images.data.DataManager;
import com.andavin.images.data.FileDataManager;
import com.andavin.images.data.MySQLDataManager;
import com.andavin.images.data.SQLiteDataManager;
import com.andavin.images.image.CustomImage;
import com.andavin.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;

import static com.andavin.reflect.Reflection.setFieldValue;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * @since September 20, 2019
 * @author Andavin
 */
public class Images extends JavaPlugin implements Listener {

    /*
     * Current Pitfalls:
     *
     *  1. The command system is multiple years old and in much
     *     need of an update. This could easily be improved in many
     *     ways and if there are any suggestions I'd be open to it.
     *
     *  2. The database storage format is simply based on Java I/O Serializable.
     *     There are inherent flaws to this such as no versioning
     *     so it is extremely difficult to make any major changes to
     *     the order of how the data is written.
     *
     *     A better way of storing this, but still preferably as a
     *     byte array format would probably be a nice update.
     *
     *  3. Proxy/multi-server setups are not very well supported.
     *     Each server needs to store it's only images. Therefore, if
     *     a shared MySQL server is used, for instance, then the images
     *     will most likely be duplicated across all servers where
     *     they don't belong.
     *
     *     Maybe a BungeeCord plugin or such could help us out here
     *     or at least a setting to tell us if we are on a proxy and
     *     then somehow store images by server ID (a problem in itself).
     */

    private static final CustomImage[] EMPTY_IMAGES_ARRAY = new CustomImage[0];
    private boolean protocolLib;
    private static Images instance;
    private static File imagesDirectory;
    private static DataManager dataManager;
    private static final List<CustomImage> IMAGES = new ArrayList<>();
    private static final Map<UUID, Long> LAST_MOVE_TIMES = new HashMap<>();
    private static final PacketListener BRIDGE = Versioned.getInstance(PacketListener.class);
    private static final Map<UUID, ImageListener> LISTENER_TASKS = new HashMap<>(4);

    @Override
    public void onLoad() {
        instance = this;
        Logger.initialize(this.getLogger());
        imagesDirectory = this.getDataFolder();
        PacketListener.getImages = () -> IMAGES;
        setFieldValue(Scheduler.class, null, "instance", this);
        setFieldValue(TimeoutMetadata.class, null, "instance", this);
    }

    @Override
    public void onEnable() {

        this.saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        if (MinecraftVersion.isPaper()) {
            Logger.info("PaperMC server detected. Adjustments will be made to accommodate...");
        }

        Plugin protocolLib = Bukkit.getPluginManager().getPlugin("ProtocolLib");
        if (protocolLib != null) { // ProtocolLib is present so use it for higher stability
            this.protocolLib = true;
            Logger.info("ProtocolLib detected. Enabling generic packet handling...");
            ProtocolLibListener.register(this, LISTENER_TASKS, BRIDGE);
        }

        FileConfiguration config = this.getConfig();
        MapHelper.invisible = config.getBoolean("invisible-frames", true);
        MapHelper.showDistance = config.getInt("show-distance", 64);
        MapHelper.hideDistance = config.getInt("hide-distance", 128);
        String type = config.getString("database.type").toUpperCase(Locale.ENGLISH);
        switch (type) {
            case "MYSQL":
                dataManager = new MySQLDataManager(
                        config.getString("database.host"),
                        config.getInt("database.port"),
                        config.getString("database.schema"),
                        config.getString("database.user"),
                        config.getString("database.password")
                );
                break;
            case "SQLITE":
                dataManager = new SQLiteDataManager(new File(imagesDirectory, "images.db"));
                break;
            case "FILE":
                dataManager = new FileDataManager(new File(imagesDirectory, "images.cimg"));
                break;
            default:
                throw new IllegalStateException("Unknown database type: " + type);
        }

        if (new File(imagesDirectory, "data.yml").exists()) {
            Logger.info("Found legacy image data (1.0.x-SNAPSHOT).");
            Logger.info("Use '/images import' to import it to the new format");
        }

        if (config.getBoolean("database.initialize")) {
            dataManager.initialize();
        }

        Scheduler.laterAsync(() -> {
            IMAGES.addAll(dataManager.load());
            Logger.info("Loaded {} images...", IMAGES.size());
            CommandRegistry.registerCommands();
        }, 40L);

        Scheduler.repeatAsync(() -> {

            try {

                long now = System.currentTimeMillis();
                LAST_MOVE_TIMES.forEach((uuid, time) -> {

                    if (now - time > 2500) { // More than 2.5 seconds

                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null) {
                            refreshImages(player, player.getLocation());
                        }
                    }
                });
            } catch (ConcurrentModificationException ignored) {
            }
        }, 200, 30);
    }

    @EventHandler
    // This is called directly after the PlayerConnection
    // is set as the packetListener for the player
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        Location location = player.getLocation();
        Scheduler.laterAsync(() -> this.refreshImages(player, location), 20L);
        if (protocolLib) {
            return;
        }

        Runnable intercept = () -> {
            BRIDGE.setEntityListener(player, (clicker, image, section, action, hand) -> {

                ImageListener listener = LISTENER_TASKS.remove(clicker.getUniqueId());
                if (listener != null) {
                    listener.click(clicker, image, section, action, hand);
                }
            });
        };
        // If we're using Paper, attempt to delay the entity listener to prevent
        // a bug where the server does not track accurate movement after the replacement
        if (MinecraftVersion.isPaper() && MinecraftVersion.greaterThan(MinecraftVersion.v1_20)) {
            Scheduler.later(intercept, 20L);
        } else {
            intercept.run();
        }

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();
        Scheduler.async(() -> {

            CustomImage[] images;
            synchronized (IMAGES) {
                images = IMAGES.toArray(EMPTY_IMAGES_ARRAY);
            }

            for (CustomImage image : images) {
                image.remove(player, false);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {

        Player player = event.getPlayer();
        Location location = event.getRespawnLocation();
        Scheduler.laterAsync(() -> {

            CustomImage[] images;
            synchronized (IMAGES) {
                images = IMAGES.toArray(EMPTY_IMAGES_ARRAY);
            }

            for (CustomImage image : images) {
                image.remove(player, true);
                image.refresh(player, location);
            }
        }, 20L);
    }

    @EventHandler
    public void onWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        Scheduler.laterAsync(() -> this.refreshImages(player, location), 20L);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();
        Location from = event.getFrom(), to = event.getTo();
        if (!LocationUtil.isSameBlock(from, to)) {
            LAST_MOVE_TIMES.put(player.getUniqueId(), System.currentTimeMillis());
        }

        if (from.getBlockX() >> 4 != to.getBlockX() >> 4 ||
                from.getBlockZ() >> 4 != to.getBlockZ() >> 4) {
            Scheduler.async(() -> this.refreshImages(player, to));
        }
    }

    /**
     * Get the singleton instance of this plugin.
     *
     * @return The plugin instance.
     */
    public static Images getInstance() {
        return instance;
    }

    /**
     * Get the {@link DataManager} for the data storage.
     *
     * @return The data manger.
     */
    public static DataManager getDataManager() {
        return dataManager;
    }

    /**
     * Get the directory in which the images are stored.
     *
     * @return The image directory.
     */
    public static File getImagesDirectory() {
        return imagesDirectory;
    }

    /**
     * Get all the files that are images in the images
     * data folder.
     *
     * @return The image files.
     */
    public static List<File> getImageFiles() {

        File[] files = imagesDirectory.listFiles();
        if (files == null) {
            return emptyList();
        }

        List<File> images = new ArrayList<>(files.length);
        for (File file : files) {

            if (!file.isDirectory()) {

                String path = file.getPath();
                if (!path.endsWith(".yml") && !path.endsWith(".db")) {
                    images.add(file);
                }
            }
        }

        return images;
    }

    /**
     * Get a {@link File} from the image directory that
     * is the given file name or partially matches the name.
     *
     * @param fileName The file name to match the file name to.
     * @return The file with the matching name.
     * @throws IllegalArgumentException If there is no file with
     *                                  the given name to match.
     * @throws IllegalStateException If there are no images in the
     *                               image directory.
     */
    public static File getImageFile(String fileName) throws IllegalArgumentException, IllegalStateException {

        File imageFile = new File(imagesDirectory, fileName);
        if (imageFile.exists()) {
            return imageFile;
        }

        File match = null;
        File[] imageFiles = imagesDirectory.listFiles();
        checkState(imageFiles != null, "§cNo available images");
        for (File file : imageFiles) {

            String name = file.getName();
            if (name.startsWith(fileName) && file.isFile()) {
                // Basically, check if it starts with the fileName and then directly
                // after there is a dot followed by 3 or 4 other characters
                if (name.lastIndexOf('.') == fileName.length() && name.length() - fileName.length() <= 5) {
                    return file;
                }
                // If it's not a perfect match then it's pretty
                // close because it starts with the name
                match = file;
            }
        }

        checkArgument(match != null, "§cImage Not Found§f %s", fileName);
        return match;
    }

    /**
     * Add a new task to be handled when an action is
     * performed by the given player.
     *
     * @param player The player to add the listener for.
     * @param task The task to execute when an action is handled.
     */
    public static void addListenerTask(Player player, ImageListener task) {
        LISTENER_TASKS.put(player.getUniqueId(), task);
    }

    /**
     * Add a new {@link CustomImage} to the image storage
     * and save it to the database.
     *
     * @param image The image to add.
     * @return If the image was successfully added.
     */
    public static boolean addImage(CustomImage image) {

        if (IMAGES.contains(image)) {
            return false;
        }

        dataManager.save(image);
        synchronized (IMAGES) {
            IMAGES.add(image);
        }

        return true;
    }

    /**
     * Add all of the given images to the image storage,
     * but do not save them to the database.
     *
     * @param images The images to add.
     */
    public static void addImages(List<CustomImage> images) {

        synchronized (IMAGES) {
            IMAGES.addAll(images);
        }
    }

    /**
     * Remove the given {@link CustomImage} from the image
     * storage and delete it from the database.
     *
     * @param image The image to remove.
     * @return If the image was successfully removed.
     */
    public static boolean removeImage(CustomImage image) {

        synchronized (IMAGES) {
            dataManager.delete(image);
            return IMAGES.remove(image);
        }
    }

    /**
     * Get all of the {@link CustomImage CustomImages}
     * that match the given criteria.
     *
     * @param test The criteria to test the images with.
     * @return The images matching the criteria.
     */
    public static List<CustomImage> getMatchingImages(Predicate<CustomImage> test) {

        synchronized (IMAGES) {
            return IMAGES.stream().filter(test).collect(toList());
        }
    }

    private void refreshImages(Player player, Location location) {

        CustomImage[] images;
        synchronized (IMAGES) {
            images = IMAGES.toArray(EMPTY_IMAGES_ARRAY);
        }

        for (CustomImage image : images) {
            image.refresh(player, location);
        }
    }
}
