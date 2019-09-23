package com.andavin.images.data;

import com.andavin.images.image.CustomImage;
import com.andavin.reflect.exception.UncheckedClassNotFoundException;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @since September 21, 2019
 * @author Andavin
 */
public class FileDataManager implements DataManager {

    private volatile File dataFile;

    public FileDataManager(File dataFile) {
        checkArgument(!dataFile.exists() || dataFile.isFile(),
                "not a file %s", dataFile);
        this.dataFile = dataFile;
    }

    @Override
    public void initialize() {
    }

    @Override
    public synchronized List<CustomImage> load() {

        if (!this.dataFile.exists()) {
            return new ArrayList<>();
        }

        byte[] data;
        try {
            data = Files.readAllBytes(this.dataFile.toPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        if (data.length == 0) {
            return new ArrayList<>();
        }

        List<CustomImage> images;
        try (ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(data))) {

            int capacity = stream.readInt();
            images = new ArrayList<>(capacity);
            for (int i = 0; i < capacity; i++) {
                images.add((CustomImage) stream.readObject());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ClassNotFoundException e) {
            throw new UncheckedClassNotFoundException(e.getMessage(), e);
        }

        return images;
    }

    @Override
    public void save(CustomImage image) {
        // We have to read all of the images, add one to it and
        // write them all again so as to not overwrite anything
        List<CustomImage> images = this.load();
        images.add(image);
        this.saveAll(images);
    }

    @Override
    public synchronized void saveAll(List<CustomImage> images) {

        if (images.isEmpty()) {
            return;
        }

        File dataFile = new File(this.dataFile.getAbsolutePath() + ".tmp");
        try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(dataFile))) {

            stream.writeInt(images.size());
            for (CustomImage image : images) {
                stream.writeObject(image);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        if (!this.dataFile.exists() || this.dataFile.delete()) {
            dataFile.renameTo(this.dataFile);
            this.dataFile = dataFile;
        } else {
            throw new IllegalStateException("Failed to save file " + this.dataFile);
        }
    }

    @Override
    public void delete(CustomImage image) {
        // We have to read all of the images, remove one from it
        // and write them all again so as to not overwrite anything
        List<CustomImage> images = this.load();
        if (images.remove(image)) {
            this.saveAll(images);
        }
    }
}
