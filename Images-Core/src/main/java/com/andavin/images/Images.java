package com.andavin.images;

import com.andavin.images.PacketListener.ImageListener;
import com.andavin.images.command.CommandRegistry;
import com.andavin.images.data.DataManager;
import com.andavin.images.data.FileDataManager;
import com.andavin.images.data.MySQLDataManager;
import com.andavin.images.data.SQLiteDataManager;
import com.andavin.images.image.CustomImage;
import com.andavin.util.Logger;
import com.andavin.util.Scheduler;
import com.andavin.util.TimeoutMetadata;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

import static com.andavin.reflect.Reflection.setFieldValue;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.emptyList;

/**
 * @since September 20, 2019
 * @author Andavin
 */
public class Images extends JavaPlugin implements Listener {

    private static final int PIXELS_PER_FRAME = 128;
    public static final String[] EXTENSIONS = { ".png", ".jpeg", ".jpg", /*".gif"*/ };

    private static Images instance;
    private static File imagesDirectory;
    private static DataManager dataManager;
    private static final List<CustomImage> IMAGES = new ArrayList<>();
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

        FileConfiguration config = this.getConfig();
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
    }

    @EventHandler
    // This is called directly after the PlayerConnection
    // is set as the packetListener for the player
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        Location location = player.getLocation();
        Scheduler.laterAsync(() -> this.refreshImages(player, location), 20L);
        BRIDGE.setEntityListener(player, (clicker, image, section, action, hand) -> {

            ImageListener listener = LISTENER_TASKS.remove(clicker.getUniqueId());
            if (listener != null) {
                listener.click(clicker, image, section, action, hand);
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();
        Scheduler.async(() -> {

            synchronized (IMAGES) {

                for (CustomImage image : IMAGES) {
                    image.remove(player, false);
                }
            }
        });
    }

    @EventHandler
    public void onWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        Scheduler.laterAsync(() -> this.refreshImages(player, location), 20L);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {

        Location from = event.getFrom(), to = event.getTo();
        if (from.getBlockX() >> 4 != to.getBlockX() >> 4 ||
                from.getBlockZ() >> 4 != to.getBlockZ() >> 4) {
            Player player = event.getPlayer();
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

            if (file.isDirectory()) {
                continue;
            }

            String path = file.getPath();
            for (String extension : EXTENSIONS) {

                if (path.endsWith(extension)) {
                    images.add(file);
                    break;
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

    private void refreshImages(Player player, Location location) {

        synchronized (IMAGES) {

            for (CustomImage image : IMAGES) {
                image.refresh(player, location);
            }
        }
    }
}
