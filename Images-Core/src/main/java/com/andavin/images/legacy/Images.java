package com.andavin.images.legacy;

import com.andavin.images.legacy.command.CmdRegistry;
import com.andavin.images.legacy.image.LegacyImage;
import com.andavin.images.legacy.image.LegacyImageSection;
import com.andavin.images.legacy.util.Reflection;
import com.andavin.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("ConstantConditions")
public final class Images extends JavaPlugin {

    private static final int PIXELS_PER_FRAME = 128;
    private static final String FILE = "data.yml", PATH = "images";
    public static final String PNG = ".png", JPEG = ".jpeg", JPG = ".jpg";
    public static final String[] EXTENSIONS = { PNG, JPEG, JPG };

    private static Field mapId;
    private static Method viewId, getMap;

    private static Images instance;
    private static final List<LegacyImage> images = new ArrayList<>();

    /**
     * Get an image by a location. This will search all of the
     * {@link LegacyImageSection}s of the image as well.
     *
     * @param loc The location of the image or one of its sections.
     * @return The image at the location.
     */
    public static LegacyImage getImage(Location loc) {

        for (LegacyImage image : Images.images) {

            if (image.getSection(loc) != null) {
                return image;
            }
        }

        return null;
    }

    /**
     * Get the instance of this plugin.
     *
     * @return The instance.
     */
    public static Images getInstance() {
        return Images.instance;
    }

    /**
     * Get an image file with the given name in the data
     * folder of this plugin.
     *
     * @param image The name of the file.
     * @return The file (may not exist) with the name.
     */
    public static File getImage(String image) {
        return new File(Images.instance.getDataFolder(), image);
    }

    /**
     * Create a new image fully in the world and store/save it.
     *
     * @param imageFile The file that is storing the actual image.
     * @param block The block to start the image at.
     * @param face The block face that was clicked.
     * @return The newly created image or <tt>null</tt> if the creation failed.
     */
    static LegacyImage createAndStoreImage(File imageFile, Block block, BlockFace face) {

        if (face == BlockFace.DOWN || face == BlockFace.UP) {
            return null;
        }

        try {

            BufferedImage image = ImageIO.read(imageFile);
            if (image.getWidth() < PIXELS_PER_FRAME || image.getHeight() < PIXELS_PER_FRAME) {
                throw new IllegalArgumentException("§cThe image §l" + imageFile.getName() + "§c is too small! Must be at least 128x128 pixels.");
            }

            int xSections = image.getWidth() / PIXELS_PER_FRAME;
            int ySections = image.getHeight() / PIXELS_PER_FRAME;
            image = Images.resize(image, xSections, ySections);

            Location loc = block.getLocation();
            LegacyImage newImage = new LegacyImage(loc, imageFile);
            World world = block.getWorld();
            for (int x = 0; x < xSections; x++) {

                for (int y = 0; y < ySections; y++) {

                    // Place an item frame on the wall and put a map in it
                    // with the data of our image section
                    Location relLoc = loc.clone();
                    Images.addRelative(relLoc, face, x, y);
                    ItemFrame frame;

                    try {
                        frame = loc.getWorld().spawn(relLoc, ItemFrame.class);
                    } catch (IllegalArgumentException e) {
                        Logger.warn("Attempted to place an image where there wasn't space for it.");
                        newImage.destroy();
                        return null;
                    }


                    // Create a new map and clear it's renderers
                    MapView view = Bukkit.createMap(world);
                    view.getRenderers().forEach(view::removeRenderer); // No worry for concurrent modifications
                    BufferedImage section = image.getSubimage(x * PIXELS_PER_FRAME, y * PIXELS_PER_FRAME,
                            PIXELS_PER_FRAME, PIXELS_PER_FRAME);

                    // Add the new renderer for the single section
                    view.addRenderer(new MapRenderer() {

                        private boolean rendered;

                        @Override
                        public void render(MapView map, MapCanvas canvas, Player player) {

                            if (!this.rendered) {
                                canvas.drawImage(0, 0, section);
                                this.rendered = true;
                            }
                        }
                    });

                    if (viewId == null) {
                        viewId = Reflection.getMethod(MapView.class, "getId");
                    }

                    Number viewId = Reflection.invokeMethod(Images.viewId, view);
                    ItemStack item = new ItemStack(Material.MAP, 1, viewId.shortValue());
                    if (Reflection.VERSION_NUMBER >= 1131) { // Check for 1.13 or greater

                        if (mapId == null) {
                            mapId = Reflection.getField(Reflection.getCraftClass("inventory.CraftMetaMap"), "mapId");
                        }

                        ItemMeta meta = item.getItemMeta();
                        Reflection.setValue(mapId, meta, viewId);
                        item.setItemMeta(meta);
                    }

                    frame.setItem(item);
                    newImage.addSection(viewId.shortValue(), x, y, relLoc);
                }
            }

            Images.images.add(newImage);
            return newImage;
        } catch (IOException e) {
            Logger.severe(e);
            throw new IllegalArgumentException("§cCould not read that image file! Check that it is a picture.");
        }
    }

