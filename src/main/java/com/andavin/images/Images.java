package com.andavin.images;

import com.andavin.images.command.CmdRegistry;
import com.andavin.images.image.Image;
import com.andavin.images.image.ImageSection;
import com.andavin.images.util.Logger;
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
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings({ "unchecked", "ResultOfMethodCallIgnored", "deprecation" })
public final class Images extends JavaPlugin {

    private static final int PIXELS_PER_FRAME = 128;
    private static final String FILE = "data.yml", PATH = "images";
    public static final String PNG = ".png", JPEG = ".jpeg", JPG = ".jpg";
    public static final String[] EXTENSIONS = { PNG, JPEG, JPG };

    private static Images instance;
    private static List<Image> images = new LinkedList<>();

    @Override
    public void onEnable() {

        Images.instance = this;
        CmdRegistry.registerCommands();
        this.getServer().getPluginManager().registerEvents(new ImageListener(), this);
        ConfigurationSerialization.registerClass(Image.class);
        ConfigurationSerialization.registerClass(ImageSection.class);

        final File dataFile = new File(this.getDataFolder(), FILE);
        if (dataFile.exists()) {

            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {

                // Load all the images in
                Logger.info("Loading images...");
                final FileConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
                final List<Image> images = (List<Image>) data.get(PATH);
                images.forEach(this::load);
                Logger.info("Loaded {} images.", Images.images.size());
            });
        }
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
    @Nonnull
    public static File getImage(final String image) {
        return new File(Images.instance.getDataFolder(), image);
    }

    /**
     * Get an image by a location. This will search all of the
     * {@link ImageSection}s of the image as well.
     *
     * @param loc The location of the image or one of its sections.
     * @return The image at the location.
     */
    @Nullable
    public static Image getImage(final Location loc) {

        for (final Image image : Images.images) {

            if (image.getSection(loc) != null) {
                return image;
            }
        }

        return null;
    }

    /**
     * Get all the files that are images in the images
     * data folder.
     *
     * @return The image files.
     */
    @Nonnull
    public static List<File> getImageFiles() {

        final File file = Images.instance.getDataFolder();
        final File[] files = file.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }

