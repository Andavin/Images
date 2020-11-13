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
package com.andavin.util;

import com.andavin.reflect.Reflection;
import com.andavin.reflect.exception.UncheckedClassNotFoundException;
import org.bukkit.Bukkit;

import static com.andavin.reflect.Reflection.findClass;

/**
 * An enum constant that represents the possibilities of each
 * version of Minecraft to date since Minecraft {@code 1.7.10}.
 * <p>
 * Each version is representative of the latest minor version
 * from within the major version. For example, {@link #v1_9}
 * is the version {@code 1.9.4} since that was the last version
 * to be released for version {@code 1.9}.
 * <p>
 * Methods provided in this class are shortcut methods that provide
 * easy use of the {@link Enum#compareTo(Enum)} method.
 * They should not be statically imported, but instead used alongside
 * a statically imported field:
 * <pre>
 *     import static com.andavin.util.MinecraftVersion.v1_12_R1;
 *
 *     MinecraftVersion.is(v1_12_R1); // If this version is 1.12.2
 *     MinecraftVersion.lessThan(v1_12_R1); // If this version is 1.11.2 or earlier
 *     MinecraftVersion.greaterThan(v1_12_R1); // If this later than 1.12.2
 *     MinecraftVersion.greaterThanOrEqual(v1_12_R1); // If this version is 1.12.2 or later
 * </pre>
 *
 * @since November 12, 2018
 * @author Andavin
 * @see Reflection
 * @see #findMcClass(String)
 * @see #findCraftClass(String)
 */
public enum MinecraftVersion {

    /**
     * The representation of the Minecraft version {@code 1.7}.
     * This is only the major version of the game while minor
     * versions can be retrieved via the {@link MinorVersion#CURRENT}.
     */
    v1_7,

    /**
     * The representation of the Minecraft version {@code 1.8}.
     * This is only the major version of the game while minor
     * versions can be retrieved via the {@link MinorVersion#CURRENT}.
     */
    v1_8,

    /**
     * The representation of the Minecraft version {@code 1.9}.
     * This is only the major version of the game while minor
     * versions can be retrieved via the {@link MinorVersion#CURRENT}.
     */
    v1_9,

    /**
     * The representation of the Minecraft version {@code 1.10}.
     * This is only the major version of the game while minor
     * versions can be retrieved via the {@link MinorVersion#CURRENT}.
     */
    v1_10,

    /**
     * The representation of the Minecraft version {@code 1.11}.
     * This is only the major version of the game while minor
     * versions can be retrieved via the {@link MinorVersion#CURRENT}.
     */
    v1_11,

    /**
     * The representation of the Minecraft version {@code 1.12}.
     * This is only the major version of the game while minor
     * versions can be retrieved via the {@link MinorVersion#CURRENT}.
     */
    v1_12,

    /**
     * The representation of the Minecraft version {@code 1.13}.
     * This is only the major version of the game while minor
     * versions can be retrieved via the {@link MinorVersion#CURRENT}.
     */
    v1_13,

    /**
     * The representation of the Minecraft version {@code 1.14}.
     * This is only the major version of the game while minor
     * versions can be retrieved via the {@link MinorVersion#CURRENT}.
     */
    v1_14,

    /**
     * The representation of the Minecraft version {@code 1.15}.
     * This is only the major version of the game while minor
     * versions can be retrieved via the {@link MinorVersion#CURRENT}.
     */
    v1_15,

    /**
     * The representation of the Minecraft version {@code 1.16}.
     * This is only the major version of the game while minor
     * versions can be retrieved via the {@link MinorVersion#CURRENT}.
     */
    v1_16,

    /**
     * The representation of the Minecraft version {@code 1.17}.
     * This is only the major version of the game while minor
     * versions can be retrieved via the {@link MinorVersion#CURRENT}.
     */
    v1_17;

    /**
     * The current {@link MinecraftVersion} of this server.
     */
    public static final MinecraftVersion CURRENT;

    static {

        String name = Bukkit.getServer().getClass().getPackage().getName();
        String versionString = name.substring("org.bukkit.craftbukkit.".length(), name.lastIndexOf("_R"));
        try {
            CURRENT = MinecraftVersion.valueOf(versionString);
        } catch (RuntimeException e) {
            throw new UnsupportedOperationException("Version " + versionString + " is not supported.", e);
        }
    }

    /**
     * The package prefix for all Minecraft server (NMS) classes.
     *
     * @see #findMcClass(String)
     */
    public static final String MINECRAFT_PREFIX = "net.minecraft.server." + CURRENT + '.';

    /**
     * The package prefix for all CraftBukkit classes.
     *
     * @see #findCraftClass(String)
     */
    public static final String CRAFTBUKKIT_PREFIX = "org.bukkit.craftbukkit." + CURRENT + '.';

    private static final String FULL_VERSION = CURRENT.name() + '_' + MinorVersion.CURRENT;