    /**
     * Get all the files that are images in the images
     * data folder.
     *
     * @return The image files.
     */
    public static List<File> getImageFiles() {

        File file = Images.instance.getDataFolder();
        File[] files = file.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }

        List<File> images = new ArrayList<>(files.length);
        for (File file1 : files) {

            String path = file1.getPath();
            for (String extension : EXTENSIONS) {

                if (path.endsWith(extension)) {
                    images.add(file1);
                    break;
                }
            }
        }

        return images;
    }

    /**
     * Save all of the images to the data storage so
     * they can be loaded again on startup.
     */
    public static void save() throws IOException {

        File dataFolder = Images.instance.getDataFolder();
        File dataFile = new File(dataFolder, FILE);
        if (!dataFile.exists()) {

            dataFile.getParentFile().mkdirs();
            if (!dataFile.createNewFile()) {
                throw new IOException("Failed to create data file!");
            }
        }

        // Load the data file and save all the images to it
        // The images will be serialized during the save process
        FileConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
        data.set(PATH, Images.images);
        data.save(dataFile);
    }

    /**
     * Delete the given image and destroy all of it's sections.
     *
     * @param player The player that is deleting the image.
     * @param image The image to delete.
     */
    static void delete(Player player, LegacyImage image) {

        Bukkit.getScheduler().runTaskAsynchronously(Images.instance, () -> {

            try {
                player.sendMessage("§aDestroying...");
                image.destroy();
                player.sendMessage("§aDestroyed.");
                player.sendMessage("§aRemoving and saving...");
                Images.images.remove(image);
                Images.save();
                player.sendMessage("§aFinished!");
            } catch (IOException e) {
                player.sendMessage("§cAn issue occurred and the deletion process could not complete properly.");
                Logger.severe(e);
            }
        });
    }

    @Override
    public void onEnable() {

        Images.instance = this;
        CmdRegistry.registerCommands();
        this.getServer().getPluginManager().registerEvents(new ImageListener(), this);
        ConfigurationSerialization.registerClass(LegacyImage.class);
        ConfigurationSerialization.registerClass(LegacyImageSection.class);

        File folder = this.getDataFolder();
        if (folder.exists()) {

            File dataFile = new File(folder, FILE);
            if (dataFile.exists()) {
                // Load all the images in
                Logger.info("Loading images...");
                FileConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
                List<LegacyImage> images = (List<LegacyImage>) data.get(PATH);
                images.forEach(this::load);
                Logger.info("Loaded {} images.", Images.images.size());
            }
        } else {
            folder.mkdirs();
        }
    }

    private void load(LegacyImage loadedImage) {

        Logger.debug("Loading {}...", loadedImage.getImageFile().getName());
        if (!loadedImage.getImageFile().exists()) {
            Logger.warn("The file {} no longer exists and can't be loaded.", loadedImage.getImageFile().getName());
            return;
        }

        try {

            Logger.debug("Reading the file...");
            BufferedImage image = ImageIO.read(loadedImage.getImageFile());
            int xSections = image.getWidth() / PIXELS_PER_FRAME;
            int ySections = image.getHeight() / PIXELS_PER_FRAME;
            BufferedImage finalImage = Images.resize(image, xSections, ySections);

            Images.images.add(loadedImage);
            AtomicBoolean save = new AtomicBoolean();
            loadedImage.getSections().removeIf(section -> {

                Logger.debug("Getting the map with ID {}.", section.getId());
                if (getMap == null) {
                    getMap = Reflection.getMethod(Bukkit.class, "getMap",
                            Reflection.VERSION_NUMBER < 1132 ? short.class : int.class);
                }

                MapView view = Reflection.invokeMethod(getMap, null, section.getId());
                if (view == null) {
                    Logger.warn("Map with ID {} is no longer available for rendering. Skipping...", section.getId());
                    save.set(true);
                    return true;
                }

                BufferedImage imageSection = finalImage.getSubimage(section.getX() * PIXELS_PER_FRAME,
                        section.getY() * PIXELS_PER_FRAME, PIXELS_PER_FRAME, PIXELS_PER_FRAME);

                view.getRenderers().forEach(view::removeRenderer);
                view.addRenderer(new MapRenderer() {

                    private boolean rendered;

                    @Override
                    public void render(MapView map, MapCanvas canvas, Player player) {

                        if (!this.rendered) {
                            canvas.drawImage(0, 0, imageSection);
                            this.rendered = true;
                        }
                    }
                });

                return false;
            });

            if (save.get()) {
                Images.save();
            }
        } catch (IOException e) {
            Logger.severe(e, "Failed to load images.");
        }
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
                loc.subtract(x, y, 0);
                break;

            case SOUTH:
                loc.add(x, -y, 0);
                break;

            case EAST:
                loc.add(0, -y, -x);
                break;

            case WEST:
                loc.subtract(0, y, -x);
        }
    }
}
