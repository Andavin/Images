/* Copyright (c) 2019 */
package com.andavin.images.data;

import com.andavin.images.image.CustomImage;

import java.util.List;

/**
 * A class to handle basic loading and saving of image
 * data to an implementation based database.
 * <p>
 * All methods within this class should be called with
 * the assumption that blocking operations will most likely
 * take place and therefore should be off of the main
 * game loop thread.
 *
 * @since September 21, 2019
 * @author Andavin
 */
public interface DataManager {

    /**
     * Initialize the database if it needs to be initialized.
     */
    void initialize();

    /**
     * Load and return a mutable list of all of the images
     * from the database.
     *
     * @return The loaded images.
     */
    List<CustomImage> load();

    /**
     * Save and add a single image to the database.
     *
     * @param image The image to save.
     */
    void save(CustomImage image);

    /**
     * Save all of the available images to the database.
     * <p>
     * This should be called with all of the images to
     * save as this method will be allowed to overwrite
     * all data if need be in certain implementations
     * to save these images.
     *
     * @param images All of the images to save.
     */
    void saveAll(List<CustomImage> images);

    /**
     * Delete the given custom image from the database.
     *
     * @param image The image to delete.
     */
    void delete(CustomImage image);
}
