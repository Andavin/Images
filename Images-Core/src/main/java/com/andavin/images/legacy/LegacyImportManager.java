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
import java.math.BigInteger;
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
                    CustomImage image = new CustomImage(file.getName(), "", -1, BigInteger.valueOf(-1), legacyImage.getLocation(),
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
