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

import com.andavin.reflect.exception.UncheckedNoSuchMethodException;
import com.andavin.reflect.exception.UncheckedReflectiveOperationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static com.andavin.reflect.Reflection.compare;
import static java.util.stream.Collectors.joining;

/**
 * An attribute matcher that takes in all attributes
 * of a constructor to match to a specific constructor
 * in a class.
 *
 * @since Jul7 27, 2019
 * @author Andavin
 */
public class ConstructorMatcher extends AttributeMatcher<Constructor, ConstructorMatcher> {

    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];
    private final Class<?>[] parametersTypes;

    /**
     * Create a new matcher to find a constructor that
     * has no parameters.
     */
    public ConstructorMatcher() {
        this(EMPTY_CLASS_ARRAY);
    }

    /**
     * Create a new matcher to find a constructor.
     *
     * @param parametersTypes The types of the parameters for the
     *                        constructor to match to in order of how
     *                        they are declared on the constructor.
     */
    public ConstructorMatcher(Class<?>... parametersTypes) {
        super(null, Modifier.constructorModifiers());
        this.parametersTypes = parametersTypes;
    }

    /**
     * Require that the constructor match all parameters provided exactly.
     * For example, if {@link Byte} is provided as a parameter type,
     * then a constructor that takes a parameter of type {@code byte} will
     * not be a match.
     * <p>
     * In addition, there will be no hierarchy comparison as described
     * in the {@link Reflection#findConstructor(Class, boolean, Class[])} method.
     *
     * @return This attribute matcher.
     * @see Reflection#findConstructor(Class, boolean, Class[])
     * @see Class#isAssignableFrom(Class)
     */
    @Override
    public ConstructorMatcher requireExactMatch() {
        this.requireExactMatch = true;
        return this;
    }

    @Override
    public boolean match(Constructor constructor) {
        return this.match(constructor.getModifiers(), null) && (this.parametersTypes == null ||
                compare(constructor.getParameterTypes(), this.parametersTypes, this.requireExactMatch));
    }

    @Override
    UncheckedReflectiveOperationException buildException() {
        return new UncheckedNoSuchMethodException("Could not find Constructor(" +
                (this.parametersTypes != null ? Arrays.stream(this.parametersTypes)
                        .map(Class::getSimpleName).collect(joining(", ")) : "any parameters") +
                ") requiring " + Integer.toBinaryString(this.requiredModifiers) + " and disallowing " +
                Integer.toBinaryString(this.disallowedModifiers));
    }
}
