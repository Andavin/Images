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

package com.andavin.util;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import static com.google.common.base.Preconditions.checkArgument;

public final class LocationUtil {

    private static final BlockFace[] LEFT_ROTATION, RIGHT_ROTATION;
    private static final BlockFace[] CARDINAL = {
            BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST
    };

    private static final BlockFace[] DIAGONAL = {
            BlockFace.SOUTH, BlockFace.SOUTH_WEST,
            BlockFace.WEST, BlockFace.NORTH_WEST,
            BlockFace.NORTH, BlockFace.NORTH_EAST,
            BlockFace.EAST, BlockFace.SOUTH_EAST
    };

    static {

        BlockFace[] values = BlockFace.values();
        LEFT_ROTATION = new BlockFace[values.length];
        LEFT_ROTATION[BlockFace.SELF.ordinal()] = BlockFace.SELF;
        LEFT_ROTATION[BlockFace.UP.ordinal()] = BlockFace.UP;
        LEFT_ROTATION[BlockFace.DOWN.ordinal()] = BlockFace.DOWN;
        LEFT_ROTATION[BlockFace.NORTH.ordinal()] = BlockFace.NORTH_NORTH_WEST;
        LEFT_ROTATION[BlockFace.NORTH_NORTH_WEST.ordinal()] = BlockFace.NORTH_WEST;
        LEFT_ROTATION[BlockFace.NORTH_WEST.ordinal()] = BlockFace.WEST_NORTH_WEST;
        LEFT_ROTATION[BlockFace.WEST_NORTH_WEST.ordinal()] = BlockFace.WEST;
        LEFT_ROTATION[BlockFace.WEST.ordinal()] = BlockFace.WEST_SOUTH_WEST;
        LEFT_ROTATION[BlockFace.WEST_SOUTH_WEST.ordinal()] = BlockFace.SOUTH_WEST;
        LEFT_ROTATION[BlockFace.SOUTH_WEST.ordinal()] = BlockFace.SOUTH_SOUTH_WEST;
        LEFT_ROTATION[BlockFace.SOUTH_SOUTH_WEST.ordinal()] = BlockFace.SOUTH;
        LEFT_ROTATION[BlockFace.SOUTH.ordinal()] = BlockFace.SOUTH_SOUTH_EAST;
        LEFT_ROTATION[BlockFace.SOUTH_SOUTH_EAST.ordinal()] = BlockFace.SOUTH_EAST;
        LEFT_ROTATION[BlockFace.SOUTH_EAST.ordinal()] = BlockFace.EAST_SOUTH_EAST;
        LEFT_ROTATION[BlockFace.EAST_SOUTH_EAST.ordinal()] = BlockFace.EAST;
        LEFT_ROTATION[BlockFace.EAST.ordinal()] = BlockFace.EAST_NORTH_EAST;
        LEFT_ROTATION[BlockFace.EAST_NORTH_EAST.ordinal()] = BlockFace.NORTH_EAST;
        LEFT_ROTATION[BlockFace.NORTH_EAST.ordinal()] = BlockFace.NORTH_NORTH_EAST;
        LEFT_ROTATION[BlockFace.NORTH_NORTH_EAST.ordinal()] = BlockFace.NORTH;

        RIGHT_ROTATION = new BlockFace[values.length];
        RIGHT_ROTATION[BlockFace.SELF.ordinal()] = BlockFace.SELF;
        RIGHT_ROTATION[BlockFace.UP.ordinal()] = BlockFace.UP;
        RIGHT_ROTATION[BlockFace.DOWN.ordinal()] = BlockFace.DOWN;
        RIGHT_ROTATION[BlockFace.NORTH.ordinal()] = BlockFace.NORTH_NORTH_EAST;
        RIGHT_ROTATION[BlockFace.NORTH_NORTH_EAST.ordinal()] = BlockFace.NORTH_EAST;
        RIGHT_ROTATION[BlockFace.NORTH_EAST.ordinal()] = BlockFace.EAST_NORTH_EAST;
        RIGHT_ROTATION[BlockFace.EAST_NORTH_EAST.ordinal()] = BlockFace.EAST;
        RIGHT_ROTATION[BlockFace.EAST.ordinal()] = BlockFace.EAST_SOUTH_EAST;
        RIGHT_ROTATION[BlockFace.EAST_SOUTH_EAST.ordinal()] = BlockFace.SOUTH_EAST;
        RIGHT_ROTATION[BlockFace.SOUTH_EAST.ordinal()] = BlockFace.SOUTH_SOUTH_EAST;
        RIGHT_ROTATION[BlockFace.SOUTH_SOUTH_EAST.ordinal()] = BlockFace.SOUTH;
        RIGHT_ROTATION[BlockFace.SOUTH.ordinal()] = BlockFace.SOUTH_SOUTH_WEST;
        RIGHT_ROTATION[BlockFace.SOUTH_SOUTH_WEST.ordinal()] = BlockFace.SOUTH_WEST;
        RIGHT_ROTATION[BlockFace.SOUTH_WEST.ordinal()] = BlockFace.WEST_SOUTH_WEST;
        RIGHT_ROTATION[BlockFace.WEST_SOUTH_WEST.ordinal()] = BlockFace.WEST;
        RIGHT_ROTATION[BlockFace.WEST.ordinal()] = BlockFace.WEST_NORTH_WEST;
        RIGHT_ROTATION[BlockFace.WEST_NORTH_WEST.ordinal()] = BlockFace.NORTH_WEST;
        RIGHT_ROTATION[BlockFace.NORTH_WEST.ordinal()] = BlockFace.NORTH_NORTH_WEST;
        RIGHT_ROTATION[BlockFace.NORTH_NORTH_WEST.ordinal()] = BlockFace.NORTH;
    }

