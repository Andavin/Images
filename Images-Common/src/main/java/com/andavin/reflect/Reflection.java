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
package com.andavin.reflect;

import com.andavin.reflect.exception.*;

import java.lang.reflect.*;
import java.util.*;

public final class Reflection {

    private static final ClassResolver CLASS_RESOLVER = /*isAtLeastJava9() ?
            new ModernClassResolver() :*/ new LegacyClassResolver();
    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];
    private static final Map<Class<?>, Class<?>> PRIMITIVES = new HashMap<>(10);

    static {
        PRIMITIVES.put(Byte.class, Byte.TYPE);
        PRIMITIVES.put(Short.class, Short.TYPE);
        PRIMITIVES.put(Integer.class, Integer.TYPE);
        PRIMITIVES.put(Long.class, Long.TYPE);
        PRIMITIVES.put(Float.class, Float.TYPE);
        PRIMITIVES.put(Double.class, Double.TYPE);
        PRIMITIVES.put(Boolean.class, Boolean.TYPE);
        PRIMITIVES.put(Void.class, Void.TYPE);
    }

    // No instance accessibility
    private Reflection() {
    }

    /**
     * Find a constructor for the class and instantiate a new instance
     * using it while passing in the given parameters. If the
     * constructor is not currently accessible, then an attempt will be
     * made to make it accessible.
     * <p>
     * Note that the parameter types for the constructor will be derived
     * from the parameters passed in. There are several downfalls to
     * this approach:
     * <ol>
     *     <li>{@code null} cannot be passed as a parameter</li>
     *     <li>Type mismatch may occur with child classes while the
     *     parent is the actual parameter type</li>
     *     <li>Type mismatch occurs with primitive types and their
     *     wrappers ({@code byte} to {@link Byte} etc.)</li>
     * </ol>
     * However, items 1 and 2 on the above list are compensated for
     * by the hierarchy and primitive type conversions described in
     * the {@link #findConstructor(Class, Class[])} method.
     * On the other hand, if {@code null} is needed as a parameter,
     * then {@link #findConstructor(Class, Class[])} should be
     * used and then {@link #newInstance(Constructor, Object...)}
     * instead of this method (which is basically a shortcut between the two).
     * <p>
     * Any checked {@link Exception}s will be wrapped into a similar
     * unchecked {@link RuntimeException} and re-thrown.
     *
     * @param clazz The class to get the constructor from.
     * @param params The parameters to pass to the constructor.
     * @param <T> The class type instance that will be returned.
     * @return The new instance of the class.
     * @throws SecurityException If the constructor was inaccessible
     *                           and could not be made accessible.
     * @throws UncheckedInstantiationException If the class is a type that cannot be
     *                                         instantiated (e.g. abstract, interface, primitive etc.)
     * @throws UncheckedNoSuchMethodException If the constructor could not be found.
     * @throws UncheckedIllegalAccessException If there was an attempt to access the
     *                                         constructor that failed.
     * @throws UncheckedInvocationTargetException If there is an exception thrown during
     *                                            instantiation of the new instance. The exception
     *                                            will be wrapped into this exception as the cause.
     */
    public static <T> T newInstance(Class<T> clazz, Object... params) throws SecurityException, UncheckedInstantiationException,
            UncheckedNoSuchMethodException, UncheckedIllegalAccessException, UncheckedInvocationTargetException {

        if (params.length == 0) {

            try {
                return clazz.newInstance();
            } catch (InstantiationException e) {
                throw wrapException(e);
            } catch (IllegalAccessException ignored) {
            }
        }

        Class<?>[] classes = getClassesForObjects(params);
        return newInstance(findConstructor(clazz, classes), params);
    }

    /**
     * Use the given constructor to instantiate a new instance passing
     * in the given parameters. If the constructor is not currently
     * accessible, then an attempt will be made to make it accessible.
     * <p>
     * Any checked {@link Exception}s will be wrapped into a similar
     * unchecked {@link RuntimeException} and re-thrown.
     *
     * @param con The constructor to use to instantiate the new instance.
     * @param params The parameters to pass to the constructor.
     * @param <T> The class type instance that will be returned.
     * @return The new instance of the constructor's declaring class.
     * @throws NullPointerException If the constructor is {@code null}.
     * @throws SecurityException If the constructor was inaccessible
     *                           and could not be made accessible.
     * @throws UncheckedInstantiationException If the declaring class for the constructor
     *                                         is abstract.
     * @throws UncheckedIllegalAccessException If there was an attempt to access the
     *                                         constructor that failed.
     * @throws UncheckedInvocationTargetException If there is an exception thrown during
     *                                            instantiation of the new instance. The exception
     *                                            will be wrapped into this exception as the cause.
     */
    public static <T> T newInstance(Constructor<T> con, Object... params) throws SecurityException,
            UncheckedInstantiationException, UncheckedIllegalAccessException, UncheckedInvocationTargetException {

        if (!con.isAccessible()) {
            con.setAccessible(true);
        }

        try {
            return con.newInstance(params);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw wrapException(e);
        }
    }

    /**
     * Find a field in the given class with the given name and
     * get its value from it using the given object instance.
     * If the field is not currently accessible, then an attempt
     * will be made to make it accessible.
     * <p>
     * Any checked {@link Exception}s will be wrapped into a similar
     * unchecked {@link RuntimeException} and re-thrown.
     *
     * @param clazz The class to get the field from.
     * @param instance The instance invoke the field getter on.
     * @param name The name of the field to find.
     * @param <T> The class type of the field.
     * @return The value contained within the field.
     * @throws ClassCastException If the {@link T} is not the field's type.
     * @throws SecurityException If the field was inaccessible
     *                           and could not be made accessible.
     * @throws UncheckedNoSuchFieldException If the field could not be found.
     * @throws UncheckedIllegalAccessException If there was an attempt to access the
     *                                         field that failed.
     */
    public static <T> T getFieldValue(Class<?> clazz, Object instance, String name) throws ClassCastException,
            SecurityException, UncheckedNoSuchFieldException, UncheckedIllegalAccessException {
        return getFieldValue(findField(clazz, name), instance);
    }

    /**
     * Get the value of the given field using the given object instance.
     * If the field is not currently accessible, then an attempt will
     * be made to make it accessible.
     * <p>
     * Any checked {@link Exception}s will be wrapped into a similar
     * unchecked {@link RuntimeException} and re-thrown.
     *
     * @param field The field to get the value from.
     * @param instance The instance invoke the field getter on.
     * @param <T> The class type of the field.
     * @return The value contained within the field.
     * @throws NullPointerException If the field is {@code null}.
     * @throws ClassCastException If the {@link T} is not the field's type.
     * @throws SecurityException If the field was inaccessible
     *                           and could not be made accessible.
     * @throws UncheckedIllegalAccessException If there was an attempt to access the
     *                                         field that failed.
     */
    // May need to validate the generic return type T by taking a Class<T> as parameter
    public static <T> T getFieldValue(Field field, Object instance) throws ClassCastException,
            SecurityException, UncheckedIllegalAccessException {

        if (!field.isAccessible()) {
            field.setAccessible(true);
        }

        try {
            return (T) field.get(instance);
        } catch (IllegalAccessException e) {
            throw wrapException(e);
        }
    }

    /**
     * Find a field in the given class with the given name and
     * set its value using the given object instance.
     * If the field is not currently accessible, then an attempt
     * will be made to make it accessible.
     * <p>
     * Any checked {@link Exception}s will be wrapped into a similar
     * unchecked {@link RuntimeException} and re-thrown.
     *
     * @param clazz The class to get the field from.
     * @param instance The instance invoke the field setter on.
     * @param name The name of the field to find.
     * @param value The value to set the value of the field to.
     * @throws SecurityException If the field was inaccessible
     *                           and could not be made accessible.
     * @throws UncheckedNoSuchFieldException If the field could not be found.
     * @throws UncheckedIllegalAccessException If there was an attempt to access the
     *                                         field that failed.
     */
    public static void setFieldValue(Class<?> clazz, Object instance, String name, Object value)
            throws SecurityException, UncheckedNoSuchFieldException, UncheckedIllegalAccessException {
        setFieldValue(findField(clazz, name), instance, value);
    }

    /**
     * Set the value of the given field using the given object instance.
     * If the field is not currently accessible, then an attempt
     * will be made to make it accessible.
     * <p>
     * Any checked {@link Exception}s will be wrapped into a similar
     * unchecked {@link RuntimeException} and re-thrown.
     *
     * @param field The field to set the value of.
     * @param instance The instance invoke the field setter on.
     * @param value The value to set the value of the field to.
     * @throws NullPointerException If the field is {@code null}.
     * @throws SecurityException If the field was inaccessible
     *                           and could not be made accessible.
     * @throws UncheckedIllegalAccessException If there was an attempt to access the
     *                                         field that failed.
     */
    public static void setFieldValue(Field field, Object instance, Object value)
            throws SecurityException, UncheckedIllegalAccessException {

        if (!field.isAccessible()) {
            field.setAccessible(true);
        }

        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw wrapException(e);
        }
    }

    /**
     * Find a field with the given name in the given class.
     * If the field is not declared by the given class, then
     * the super class (if applicable) will be searched for
     * a matching field as well.
     *
     * @param clazz The class to get the field from.
     * @param name The name of the field to find.
     * @return The field that matches the name.
     * @throws UncheckedNoSuchFieldException If no fields matching the search
     *                                       parameters are found.
     */
    public static Field findField(Class<?> clazz, String name) throws UncheckedNoSuchFieldException {

        NoSuchFieldException exception;
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            exception = e;
        }

        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != null && superClazz != Object.class) {

            try {
                return superClazz.getField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }

        throw wrapException(exception);
    }

    /**
     * Find a field by its type and its {@link Modifier modifiers}
     * specified in the given {@link FieldMatcher} parameters.
     * <p>
     * Note that this method will <i>not</i> include any fields from
     * any super classes of the given class to search; it will only
     * include fields of all access from within the class itself.
     *
     * @param clazz The class that should be searched for the field.
     * @param matcher The {@link FieldMatcher} to use to match fields
     *                in the class.
     * @return The first field found that matches all the required parameters.
     * @throws UncheckedNoSuchFieldException If no fields matching the search
     *                                       parameters are found.
     */
    public static Field findField(Class<?> clazz, FieldMatcher matcher) throws UncheckedNoSuchFieldException {
        return findField(clazz, 0, matcher);
    }

    /**
     * Find a field by its type and its {@link Modifier modifiers}
     * specified in the given {@link FieldMatcher} parameters.
     * <p>
     * If there are multiple fields that successfully match the
     * {@link FieldMatcher} given, then the index can be used to
     * retrieve the desired field.
     * <p>
     * Note that this method will <i>not</i> include any fields from
     * any super classes of the given class to search; it will only
     * include fields of all access from within the class itself.
     *
     * @param clazz The class that should be searched for the field.
     * @param index The index of field to retrieve. For example,
     *              {@code 0} should be given to retrieve the first
     *              field that matches the parameters.
     * @param matcher The {@link FieldMatcher} to use to match fields
     *                in the class.
     * @return The field found that matches all the required parameters.
     * @throws IndexOutOfBoundsException If there are not enough fields
     *                                   that match the parameters in order
     *                                   to reach the required index.
     * @throws UncheckedNoSuchFieldException If no fields matching the search
     *                                       parameters are found.
     */
    public static Field findField(Class<?> clazz, int index, FieldMatcher matcher)
            throws IndexOutOfBoundsException, UncheckedNoSuchFieldException {

        int found = 0;
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {

            if (matcher.match(field) && found++ == index) {
                return field;
            }
        }

        if (found < index) {
            throw new IndexOutOfBoundsException("Too few matching fields to reach " +
                    index + " in " + clazz.getSimpleName());
        }

        throw matcher.buildException();
    }

    /**
     * Find a method using the name and class and invoke it on the
     * object instance and passing in the given parameters. If the
     * method is not currently accessible, then an attempt will be
     * made to make it accessible.
     * <p>
     * Note that the parameter types for the method will be derived
     * from the parameters passed in. There are several downfalls to
     * this approach:
     * <ol>
     *     <li>{@code null} cannot be passed as a parameter</li>
     *     <li>Type mismatch may occur with child classes while the
     *     parent is the actual parameter type</li>
     *     <li>Type mismatch occurs with primitive types and their
     *     wrappers ({@code byte} to {@link Byte} etc.)</li>
     * </ol>
     * However, items 1 and 2 on the above list are compensated for
     * by the hierarchy and primitive type conversions described in
     * the {@link #findMethod(Class, String, Class[])} method.
     * On the other hand, if {@code null} is needed as a parameter,
     * then {@link #findMethod(Class, String, Class[])} should be
     * used and then {@link #invokeMethod(Method, Object, Object...)}
     * instead of this method (which is basically a shortcut between the two).
     * <p>
     * Any checked {@link Exception}s will be wrapped into a similar
     * unchecked {@link RuntimeException} and re-thrown.
     *
     * @param clazz The class to get the method from.
     * @param instance The instance to invoke the method for.
     * @param name The name of the method to find.
     * @param params The parameters to pass to the method.
     * @param <T> The return type of the method.
     * @return The result of the method invocation (returned value).
     * @throws ClassCastException If the method does not return the type {@link T};
     * @throws SecurityException If the method was inaccessible
     *                           and could not be made accessible.
     * @throws UncheckedNoSuchMethodException If the method could not be found.
     * @throws UncheckedIllegalAccessException If there was an attempt to  access the
     *                                         method that failed.
     * @throws UncheckedInvocationTargetException If there is an exception thrown during
     *                                            invocation of the method. The exception
     *                                            will be wrapped into this exception as the cause.
     */
    public static <T> T invokeMethod(Class<?> clazz, Object instance, String name, Object... params) throws ClassCastException,
            SecurityException, UncheckedNoSuchMethodException, UncheckedIllegalAccessException, UncheckedInvocationTargetException {
        return invokeMethod(findMethod(clazz, name, getClassesForObjects(params)), instance, params);
    }

    /**
     * Invoke the given method on the object instance and passing in
     * the given parameters. If the method is not currently accessible,
     * then an attempt will be made to make it accessible.
     * <p>
     * Any checked {@link Exception}s will be wrapped into a similar
     * unchecked {@link RuntimeException} and re-thrown.
     * <p>
     * Note that if the method passed in is {@code null}, then it will
     * be logged and {@code null} will be immediately returned.
     *
     * @param method The method to invoke.
     * @param instance The instance to invoke the method for.
     * @param params The parameters to pass to the method.
     * @param <T> The return type of the method.
     * @return The result of the method invocation (returned value).
     * @throws NullPointerException If the method is {@code null}.
     * @throws ClassCastException If the method does not return the type {@link T};
     * @throws SecurityException If the method was inaccessible
     *                           and could not be made accessible.
     * @throws UncheckedIllegalAccessException If there was an attempt to  access the
     *                                         method that failed.
     * @throws UncheckedInvocationTargetException If there is an exception thrown during
     *                                            invocation of the method. The exception
     *                                            will be wrapped into this exception as the cause.
     */
    // May need to validate the generic return type T by taking a Class<T> as parameter
    public static <T> T invokeMethod(Method method, Object instance, Object... params) throws ClassCastException,
            SecurityException, UncheckedIllegalAccessException, UncheckedInvocationTargetException {

        if (!method.isAccessible()) {
            method.setAccessible(true);
        }

        try {
            return (T) method.invoke(instance, params);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw wrapException(e);
        }
    }

    /**
     * Search the given class for a method that has matching name
     * and parameter types to those provided.
     * <p>
     * If there is no method found with exact parameter type matches,
     * then each type will be individually compared taking into
     * consideration its hierarchy.
     * <pre>
     *     class A implements B {}
     *
     *     class Foo {
     *         void bar(B b) {
     *             // Cool method stuff
     *         }
     *     }
     *
     *     // Searching with normal methods
     *     try {
     *         Foo.class.getDeclaredMethod("bar", A.class);
     *     } catch (NoSuchMethodException e) {
     *         // Exception will be thrown because of type mismatch
     *     }
     *
     *     // Search with Reflection will retrieve method
     *     // because type B is assignable from type A
     *     Reflection.findMethod(Foo.class, "bar", A.class);
     * </pre>
     * Note that if there is still no method match even with
     * the type conversions, then an unchecked exception will
     * still be thrown.
     * <p>
     * In addition to declared methods, super class methods will
     * also be searched for a match using the {@link Class#getMethod(String, Class[])}
     * while incorporating the hierarchy matching explained above as well.
     *
     * @param clazz The class to get the method from.
     * @param name The name of the method to find.
     * @param paramTypes The parameters types to match to.
     * @return The method that matches the requested.
     * @throws UncheckedNoSuchMethodException If no methods matching the name and
     *                                        parameters is found.
     */
    public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        return findMethod(clazz, name, true, paramTypes);
    }

    /**
     * Search the given class for a method that has matching name
     * and parameter types to those provided.
     * <p>
     * If there is no method found with exact parameter type matches,
     * then each type will be individually compared taking into
     * consideration its hierarchy (if {@code hierarchyMatch} is true).
     * <pre>
     *     class A implements B {}
     *
     *     class Foo {
     *         void bar(B b) {
     *             // Cool method stuff
     *         }
     *     }
     *
     *     // Searching with normal methods
     *     try {
     *         Foo.class.getDeclaredMethod("bar", A.class);
     *     } catch (NoSuchMethodException e) {
     *         // Exception will be thrown because of type mismatch
     *     }
     *
     *     // Search with Reflection will retrieve method
     *     // because type B is assignable from type A
     *     Reflection.findMethod(Foo.class, "bar", A.class);
     * </pre>
     * Note that if there is still no method match even with
     * the type conversions, then an unchecked exception will
     * still be thrown.
     * <p>
     * In addition to declared methods, super class methods will
     * also be searched for a match using the {@link Class#getMethod(String, Class[])}
     * while incorporating the hierarchy matching explained above as well.
     *
     * @param clazz The class to get the method from.
     * @param name The name of the method to find.
     * @param hierarchyMatch If the hierarchy should be matched to as well.
     *                       If set to {@code false}, then only
     *                       {@link Class#getDeclaredMethod(String, Class[])}
     *                       will be used.
     * @param paramTypes The parameters types to match to.
     * @return The method that matches the requested.
     * @throws UncheckedNoSuchMethodException If no methods matching the name and
     *                                        parameters is found.
     */
    public static Method findMethod(Class<?> clazz, String name, boolean hierarchyMatch, Class<?>... paramTypes)
            throws UncheckedNoSuchMethodException {

        NoSuchMethodException exception;
        try {
            return clazz.getDeclaredMethod(name, paramTypes);
        } catch (NoSuchMethodException e) {

            if (hierarchyMatch) {

                for (Method method : clazz.getDeclaredMethods()) {

                    if (method.getParameterCount() == paramTypes.length && method.getName().equals(name)
                            && compare(method.getParameterTypes(), paramTypes, false)) {
                        return method;
                    }
                }
            }

            exception = e;
        }

        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz == null) {
            throw wrapException(exception);
        }

        try {
            // No class only methods can be found so search public super class
            return superClazz.getMethod(name, paramTypes);
        } catch (NoSuchMethodException ignored) {
        }

        if (hierarchyMatch) {

            for (Method method : superClazz.getMethods()) {

                if (method.getName().equals(name) && method.getParameterCount() == paramTypes.length
                        && compare(method.getParameterTypes(), paramTypes, false)) {
                    return method;
                }
            }
        }

        throw wrapException(exception);
    }

    /**
     * Find the first method by the types of its parameters, its return
     * type and its {@link Modifier modifiers} specified in the given
     * {@link MethodMatcher} parameters.
     * <p>
     * Note that this method will <i>not</i> include any methods from
     * any super classes of the given class to search; it will only
     * include methods of all access from within the class itself.
     *
     * @param clazz The class that should be searched for the method.
     * @param matcher The {@link MethodMatcher} to use to match methods
     *                in the class.
     * @return The first method found that matches all the required parameters.
     * @throws UncheckedNoSuchMethodException If no methods matching the search
     *                                        parameters are found.
     */
    public static Method findMethod(Class<?> clazz, MethodMatcher matcher) throws UncheckedNoSuchMethodException {
        return findMethod(clazz, 0, matcher);
    }

    /**
     * Find a method by the types of its parameters, its return type
     * and its {@link Modifier modifiers} specified in the given
     * {@link MethodMatcher} parameters.
     * <p>
     * If there are multiple methods that successfully match the
     * {@link MethodMatcher} given, then the index can be used to
     * retrieve the desired method.
     * <p>
     * Note that this method will <i>not</i> include any methods from
     * any super classes of the given class to search; it will only
     * include methods of all access from within the class itself.
     *
     * @param clazz The class that should be searched for the method.
     * @param index The index of method to retrieve. For example,
     *              {@code 0} should be given to retrieve the first
     *              method that matches the parameters.
     * @param matcher The {@link MethodMatcher} to use to match methods
     *                in the class.
     * @return The method found that matches all the required parameters.
     * @throws IndexOutOfBoundsException If there are not enough methods
     *                                   that match the parameters in order
     *                                   to reach the required index.
     * @throws UncheckedNoSuchMethodException If no methods matching the search
     *                                        parameters are found.
     */
    public static Method findMethod(Class<?> clazz, int index, MethodMatcher matcher)
            throws IndexOutOfBoundsException, UncheckedNoSuchMethodException {

        int found = 0;
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {

            if (matcher.match(method) && found++ == index) {
                return method;
            }
        }

        if (found < index) {
            throw new IndexOutOfBoundsException("Too few matching methods to reach " +
                    index + " in " + clazz.getSimpleName());
        }

        throw matcher.buildException();
    }

    /**
     * Search the given class for a constructor that has matching
     * parameter types to those provided.
     * <p>
     * If there is no constructor found with exact type matches,
     * then each type will be individually compared taking into
     * consideration its hierarchy.
     * <pre>
     *     class A implements B {}
     *
     *     class Foo {
     *         Foo(B b) {
     *             // The best construction
     *         }
     *     }
     *
     *     // Searching with normal methods
     *     try {
     *         Foo.class.getDeclaredConstructor(A.class);
     *     } catch (NoSuchMethodException e) {
     *         // Exception will be thrown because of type mismatch
     *     }
     *
     *     // Search with Reflection will retrieve constructor
     *     // because type B is assignable from type A
     *     Reflection.findConstructor(Foo.class, A.class);
     * </pre>
     * Note that if there is still no constructor match even with
     * the type conversions, then an unchecked exception will still
     * be thrown.
     *
     * @param clazz The class to get the constructor from.
     * @param paramTypes The parameters types to match to.
     * @param <T> The type of the class to retrieve the constructor for.
     * @return The constructor that matches the requested;
     * @throws UncheckedNoSuchMethodException If the constructor with the
     *                                        parameters was not found.
     */
    public static <T> Constructor<T> findConstructor(Class<T> clazz, Class<?>... paramTypes)
            throws UncheckedNoSuchMethodException {
        return findConstructor(clazz, true, paramTypes);
    }

    /**
     * Search the given class for a constructor that has matching
     * parameter types to those provided.
     * <p>
     * If there is no constructor found with exact type matches,
     * then each type will be individually compared taking into
     * consideration its hierarchy (if {@code hierarchyMatch} is true).
     * <pre>
     *     class A implements B {}
     *
     *     class Foo {
     *         Foo(B b) {
     *             // The best construction
     *         }
     *     }
     *
     *     // Searching with normal methods
     *     try {
     *         Foo.class.getDeclaredConstructor(A.class);
     *     } catch (NoSuchMethodException e) {
     *         // Exception will be thrown because of type mismatch
     *     }
     *
     *     // Search with Reflection will retrieve constructor
     *     // because type B is assignable from type A
     *     Reflection.findConstructor(Foo.class, A.class);
     * </pre>
     * Note that if there is still no constructor match even with
     * the type conversions, then an unchecked exception will still
     * be thrown.
     *
     * @param clazz The class to get the constructor from.
     * @param hierarchyMatch If the hierarchy should be matched to as well.
     *                       If set to {@code false}, then only
     *                       {@link Class#getDeclaredConstructor(Class[])}
     *                       will be used.
     * @param paramTypes The parameters types to match to.
     * @param <T> The type of the class to retrieve the constructor for.
     * @return The constructor that matches the requested.
     * @throws UncheckedNoSuchMethodException If the constructor with the
     *                                        parameters was not found.
     */
    public static <T> Constructor<T> findConstructor(Class<T> clazz, boolean hierarchyMatch, Class<?>... paramTypes)
            throws UncheckedNoSuchMethodException {

        NoSuchMethodException exception;
        try {
            return clazz.getDeclaredConstructor(paramTypes);
        } catch (NoSuchMethodException e) {

            if (!hierarchyMatch) {
                throw wrapException(e);
            }

            exception = e;
        }

        // Sometimes we fail to find the constructor due to a type
        // that is assignable from the true type, but not the exact type.
        // Reflection can be stupid in that way sometimes.

        // Therefore, below, we will get each constructor and check it
        // for compatibility ourselves, but widen the restrictions a bit.

        // All constructors in class regardless of accessibility
        for (Constructor<?> con : clazz.getDeclaredConstructors()) {

            // Must have the same amount of parameters
            if (con.getParameterCount() == paramTypes.length && compare(con.getParameterTypes(), paramTypes, false)) {
                // Class<T> is the class we're searching so it's a safe cast
                return (Constructor<T>) con;
            }
        }

        throw wrapException(exception);
    }

    /**
     * Get a class of a specific type using a generic type using the
     * exact canonical name of a class. If the class is not the type
     * that is given by the generic, then a {@link ClassCastException}
     * will be thrown.
     *
     * @param name The canonical name of the class to retrieve.
     * @param <T> The type of the class.
     * @return The class with the name;
     * @throws ClassCastException If the class is not the type that
     *                            is given by the generic type.
     * @throws UncheckedClassNotFoundException If the class was not found
     *                                         or an exception occurred while
     *                                         loading the class.
     */
    public static <T> Class<T> findClass(String name) throws UncheckedClassNotFoundException {

        try {
            return (Class<T>) Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw wrapException(e);
        }
    }

    /**
     * Get the class that is calling the method that is calling
     * this method. For example, if method {@code foo()} in class
     * {@code A} calls method {@code bar()} in class {@code B}
     * and {@code bar()} calls this method then this will return
     * the class {@code A}.
     *
     * @return The name of the class calling a method.
     */
    public static String getCallerClass() {
        // Start at 1 to get the class that called
        // the method that called this method :P
        return getCallerClass(1, Collections.emptySet());
    }

    /**
     * Get the class that is calling the method that is calling
     * this method. For example, if method {@code foo()} in class
     * {@code A} calls method {@code bar()} in class {@code B}
     * and {@code bar()} calls this method with index of {@code 0}
     * then this will return the class {@code A}.
     *
     * @param index The index (starting at {@code 0}) of the class to
     *              get from the class calling this method. {@code 0}
     *              will return the calling class {@code 1} will return
     *              the class that called that one and so on.
     * @param exclude The classes to skip over when finding the calling class.
     * @return The name of the class index away from calling a method.
     */
    public static String getCallerClass(int index, Class<?>... exclude) {

        Set<String> excluded;
        if (exclude.length != 0) {

            excluded = new HashSet<>((int) (exclude.length / 0.75));
            for (Class<?> clazz : exclude) {
                excluded.add(clazz.getName());
            }
        } else {
            excluded = Collections.emptySet();
        }

        // Add one to exclude the second call in this class
        return getCallerClass(++index, excluded);
    }

    /**
     * Get the class that is calling the method that is calling
     * this method. For example, if method {@code foo()} in class
     * {@code A} calls method {@code bar()} in class {@code B}
     * and {@code bar()} calls this method with index of {@code 0}
     * then this will return the class {@code A}.
     *
     * @param index The index (starting at {@code 0}) of the class to
     *              get from the class calling this method. {@code 0}
     *              will return the calling class {@code 1} will return
     *              the class that called that one and so on.
     * @param excluded The class names to exclude.
     * @return The name of the class index away from calling a method.
     */
    public static String getCallerClass(int index, Set<String> excluded) {
        // Must add one to exclude this class as well
        String name = CLASS_RESOLVER.resolve(++index);
        while (excluded.contains(name)) {
            // Increment to get the next name since the previous was excluded
            name = CLASS_RESOLVER.resolve(++index);
        }

        return name;
    }

    /**
     * Tell whether the parameter types of a method or constructor
     * match the second given parameter types either exactly or somewhere
     * in the hierarchy.
     *
     * @param primary The parameters of the method or constructor.
     * @param secondary The parameter types to compare to.
     * @param exact If the parameters should match exactly or if
     *              primitive to wrapper type conversions should be
     *              considered when comparing Java primitive types
     *              as well as if assignable types should be included
     *              rather than just the class itself.
     * @return Whether the parameters match or not.
     */
    public static boolean compare(Class<?>[] primary, Class<?>[] secondary, boolean exact) {

        if (primary.length != secondary.length) {
            return false;
        }

        if (primary.length == 0) {
            return true;
        }

        for (int i = 0; i < primary.length; ++i) {

            Class<?> primaryType = primary[i], secondaryType = secondary[i];
            if (exact) {

                if (primaryType != secondaryType) {
                    return false;
                }
            } else {

                // If there is anything that does not match then return false
                if (!primaryType.isAssignableFrom(secondaryType)) {
                    // Primitives can have mismatch problems sometimes
                    boolean primaryTypePrimitive = primaryType.isPrimitive();
                    boolean secondaryTypePrimitive = secondaryType.isPrimitive();
                    if (primaryTypePrimitive || secondaryTypePrimitive) {
                        Class<?> type1 = primaryTypePrimitive ? primaryType : PRIMITIVES.get(primaryType);
                        Class<?> type2 = secondaryTypePrimitive ? secondaryType : PRIMITIVES.get(secondaryType);
                        return type1 == type2;
                    }

                    return false;
                }
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
    private static Class<?>[] getClassesForObjects(Object... params) {

        if (params.length == 0) {
            return EMPTY_CLASS_ARRAY;
        }

        Class<?>[] paramClasses = new Class<?>[params.length];
        for (int i = 0; i < params.length; ++i) {
            Object param = params[i]; // Null check
            paramClasses[i] = param == null ? Void.class : param.getClass();
        }

        return paramClasses;
    }

    private static RuntimeException wrapException(Exception e) {

        if (e instanceof IllegalAccessException) {
            return new UncheckedIllegalAccessException(e.getMessage());
        }

        if (e instanceof InvocationTargetException) {
            return new UncheckedInvocationTargetException(e.getCause(), e.getMessage());
        }

        if (e instanceof InstantiationException) {
            return new UncheckedInstantiationException(e.getMessage());
        }

        if (e instanceof NoSuchMethodException) {
            throw new UncheckedNoSuchMethodException(e.getMessage());
        }

        if (e instanceof NoSuchFieldException) {
            throw new UncheckedNoSuchFieldException(e.getMessage());
        }

        if (e instanceof ClassNotFoundException) {
            throw new UncheckedClassNotFoundException(e.getMessage(), e.getCause());
        }

        return e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
    }

    private static boolean isAtLeastJava9() {

        String version = System.getProperty("java.version");
        if (version == null) {
            return false;
        }

        int index = version.indexOf('.');
        if (index > 0) {
            version = version.substring(0, index);
        }

        return version.matches("[0-9]{1,8}") && Integer.parseInt(version) >= 9;
    }
}
