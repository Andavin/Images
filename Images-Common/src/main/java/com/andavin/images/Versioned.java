/*
 * MIT License
 *
 * Copyright (c) 2018 Andavin
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

/* Copyright (c) 2019 */
package com.andavin.images;

import com.andavin.reflect.exception.UncheckedClassNotFoundException;
import com.andavin.util.MinecraftVersion;

import static com.andavin.reflect.Reflection.findClass;
import static com.andavin.reflect.Reflection.newInstance;

/**
 * A simple marker interface to mark a class as a
 * versioned type.
 *
 * @since November 13, 2018
 * @author Andavin
 */
public interface Versioned {

    String PACKAGE = Versioned.class.getPackage().getName();
    String VERSION_PREFIX = PACKAGE + '.' + MinecraftVersion.CURRENT;

    /**
     * Get an instance of a versioned class.
     *
     * @param clazz The class to get the versioned counterpart for.
     * @param args The arguments to pass to the constructor.
     * @param <T> The type of class to retrieve.
     * @return The instance of the versioned type.
     * @throws UnsupportedOperationException If the class is not found (no supported).
     */
    static <T extends Versioned> T getInstance(Class<T> clazz, Object... args) throws UnsupportedOperationException {

        Class<T> found;
        try {
            String name = clazz.getName();
            found = findClass(VERSION_PREFIX + name.substring(name.lastIndexOf('.')));
        } catch (UncheckedClassNotFoundException e) {
            throw new UnsupportedOperationException("Class " + clazz +
                    " is not currently supported for version " + MinecraftVersion.CURRENT);
        }

        return newInstance(found, args);
    }
}