    /**
     * Get a copy of the given location that is at the center
     * of the block that the location is on.
     *
     * @param loc The location to get the block location from.
     * @return The centered location.
     */
    public static Location center(Location loc) {
        return new Location(loc.getWorld(),
                loc.getBlockX() + 0.5,
                loc.getBlockY() + 0.5,
                loc.getBlockZ() + 0.5,
                loc.getYaw(),
                loc.getPitch());
    }

    /**
     * Tell if the given locations are on the same block.
     *
     * @param loc1 The first location in question.
     * @param loc2 The second location in question.
     * @return If the location are on the same block.
     */
    public static boolean isSameBlock(Location loc1, Location loc2) {
        return loc1.getWorld().equals(loc2.getWorld()) && loc1.getBlockX() == loc2.getBlockX()
                && loc1.getBlockY() == loc2.getBlockY() && loc1.getBlockZ() == loc2.getBlockZ();
    }

    /**
     * Tell if the chunk that holds the given location is
     * currently loaded.
     *
     * @param loc The location in question.
     * @return If the location's chunk is loaded.
     */
    public static boolean isChunkLoaded(Location loc) {
        return loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }

    /**
     * Get the {@link BlockFace direction} that the location is facing.
     * This will include the main 4 directions and their diagonal
     * counterparts as well:
     * <ul>
     *     <li>{@link BlockFace#NORTH}</li>
     *     <li>{@link BlockFace#NORTH_EAST}</li>
     *     <li>{@link BlockFace#EAST}</li>
     *     <li>{@link BlockFace#SOUTH_EAST}</li>
     *     <li>{@link BlockFace#SOUTH}</li>
     *     <li>{@link BlockFace#SOUTH_WEST}</li>
     *     <li>{@link BlockFace#WEST}</li>
     *     <li>{@link BlockFace#NORTH_WEST}</li>
     * </ul>
     * It does not include {@link BlockFace#UP} or {@link BlockFace#DOWN}
     * or any diagonal direction.
     * <p>
     * This is equivalent to:
     * <pre>
     *     LocationUtil.getDirection(loc, true, false);</pre>
     *
     * @param loc The location to get the direction from.
     * @return The direction that the location is facing.
     * @see #getDirection(Location, boolean, boolean)
     */
    public static BlockFace getDirection(Location loc) {
        return getDirection(loc, true, false);
    }

    /**
     * Get the cardinal {@link BlockFace direction} that the location
     * is facing. Cardinal direction includes the 4 normal directions:
     * <ul>
     *     <li>{@link BlockFace#NORTH}</li>
     *     <li>{@link BlockFace#SOUTH}</li>
     *     <li>{@link BlockFace#EAST}</li>
     *     <li>{@link BlockFace#WEST}</li>
     * </ul>
     * It does not include {@link BlockFace#UP} or {@link BlockFace#DOWN}
     * or any diagonal direction.
     * <p>
     * This is equivalent to:
     * <pre>
     *     LocationUtil.getDirection(loc, false, false);</pre>
     *
     * @param loc The location to get the direction from.
     * @return The direction that the location is facing.
     * @see #getDirection(Location, boolean, boolean)
     */
    public static BlockFace getCardinalDirection(Location loc) {
        return getDirection(loc, false, false);
    }

