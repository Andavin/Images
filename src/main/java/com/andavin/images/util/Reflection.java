package com.andavin.images.util;

import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings({ "WeakerAccess", "unchecked", "unused" })
public final class Reflection {

    /**
     * The version string that makes up part of CraftBukkit or MinecraftServer imports.
     */
    public static final String VERSION_STRING = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    /**
     * The version number. 170 for 1_7_R0, 181 for 1_8_R1, etc.
     */
    public static final int VERSION_NUMBER = Integer.parseInt(VERSION_STRING.replaceAll("[v_R]", ""));

    /**
     * The prefix for all NMS packages (e.g. net.minecraft.server.version.).
     */
    public static final String NMS_PREFIX = "net.minecraft.server." + VERSION_STRING + '.';

    /**
     * The prefix for all Craftbukkit packages (e.g. org.bukkit.craftbukkit.version.).
     */
    public static final String CRAFT_PREFIX = "org.bukkit.craftbukkit." + VERSION_STRING + '.';

    /**
     * Get an instance of the specified class object optionally
     * getting the object even if the access is denied.
     * <br>
     * If the object is not accessible and access is set to false
     * or an exception is thrown this will return null.
     *
     * @param clazz The class to get an instance of.
     * @param params The parameters to pass into the constructor method.
     * @param <T> The object type to get the instance for.
     * @return A new instance of the given class or null if it is not possible.
     */
    public static <T> T getInstance(final Class<T> clazz, final Object... params) {
        final Constructor<T> con = Reflection.getConstructor(clazz, Reflection.getClassesForObjects(params));
        return con == null ? null : Reflection.getInstance(con, params);
    }

    /**
     * Get an instance of the class with the given constructor
     * using the given parameters. Optionally getting the instance
     * whether the constructor is accessible or not.
     *
     * @param con The constructor of the object class.
     * @param params The object parameters to pass to the constructor.
     * @param <T> The type of object to retrieve.
     * @return The object type of the given constructor.
     */
    public static <T> T getInstance(final Constructor<T> con, final Object... params) {

        if (!con.isAccessible()) {
            con.setAccessible(true);
        }

        try {
            return con.newInstance(params);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            Logger.severe(e);
            return null;
        }
    }

    /**
     * Set the value of the field with the given name inside of
     * the specified class and optionally in the instance of the
     * given object. If there is no field with the given name this
     * method will do nothing.
     *
     * @param clazz The class type that the field belongs to.
     * @param instance The instance of the class to set the field for.
     * @param name The name of the field to set the value of.
     * @param value The value to give the field.
     */
    public static void setValue(final Class<?> clazz, final Object instance, final String name, final Object value) {
        Reflection.setValue(Reflection.getField(clazz, name), instance, value);
    }

    /**
     * Set a value to the given field providing the object instance
     * and the value to set.
     *
     * @param field The field to set the value to.
     * @param instance The instance of the class to set the field for.
     * @param value The value to give the field.
     */
    public static void setValue(final Field field, final Object instance, final Object value) {

        if (field != null) {

            if (!field.isAccessible()) {
                field.setAccessible(true);
            }

            try {
                field.set(instance, value);
            } catch (IllegalAccessException e) {
                Logger.severe(e);
            }
        }
    }

    /**
     * Get the value of a field inside of the specified object's
     * class with the given name and casting it to the given type.
     * If the type of the field is different then the return type T
     * then a {@link ClassCastException} will be thrown.
     *
     * @param clazz The class type that the field belongs to.
     * @param instance The instance of the class to get the field for.
     * @param name The name of the field to get.
     * @param <T> The declaration type of the field.
     * @return The value of the given field or null if none exists.
     */
    public static <T> T getValue(final Class<?> clazz, final Object instance, final String name) {
        return Reflection.getValue(Reflection.getField(clazz, name), instance);
    }