    /**
     * Tell if the {@link #CURRENT current server
     * version} is the specified version.
     * <p>
     * For example, if {@code MinecraftVersion.is(v1_8_R3)} is
     * {@code true}, then the current server version is {@code 1.8.8}.
     *
     * @param version The version to test the current version against.
     * @return If the version is the same as the current version.
     */
    public static boolean is(MinecraftVersion version) {
        return CURRENT == version;
    }

    /**
     * Tell if the specified server version is less than (i.e. older
     * than) the {@link #CURRENT current server version}.
     * <p>
     * For example, if {@code MinecraftVersion.lessThan(v1_10_R1)} is
     * {@code true}, then the current server version is {@code 1.9.4}
     * or an earlier version.
     *
     * @param version The version to test the current version against.
     * @return If the version is less than the current version.
     */
    public static boolean lessThan(MinecraftVersion version) {
        return CURRENT.ordinal() < version.ordinal();
    }

    /**
     * Tell if the specified server version is greater than (i.e. newer
     * than) the {@link #CURRENT current server version}.
     * <p>
     * For example, if {@code MinecraftVersion.greaterThan(v1_8_R3)} is
     * {@code true}, then the current server version is {@code 1.9.4}
     * or a more recent version (usually meaning version compatibility
     * with {@code 1.9.4}).
     *
     * @param version The version to test the current version against.
     * @return If the version is greater than the current version.
     */
    public static boolean greaterThan(MinecraftVersion version) {
        return CURRENT.ordinal() > version.ordinal();
    }

    /**
     * Tell if the specified server version is greater than or equal to
     * (i.e. newer than or the same as) the {@link #CURRENT
     * current server version}.
     * <p>
     * For example, if {@code MinecraftVersion.greaterThanOrEqual(v1_8_R3)}
     * is {@code true}, then the current server version is {@code 1.8.8}
     * or a more recent version (usually meaning version compatibility
     * with {@code 1.8.8}).
     *
     * @param version The version to test the current version against.
     * @return If the version is greater than or equal to the current version.
     */
    public static boolean greaterThanOrEqual(MinecraftVersion version) {
        return CURRENT.ordinal() >= version.ordinal();
    }

    /**
     * Get a class in any NMS package omitting the
     * beginning of the canonical name and enter anything
     * following the version package.
     * <p>
     * For example, to get <b>net.minecraft.server.version.PacketPlayOutChat</b>
     * simply input <b>PacketPlayOutChat</b> omitting the
     * <b>net.minecraft.server.version</b>.
     *
     * @param name The name of the class to retrieve.
     * @return The Minecraft class for the given name.
     * @throws UncheckedClassNotFoundException If the class was not found
     *                                         or an exception occurred while
     *                                         loading the class.
     */
    public static Class<?> findMcClass(String name) throws UncheckedClassNotFoundException {
        return findClass(MINECRAFT_PREFIX + name);
    }

    /**
     * Get a class in any Craftbukkit package omitting the
     * beginning of the canonical name and enter anything
     * following the version package.
     * <p>
     * For example, to get <b>org.bukkit.craftbukkit.version.CraftServer</b>
     * simply input <b>CraftServer</b> omitting the
     * <b>org.bukkit.craftbukkit.version</b>. In addition, in order
     * get <b>org.bukkit.craftbukkit.version.entity.CraftPlayer</b>
     * simply input <b>entity.CraftPlayer</b>.
     *
     * @param name The name of the class to retrieve.
     * @return The Craftbukkit class for the given name.
     * @throws UncheckedClassNotFoundException If the class was not found
     *                                         or an exception occurred while
     *                                         loading the class.
     */
    public static Class<?> findCraftClass(String name) throws UncheckedClassNotFoundException {
        return findClass(CRAFTBUKKIT_PREFIX + name);
    }

    @Override
    public String toString() {
        return FULL_VERSION;
    }

    /**
     * A class that represents the minor version for the Bukkit
     * version barrier. For example, Minecraft {@code 1.7.10} is
     * {@link #v1_7} for the major version and {@link #R4} for the
     * minor making {@code v1_7_R4}.
     * <p>
     * {@link Enum#compareTo(Enum)} can be used to determine the exact
     * minor version if or when that may be needed using the
     * {@link #CURRENT} field to compare against.
     */
    public enum MinorVersion {

        R1, R2, R3, R4, R5;

        /**
         * The current {@link MinorVersion} of this server.
         */
        public static final MinorVersion CURRENT;

        static {

            String name = Bukkit.getServer().getClass().getPackage().getName();
            String versionString = name.substring(name.indexOf('R'));
            try {
                CURRENT = MinorVersion.valueOf(versionString);
            } catch (RuntimeException e) {
                throw new UnsupportedOperationException("Minor version " + versionString + " is not supported.", e);
            }
        }
    }
}