    /**
     * Get the {@link BlockFace direction} that the location is facing
     * including vertical and cardinal directions directions:
     * <ul>
     *     <li>{@link BlockFace#NORTH}</li>
     *     <li>{@link BlockFace#SOUTH}</li>
     *     <li>{@link BlockFace#EAST}</li>
     *     <li>{@link BlockFace#WEST}</li>
     *     <li>{@link BlockFace#UP}</li>
     *     <li>{@link BlockFace#DOWN}</li>
     * </ul>
     * This is equivalent to:
     * <pre>
     *     LocationUtil.getDirection(loc, false, true);</pre>
     *
     * @param loc The location to get the direction from.
     * @return The direction that the location is facing.
     * @see #getDirection(Location, boolean, boolean)
     */
    public static BlockFace getVerticalDirection(Location loc) {
        return getDirection(loc, false, true);
    }

    /**
     * Get the {@link BlockFace direction} that the location is facing.
     * Optionally include directions such as {@link BlockFace#NORTH_EAST}
     * or {@link BlockFace#SOUTH_WEST} as well as {@link BlockFace#UP} or
     * {@link BlockFace#DOWN}.
     *
     * @param loc The location to get the direction off of.
     * @param diagonal If directions such as {@link BlockFace#NORTH_EAST} or
     *                 {@link BlockFace#NORTH_WEST} etc. should be included.
     * @param vertical If the {@link BlockFace#UP} and {@link BlockFace#DOWN}
     *                 directions should be included in the calculation.
     * @return The direction that the location is facing.
     */
    public static BlockFace getDirection(Location loc, boolean diagonal, boolean vertical) {

        if (vertical) {

            float pitch = loc.getPitch();
            if (pitch < -70) {
                return BlockFace.UP;
            }

            if (pitch > 70) {
                return BlockFace.DOWN;
            }
        }

        BlockFace[] directions = diagonal ? DIAGONAL : CARDINAL;
        return directions[Math.round(loc.getYaw() / (360 / directions.length)) & directions.length - 1];
    }

    /**
     * Get the difference, in degrees, between the first {@link BlockFace}
     * given and the second.
     *
     * @param from The BlockFace to measure from (0º).
     * @param to The BlockFace to measure to (degrees from 0º).
     * @return The amount of degrees that separate the two BlockFaces.
     */
    public static float getDifference(BlockFace from, BlockFace to) {

        if (from == to) {
            return 0F;
        }

        if (from.getOppositeFace() == to) {
            return 180F;
        }

        float degrees = 22.5F;
        while ((from = rotateRight(from)) != to) {
            degrees += 22.5F;
        }

        return degrees;
    }

    /**
     * Rotate the given {@link BlockFace} by the given degrees to the
     * closest 16th of a circle on the y-axis and to the closest 4th
     * of a circle on the x or z axes.
     * <p>
     * If the degrees are positive, the face will be rotated clockwise
     * and if they are negative it will be rotated counterclockwise.
     * <p>
     * Note that the rotation cannot take place on both X and Z axes.
     * Therefore, both {@code xAxis} and {@code zAxis} cannot both be
     * {@code true}. If they are, then it will be rotated on the X-axis.
     *
     * @param face The BlockFace to rotate.
     * @param degrees The amount of degrees to rotate.
     * @param xAxis If the rotation is taking place on the x-axis.
     * @param zAxis If the rotation is taking place on the z-axis.
     * @return The block face that is rotated.
     */
    public static BlockFace rotate(BlockFace face, float degrees, boolean xAxis, boolean zAxis) {

        if (face == BlockFace.SELF || degrees == 0) {
            return face;
        }

        float dRotations = degrees / 22.5F;
        int rotations = (int) dRotations;
        if (dRotations > rotations) { // If the degrees were positive
            rotations++;
        } else if (dRotations < rotations) { // If the degrees were negative
            rotations--;
        }

        rotations %= 16;
        if (xAxis || zAxis) {

            rotations /= 4;
            if (rotations == 2) {
                return face.getOppositeFace();
            }

            if (rotations == 3 || rotations == -3) {
                rotations /= -3;
            }

            boolean positive = rotations == 1;
            switch (face) {
                case UP:
                    return xAxis ? positive ? BlockFace.NORTH : BlockFace.SOUTH :
                            positive ? BlockFace.EAST : BlockFace.WEST;
                case NORTH:
                case EAST:
                    return positive ? BlockFace.DOWN : BlockFace.UP;
                case DOWN:
                    return xAxis ? positive ? BlockFace.SOUTH : BlockFace.NORTH :
                            positive ? BlockFace.WEST : BlockFace.EAST;
                case SOUTH:
                case WEST:
                    return positive ? BlockFace.UP : BlockFace.DOWN;
            }
        }

        if (degrees > 0) {
            return rotateRight(face, rotations);
        }

        if (degrees < 0) {
            return rotateLeft(face, rotations * -1);
        }

        return face;
    }

