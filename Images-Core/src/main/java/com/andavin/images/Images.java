package com.andavin.images;

import com.andavin.images.PacketListener.ImageListener;
import com.andavin.images.command.CommandRegistry;
import com.andavin.images.data.DataManager;
import com.andavin.images.data.FileDataManager;
import com.andavin.images.data.MySQLDataManager;
import com.andavin.images.data.SQLiteDataManager;
import com.andavin.images.image.CustomImage;
import com.andavin.images.legacy.LegacyImportManager;
import com.andavin.util.LocationUtil;
import com.andavin.util.Logger;
import com.andavin.util.Scheduler;
import com.andavin.util.TimeoutMetadata;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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

    /*
     * Commands:
     * - list
     * - create <image> [scale]
     * - resize [scale] // Will interact with the image to resize
     * - delete // Will interact with the image to resize
     * - import // Will save images and then tell the player how many were
     *             imported and add them to the loaded images list
     */

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

        IMAGES.addAll(dataManager.load());
        Logger.info("Loaded {} images...", IMAGES.size());
        CommandRegistry.registerCommands();
    }

    @Override
    public void onDisable() {
        dataManager.saveAll(IMAGES);
    }

    @EventHandler
    // This is called directly after the PlayerConnection
    // is set as the packetListener for the player
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        Location location = player.getLocation();
        Bukkit.getScheduler().runTaskAsynchronously(this,
                () -> this.refreshImages(player, location));
        BRIDGE.setEntityListener(player, (clicker, image, section, action, hand) -> {

            ImageListener listener = LISTENER_TASKS.remove(clicker.getUniqueId());
            if (listener != null) {
                listener.click(clicker, image, section, action, hand);
            }
        });
    }

    @EventHandler
    public void onWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        Bukkit.getScheduler().runTaskAsynchronously(this,
                () -> this.refreshImages(player, location));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {

        Location from = event.getFrom(), to = event.getTo();
        if (from.getBlockX() >> 4 != to.getBlockX() >> 4 ||
                from.getBlockZ() >> 4 != to.getBlockZ() >> 4) {
            Player player = event.getPlayer();
            Bukkit.getScheduler().runTaskAsynchronously(this,
                    () -> this.refreshImages(player, to));
        }
    }

    // TODO figure out why it's so slow after the command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;
        if (args[0].equalsIgnoreCase("import")) {

            try {
                player.sendMessage("§aImporting legacy images.\n" +
                        "§eThis will cause sever lag. Please wait...");
                List<CustomImage> images = LegacyImportManager.importImages(imagesDirectory, dataManager);
                synchronized (IMAGES) {
                    IMAGES.addAll(images);
                }

                player.sendMessage("§aSuccessfully imported §f" + images.size() + "§a images");
            } catch (IllegalStateException e) {
                player.sendMessage(e.getMessage());
            }

            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {

            try {

                File file = getImageFile(args[0]);
                BufferedImage image = ImageIO.read(file);
                if (image.getWidth() < PIXELS_PER_FRAME || image.getHeight() < PIXELS_PER_FRAME) {
                    throw new IllegalArgumentException("§cThe image §l" + file.getName() + "§c is too small! Must be at least 128x128 pixels.");
                }

                Location location = player.getLocation();
                BlockFace direction = LocationUtil.getCardinalDirection(location).getOppositeFace();
                CustomImage customImage = new CustomImage(player.getUniqueId(), file.getName(), location, direction, image);
                customImage.refresh(player, location);
                synchronized (IMAGES) {
                    IMAGES.add(customImage);
                }

            } catch (IOException e) {
                Logger.severe(e);
            } catch (Exception e) {
                Logger.handle(e, player::sendMessage, true);
            }
        });

        return true;
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
