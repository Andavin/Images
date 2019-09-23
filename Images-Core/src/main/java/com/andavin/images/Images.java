package com.andavin.images;

import com.andavin.images.data.*;
import com.andavin.images.image.CustomImage;
import com.andavin.images.legacy.image.LegacyImage;
import com.andavin.images.legacy.image.LegacyImageSection;
import com.andavin.util.LocationUtil;
import com.andavin.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

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
     */

    private static final int PIXELS_PER_FRAME = 128;
    private static final PacketListener BRIDGE = Versioned.getInstance(PacketListener.class);
    private static File imagesDirectory;
    private final List<CustomImage> images = new ArrayList<>();
    private DataManager dataManager;

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

    private static BufferedImage resize(BufferedImage image, int xSections, int ySections) {

        Logger.debug("Resizing a file...");
        if (image.getWidth() % PIXELS_PER_FRAME != 0 || image.getHeight() % PIXELS_PER_FRAME != 0) {

            // Get a scaled version of the image
            Logger.debug("The file was an incorrect size and need to be resized!");
            Logger.debug("Getting scaled image...");
            java.awt.Image img = image.getScaledInstance(xSections * PIXELS_PER_FRAME, ySections * PIXELS_PER_FRAME, 1);
            image = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Logger.debug("Copying scaled image to a new BufferedImage...");

            // Copy the image over to the new instance
            Graphics2D g2D = image.createGraphics();
            g2D.drawImage(img, 0, 0, null);
            g2D.dispose();
        }

        Logger.debug("Finished resizing.");
        return image;
    }

    private static void addRelative(Location loc, BlockFace face, int x, int y) {

        switch (face) {
            case NORTH:
                loc.add(-x, -y, 0);
                break;
            case SOUTH:
                loc.add(x, -y, 0);
                break;
            case EAST:
                loc.add(0, -y, -x);
                break;
            case WEST:
                loc.add(0, -y, x);
                break;
        }
    }

    @EventHandler
    // This is called directly after the PlayerConnection
    // is set as the packetListener for the player
    public void onJoin(PlayerJoinEvent event) {
        Logger.info("Joining");
        Player player = event.getPlayer();
        // TODO add connection packet listener
        Location location = player.getLocation();
        Bukkit.getScheduler().runTaskAsynchronously(this,
                () -> this.refreshImages(player, location));
        BRIDGE.addEntityListener(player, (pl, entityId) -> {
            Logger.info("Clicking on {}", entityId);
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

        Location to = event.getTo();
        if (!LocationUtil.isSameBlock(event.getFrom(), to)) {
            Player player = event.getPlayer();
            Bukkit.getScheduler().runTaskAsynchronously(this,
                    () -> this.refreshImages(player, to));
        }
    }

    // TODO figure out why it's so slow after the command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;
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
                synchronized (this.images) {
                    this.images.add(customImage);
                }

            } catch (IOException e) {
                Logger.severe(e);
            } catch (Exception e) {
                Logger.handle(e, player::sendMessage, true);
            }
        });

        return true;
    }

    @Override
    public void onLoad() {
        Logger.initialize(this.getLogger());
        imagesDirectory = this.getDataFolder();
        LegacyImage.dataFolder = imagesDirectory;
    }

    @Override
    public void onDisable() {
        dataManager.saveAll(this.images);
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

        File legacyDataFile = new File(imagesDirectory, "data.yml");
        if (legacyDataFile.exists()) {
            // Legacy configuration loading
            ConfigurationSerialization.registerClass(LegacyImage.class);
            ConfigurationSerialization.registerClass(LegacyImageSection.class);
            dataManager = new LegacyDataManager(legacyDataFile, dataManager);
        }

        if (config.getBoolean("database.initialize")) {
            dataManager.initialize();
        }

        this.images.addAll(dataManager.load());
    }

    private void refreshImages(Player player, Location location) {

        synchronized (this.images) {

            Logger.info("Images {}", this.images.size());
            for (CustomImage image : this.images) {
                Logger.info("Refreshing this");
                image.refresh(player, location);
            }
        }
    }
}