    /**
     * Get the value of the given field using the given object
     * as an instance to access it.
     *
     * @param field The field to get the value of.
     * @param instance The instance of the class to get the field for.
     * @param <T> The declaration type of the field.
     * @return The value of the given field or null if none exists.
     */
    // May need to validate the generic return type T by taking a Class<T> as parameter
    public static <T> T getValue(final Field field, final Object instance) {

        if (field == null) {
            return null;
        }

        if (!field.isAccessible()) {
            field.setAccessible(true);
        }

        T value = null;
        try {

            value = (T) field.get(instance);

            // For now catch the ClassCastException
        } catch (final ClassCastException | IllegalAccessException e) {
            Logger.severe(e);
        }

        return value;
    }

    /**
     * Get the field with the specified name whether it is
     * accessible or not. If there is no field with the specified
     * name or if the field is in a parent class in the hierarchy
     * and is inaccessible then this method will return null.
     *
     * @param clazz The class the field belongs to.
     * @param name The name of the field.
     * @return The field or null if no field exists.
     */
    public static Field getField(final Class<?> clazz, final String name) {

        try {
            return clazz.getDeclaredField(name);
        } catch (final NoSuchFieldException e) {

            try {
                final Class<?> superClazz = clazz.getSuperclass();
                return superClazz == null ? null : superClazz.getField(name);
            } catch (final NoSuchFieldException e1) {
                // Do nothing just continue
            }
        }

        for (final Field field : clazz.getDeclaredFields()) {

            if (field.getName().equals(name)) {
                return field;
            }
        }

        return null;
    }

    /**
     * Invoke a method from the given class with the given name
     * and matching the types of the parameters given returning
     * the type given. If the type given is not the type of the
     * return value of the method found then a {@link ClassCastException}
     * will be thrown.
     *
     * @param clazz The class that the method belong to.
     * @param instance The instance to invoke the method on.
     * @param name The name of the method to invoke.
     * @param params The parameters to pass to the method.
     * @param <T> The method return type (if different an exception will be thrown).
     * @return The value that the method returned.
     */
    public static <T> T invokeMethod(final Class<?> clazz, final Object instance, final String name, final Object... params) {
        return Reflection.invokeMethod(Reflection.getMethod(clazz, name, Reflection.getClassesForObjects(params)), instance, params);
    }

    /**
     * Invoke the given method on the given object instance and with
     * the given parameters.
     *
     * @param method The method to invoke.
     * @param instance The instance to invoke the method on.
     * @param params The parameters to pass to the method.
     * @param <T> The method return type (if different an exception will be thrown).
     * @return The value that the method returned.
     */
    // May need to validate the generic return type T by taking a Class<T> as parameter
    public static <T> T invokeMethod(final Method method, final Object instance, final Object... params) {

        if (method == null) {
            return null;
        }

        if (!method.isAccessible()) {
            method.setAccessible(true);
        }

        T value = null;
        try {

            value = (T) method.invoke(instance, params);

            // For now catch the ClassCastException
        } catch (final ClassCastException | IllegalAccessException | InvocationTargetException e) {
            Logger.severe(e);
        }

        return value;
    }

    /**
     * Get the method with the specified name whether it is
     * accessible or not. If there is no method with the specified
     * name or if the method is in a parent class in the hierarchy
     * and is inaccessible then this method will return null.
     *
     * @param clazz The class the method belongs to.
     * @param name The name of the method.
     * @param paramTypes The parameter types of the method.
     * @return The method or null if no method exists.
     */
    public static Method getMethod(final Class<?> clazz, final String name, final Class<?>... paramTypes) {

        try {
            return clazz.getDeclaredMethod(name, paramTypes);
        } catch (final NoSuchMethodException e) {

            try {
                // No class only methods can be found so search public super class
                return clazz.getSuperclass().getMethod(name, paramTypes);
            } catch (final NoSuchMethodException e1) {
                // Do nothing just continue
            }
        }

        for (final Method method : clazz.getDeclaredMethods()) {

            if (method.getName().equals(name) && method.getParameterCount() == paramTypes.length
                && Reflection.matchParams(method.getParameterTypes(), paramTypes)) {
                return method;
            }
        }

        final Class<?> superClazz = clazz.getSuperclass();
        if (superClazz == null) {
            return null;
        }

        for (final Method method : superClazz.getMethods()) {

            if (method.getName().equals(name) && method.getParameterCount() == paramTypes.length
                && Reflection.matchParams(method.getParameterTypes(), paramTypes)) {
                return method;
            }
        }

        return null;
    }

