package com.andavin.images.data;

import com.andavin.images.image.CustomImage;
import com.andavin.images.legacy.image.LegacyImage;
import com.andavin.images.legacy.image.LegacyImageSection;
import com.andavin.util.Logger;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since September 22, 2019
 * @author Andavin
 */
public class LegacyDataManager implements DataManager {

    private final File dataFile;
    private final DataManager delegate;

    public LegacyDataManager(File dataFile, DataManager delegate) {
        this.dataFile = checkNotNull(dataFile, "data file");
        this.delegate = checkNotNull(delegate, "delegate data manager");
    }

    @Override
    public void initialize() {

        this.delegate.initialize();
        if (this.dataFile.exists()) {

            FileConfiguration data = YamlConfiguration.loadConfiguration(this.dataFile);
            List<LegacyImage> images = (List<LegacyImage>) data.get("images");
            for (LegacyImage legacyImage : images) {

                File file = legacyImage.getImageFile();
                Logger.info("Importing legacy image {}...", file);
                BlockFace direction = null;
                for (Entry<Location, LegacyImageSection> entry : legacyImage.getImageSections().entrySet()) {

                    Location location = entry.getKey();
                    LegacyImageSection section = entry.getValue();
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

                    Logger.info("Image is facing {}. Importing...", direction);
                    try {
                        this.save(new CustomImage(file.getName(), legacyImage.getLocation(),
                                direction, ImageIO.read(file)));
                    } catch (IOException e) {
                        Logger.severe(e);
                    }
                } else {
                    Logger.warn("Image {} no longer exists. Skipping...", file.getName());
                }
            }

            this.dataFile.renameTo(new File(this.dataFile.getAbsolutePath() + ".old"));
        }
    }

    @Override
    public List<CustomImage> load() {
        return this.delegate.load();
    }

    @Override
    public void save(CustomImage image) {
        this.delegate.save(image);
    }

    @Override
    public void saveAll(List<CustomImage> images) {
        this.delegate.saveAll(images);
    }
}