        final List<File> images = new ArrayList<>(files.length);
        for (final File file1 : files) {

            final String path = file1.getPath();
            for (final String extension : EXTENSIONS) {

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

        final File dataFolder = Images.instance.getDataFolder();
        final File dataFile = new File(dataFolder, FILE);
        if (!dataFile.exists()) {

            dataFile.getParentFile().mkdirs();
            if (!dataFile.createNewFile()) {
                throw new IOException("Failed to create data file!");
            }
        }

        // Load the data file and save all the images to it
        // The images will be serialized during the save process
        final FileConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
        data.set(PATH, Images.images);
        data.save(dataFile);
    }

    /**
     * Create a new image fully in the world and store/save it.
     *
     * @param imageFile The file that is storing the actual image.
     * @param block The block to start the image at.
     * @param face The block face that was clicked.
     * @return The newly created image or <tt>null</tt> if the creation failed.
     */
    @Nullable
    static Image createAndStoreImage(final File imageFile, final Block block, final BlockFace face) {

        if (face == BlockFace.DOWN || face == BlockFace.UP) {
            return null;
        }

        try {

            BufferedImage image = ImageIO.read(imageFile);
            final int xSections = image.getWidth() / PIXELS_PER_FRAME;
            final int ySections = image.getHeight() / PIXELS_PER_FRAME;
            image = Images.resize(image, xSections, ySections);

            final Location loc = block.getLocation();
            final Image newImage = new Image(loc, imageFile);
            final World world = block.getWorld();
            for (int x = 0; x < xSections; x++) {

                for (int y = 0; y < ySections; y++) {

                    // Place an item frame on the wall and put a map in it
                    // with the data of our image section
                    final Location relLoc = loc.clone();
                    Images.addRelative(relLoc, face, x, y);
                    final ItemFrame frame;

                    try {
                        frame = loc.getWorld().spawn(relLoc, ItemFrame.class);
                    } catch (IllegalArgumentException e) {
                        Logger.warn("Attempted to place an image where there wasn't space for it.");
                        newImage.destroy();
                        return null;
                    }


                    // Create a new map and clear it's renderers
                    final MapView view = Bukkit.createMap(world);
                    view.getRenderers().forEach(view::removeRenderer); // No worry for concurrent modifications
                    final BufferedImage section = image.getSubimage(x * PIXELS_PER_FRAME, y * PIXELS_PER_FRAME,
                            PIXELS_PER_FRAME, PIXELS_PER_FRAME);

                    // Add the new renderer for the single section
                    view.addRenderer(new MapRenderer() {

                        private boolean rendered;

                        @Override
                        public void render(final MapView map, final MapCanvas canvas, final Player player) {

                            if (!this.rendered) {
                                canvas.drawImage(0, 0, section);
                                this.rendered = true;
                            }
                        }
                    });

                    frame.setItem(new ItemStack(Material.MAP, 1, view.getId()));
                    newImage.addSection(view.getId(), x, y, relLoc);
                }
            }

            Images.images.add(newImage);
            return newImage;
        } catch (IOException e) {
            Logger.severe(e);
            return null;
        }
    }

    /**
     * Delete the given image and destroy all of it's sections.
     *
     * @param player The player that is deleting the image.
     * @param image The image to delete.
     */
    static void delete(final Player player, final Image image) {

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

    private void load(final Image loadedImage) {

        Logger.debug("Loading {}...", loadedImage.getImageFile().getName());
        if (!loadedImage.getImageFile().exists()) {
            Logger.warn("The file {} no longer exists and can't be loaded.", loadedImage.getImageFile().getName());
            return;
        }

        try {

            Logger.debug("Reading the file...");
            BufferedImage image = ImageIO.read(loadedImage.getImageFile());
            final int xSections = image.getWidth() / PIXELS_PER_FRAME;
            final int ySections = image.getHeight() / PIXELS_PER_FRAME;
            final BufferedImage finalImage = Images.resize(image, xSections, ySections);

            Images.images.add(loadedImage);
            final AtomicBoolean save = new AtomicBoolean();
            loadedImage.getSections().removeIf(section -> {

                Logger.debug("Getting the map with ID {}.", section.getId());
                final MapView view = Bukkit.getMap(section.getId());
                if (view == null) {
                    Logger.warn("Map with ID {} is no longer available for rendering. Skipping...", section.getId());
                    save.set(true);
                    return true;
                }

                final BufferedImage imageSection = finalImage.getSubimage(section.getX() * PIXELS_PER_FRAME,
                        section.getY() * PIXELS_PER_FRAME, PIXELS_PER_FRAME, PIXELS_PER_FRAME);

                view.getRenderers().forEach(view::removeRenderer);
                view.addRenderer(new MapRenderer() {

                    private boolean rendered;

                    @Override
                    public void render(final MapView map, final MapCanvas canvas, final Player player) {

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

    private static BufferedImage resize(BufferedImage image, final int xSections, final int ySections) {

        Logger.debug("Resizing a file...");
        if (image.getWidth() % PIXELS_PER_FRAME != 0 || image.getHeight() % PIXELS_PER_FRAME != 0) {

            // Get a scaled version of the image
            Logger.debug("The file was an incorrect size and need to be resized!");
            Logger.debug("Getting scaled image...");
            final java.awt.Image img = image.getScaledInstance(xSections * PIXELS_PER_FRAME, ySections * PIXELS_PER_FRAME, 1);
            image = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Logger.debug("Copying scaled image to a new BufferedImage...");

            // Copy the image over to the new instance
            final Graphics2D g2D = image.createGraphics();
            g2D.drawImage(img, 0, 0, null);
            g2D.dispose();
        }

        Logger.debug("Finished resizing.");
        return image;
    }

    private static void addRelative(final Location loc, final BlockFace face, final int x, final int y) {

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