    /**
     * Get the constructor in the given class, who's parameter
     * types match the parameter types given.
     *
     * @param clazz The class to get the constructor from.
     * @param paramTypes The parameters types to match to.
     * @param <T> The type of the class to retrieve the constructor for.
     * @return The constructor that matches the requested or null if none is found.
     */
    public static <T> Constructor<T> getConstructor(final Class<T> clazz, final Class<?>... paramTypes) {

        try {
            return clazz.getDeclaredConstructor(paramTypes);
        } catch (final NoSuchMethodException e) {
            // Do nothing just continue
        }

        // Sometimes we fail to find the constructor due to a type
        // that is assignable from the true type, but not the exact type.
        // Reflection can be stupid in that way sometimes.

        // Therefore, below, we will get each constructor and check it
        // for compatibility ourselves, but widen the restrictions a bit.

        // All constructors in class regardless of accessibility
        for (final Constructor<?> con : clazz.getDeclaredConstructors()) {

            // Must have the same amount of parameters
            if (con.getParameterCount() == paramTypes.length && Reflection.matchParams(con.getParameterTypes(), paramTypes)) {
                // Class<T> is the class we're searching so it's a safe cast
                return (Constructor<T>) con;
            }
        }

        return null;
    }

    /**
     * Get a class of a specific type using a generic type.
     * If the class is not the type that is given by the generic
     * then a {@link ClassCastException} will be thrown.
     *
     * @param name The name of the class to get.
     * @param <T> The type of the class.
     * @return The class of the given type or null if the class does not exist.
     * @throws ClassCastException If the class is not the type that is given by the generic type.
     */
    public static <T> Class<T> getClassType(final String name) {
        return (Class<T>) Reflection.getClass(name);
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
     * @return The Minecraft class for the given name or null if class was not found.
     */
    public static Class<?> getMcClass(final String name) {
        return Reflection.getClass(NMS_PREFIX + name);
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
     * @return The Craftbukkit class for the given name or null if class was not found.
     */
    public static Class<?> getCraftClass(final String name) {
        return Reflection.getClass(CRAFT_PREFIX + name);
    }

    /**
     * Tell whether the parameter types of a method or constructor
     * match the second given parameter types either exactly or somewhere
     * in the hierarchy.
     *
     * @param params The parameters of the method or constructor.
     * @param paramTypes The parameter types to test against.
     * @return Whether the parameters match or not.
     */
    private static boolean matchParams(final Class<?>[] params, final Class<?>... paramTypes) {

        for (int i = 0; i < params.length; ++i) {

            final Class<?> param = params[i];
            final Class<?> paramType = paramTypes[i];

            // If there is anything that does not match then return false
            if (!param.isAssignableFrom(paramType)) {

                // Primitives can have mismatch problems sometimes
                if (param.isPrimitive() || paramType.isPrimitive()) {
                    final Class<?> type1 = param.isPrimitive() ? param : Reflection.getValue(param, null, "TYPE");
                    final Class<?> type2 =
                            paramType.isPrimitive() ? paramType : Reflection.getValue(paramType, null, "TYPE");
                    return type1 == type2;
                }

                return false;
            }
        }

        return true;
    }

    /**
     * Get all the classes of each object given.
     * If no objects are given this will return an empty array.
     *
     * @param params the parameter objects to get the classes for.
     * @return The class types of each parameter object.
     */
    private static Class<?>[] getClassesForObjects(final Object... params) {

        final Class<?>[] paramClasses = new Class<?>[params.length];
        for (int i = 0; i < params.length; ++i) {
            final Object param = params[i]; // Null check
            paramClasses[i] = param == null ? Void.class : param.getClass();
        }

        return paramClasses;
    }

    /**
     * Get the class for it's exact canonical name.
     *
     * @param name The canonical name of the class to retrieve.
     * @return The class with the name or null if the class is not found.
     */
    private static Class<?> getClass(final String name) {

        try {
            return Class.forName(name);
        } catch (final ClassNotFoundException e) {
            return null;
        }
    }

    // No instance accessibility
    private Reflection() {
    }
}
