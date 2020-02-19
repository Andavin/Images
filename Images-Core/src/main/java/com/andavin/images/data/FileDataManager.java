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
package com.andavin.images.data;

import com.andavin.images.image.CustomImage;
import com.andavin.reflect.exception.UncheckedClassNotFoundException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @since September 21, 2019
 * @author Andavin
 */
public class FileDataManager implements DataManager {

    private final File dataFile;

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

            Files.move(dataFile.toPath(), this.dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
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
