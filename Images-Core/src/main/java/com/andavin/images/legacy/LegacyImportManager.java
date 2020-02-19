/* Copyright (c) 2019 */
package com.andavin.images.legacy;

import com.andavin.images.data.DataManager;
import com.andavin.images.image.CustomImage;
import com.andavin.images.legacy.image.LegacyImage;
import com.andavin.images.legacy.image.LegacyImageSection;
import com.andavin.util.Logger;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * A manager to help in importing the legacy format of
 * images to the new format.
 *
 * @since September 23, 2019
 * @author Andavin
 */
public final class LegacyImportManager {

    /**
     * Start importing all of the {@link LegacyImage legacy images}
     * to the current format of images ({@link CustomImage}) and
     * save them with the current {@link DataManager}.
     *
     * @param imageFolder The folder that contains the legacy data
     *                   file ({@code data.yml}).
     * @param dataManager The {@link DataManager} to save the images to.
     * @return The {@link CustomImage}s that were imported.
     * @throws IllegalStateException If there is no data file that could be found.
     */
    public static List<CustomImage> importImages(File imageFolder, DataManager dataManager) throws IllegalStateException {

        File dataFile = new File(imageFolder, "data.yml");
        if (!dataFile.exists()) {
            throw new IllegalStateException("§cNo legacy data file exists.\n" +
                    "§aThe§f data.yml§c file must be in the Images directory");
        }

        LegacyImage.dataFolder = imageFolder;
        ConfigurationSerialization.registerClass(LegacyImage.class);
        ConfigurationSerialization.registerClass(LegacyImageSection.class);

        FileConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
        List<LegacyImage> images = (List<LegacyImage>) data.get("images");
        List<CustomImage> importedImages = new ArrayList<>(images.size());
        for (LegacyImage legacyImage : images) {

            File file = legacyImage.getImageFile();
            BlockFace direction = null;
            for (Entry<Location, LegacyImageSection> entry : legacyImage.getImageSections().entrySet()) {

                Location location = entry.getKey();
                LegacyImageSection section = entry.getValue();
                location.getWorld().loadChunk(location.getBlockX() >> 4, location.getBlockZ() >> 4);
                for (Entity entity : location.getWorld().getNearbyEntities(location, 1, 1, 1)) {

                    if (entity instanceof ItemFrame) {

                        Location loc = entity.getLocation();
                        if (loc.getBlockX() == location.getBlockX() &&
                                loc.getBlockY() == location.getBlockY() &&
                                loc.getBlockZ() == location.getBlockZ()) {

                            // Destroy the image
                            entity.remove();
                            if (direction == null) {
                                direction = ((ItemFrame) entity).getFacing();
                            }
                        }
                    }
                }
            }

            if (direction == null) {
                Logger.warn("Image has an unknown direction and cannot be imported.");
                continue;
            }

            if (file.exists()) {

                Logger.info("Importing legacy image {} facing {}...", file.getName(), direction);
                try {
                    CustomImage image = new CustomImage(file.getName(), legacyImage.getLocation(),
                            direction, ImageIO.read(file));
                    dataManager.save(image);
                    importedImages.add(image);
                } catch (IOException e) {
                    Logger.severe(e);
                }
            } else {
                Logger.warn("Image {} no longer exists. Skipping...", file.getName());
            }
        }

        return importedImages;
    }
}