    /**
     * Rotate the given {@link BlockFace} 1/16th of a 360º rotation
     * clockwise on the y-axis. If the given BlockFace is either
     * {@link BlockFace#UP} or {@link BlockFace#DOWN}, then itself
     * will be returned as those faces cannot be rotated on the y-axis.
     *
     * @param face The face to rotate on the y-axis.
     * @return The BlockFace that has been rotated 22.5º on the y-axis.
     */
    public static BlockFace rotateRight(BlockFace face) {
        return RIGHT_ROTATION[face.ordinal()];
    }

    /**
     * Rotate the given {@link BlockFace} 1/16th of a 360º rotation
     * clockwise on the y-axis. If the given BlockFace is either
     * {@link BlockFace#UP} or {@link BlockFace#DOWN}, then itself
     * will be returned as those faces cannot be rotated on the y-axis.
     *
     * @param face The face to rotate on the y-axis.
     * @param amount The amount of 22.5º rotations to rotate the face.
     *               So {@code 4} would result in a 90º rotation.
     * @return The BlockFace that has been rotated 22.5º on the y-axis.
     */
    public static BlockFace rotateRight(BlockFace face, int amount) {

        amount %= 16; // Anything over 16 is over a full rotation
        if (amount == 0 || face == BlockFace.SELF || face == BlockFace.UP || face == BlockFace.DOWN) { // No rotation
            return face;
        }

        if (amount == 1) { // Single rotation
            return rotateRight(face);
        }

        if (amount == 8) { // Faster than iterating
            return face.getOppositeFace();
        }

        checkArgument(amount > 1, "cannot have negative rotation: " + amount);
        BlockFace rotated = face;
        for (int i = 0; i < amount; i++) {
            rotated = RIGHT_ROTATION[rotated.ordinal()];
        }

        return rotated;
    }

    /**
     * Rotate the given {@link BlockFace} 1/16th of a 360º rotation
     * counterclockwise on the y-axis. If the given BlockFace is either
     * {@link BlockFace#UP} or {@link BlockFace#DOWN}, then itself will
     * be returned as those faces cannot be rotated on the y-axis.
     *
     * @param face The face to rotate on the y-axis.
     * @return The BlockFace that has been rotated 22.5º on the y-axis.
     */
    public static BlockFace rotateLeft(BlockFace face) {
        return LEFT_ROTATION[face.ordinal()];
    }

    /**
     * Rotate the given {@link BlockFace} 1/16th of a 360º rotation
     * counterclockwise on the y-axis. If the given BlockFace is either
     * {@link BlockFace#UP} or {@link BlockFace#DOWN}, then itself will
     * be returned as those faces cannot be rotated on the y-axis.
     *
     * @param face The face to rotate on the y-axis.
     * @param amount The amount of 22.5º rotations to rotate the face.
     *               So {@code 4} would result in a 90º rotation.
     * @return The BlockFace that has been rotated 22.5º on the y-axis.
     */
    public static BlockFace rotateLeft(BlockFace face, int amount) {

        amount %= 16; // Anything over 16 is over a full rotation
        if (amount == 0 || face == BlockFace.SELF || face == BlockFace.UP || face == BlockFace.DOWN) { // No rotation
            return face;
        }

        if (amount == 1) { // Single rotation
            return rotateLeft(face);
        }

        if (amount == 8) { // Faster than iterating
            return face.getOppositeFace();
        }

        checkArgument(amount > 1, "cannot have negative rotation: " + amount);
        BlockFace rotated = face;
        for (int i = 0; i < amount; i++) {
            rotated = LEFT_ROTATION[rotated.ordinal()];
        }

        return rotated;
    }
}
