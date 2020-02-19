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

import com.andavin.reflect.exception.UncheckedReflectiveOperationException;
import com.andavin.util.Logger;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;

/**
 * @since November 04, 2018
 * @author Andavin
 */
abstract class AttributeMatcher<T extends AccessibleObject & Member, C extends AttributeMatcher> {

    private static final int SYNTHETIC = 0x1000;

    final Class<?> mainType;
    private final int availableModifiers;

    boolean requireExactMatch;
    int requiredModifiers, disallowedModifiers;

    AttributeMatcher(Class<?> mainType, int availableModifiers) {
        this.mainType = mainType;
        this.availableModifiers = availableModifiers | SYNTHETIC;
    }

    /**
     * Require that the matched attribute have the given modifiers.
     * The {@link java.lang.reflect.Modifier} class should be used
     * to specify these using the constants provided in that class.
     *
     * @param modifiers The modifiers to require on the matched attribute.
     * @return This attribute matcher.
     */
    public C require(int... modifiers) {

        for (int modifier : modifiers) {

            this.requiredModifiers |= modifier;
            if ((this.disallowedModifiers & modifier) != 0) {
                Logger.warn("Modifier {} is both required and disallowed.", Integer.toBinaryString(modifier));
            }
        }

        this.requiredModifiers &= this.availableModifiers;
        return (C) this;
    }

    /**
     * Require that the matched attribute <i>not</i> have the given
     * modifiers. The {@link java.lang.reflect.Modifier} class should
     * be used to specify these using the constants provided in that class.
     *
     * @param modifiers The modifiers to disallow on the matched attribute.
     * @return This attribute matcher.
     */
    public C disallow(int... modifiers) {

        for (int modifier : modifiers) {

            this.disallowedModifiers |= modifier;
            if ((this.requiredModifiers & modifier) != 0) {
                Logger.warn("Modifier {} is both required and disallowed.", Integer.toBinaryString(modifier));
            }
        }

        this.disallowedModifiers &= this.availableModifiers;
        return (C) this;
    }

    /**
     * Require that the attribute be a {@code synthetic} member
     * that was created by the compiler.
     *
     * @return This attribute matcher.
     */
    public C requireSynthetic() {

        this.requiredModifiers |= SYNTHETIC;
        if ((this.disallowedModifiers & SYNTHETIC) != 0) {
            Logger.warn("Synthetic is both required and disallowed.");
        }

        return (C) this;
    }

    /**
     * Disallow that the attribute may be a {@code synthetic} member
     * that was created by the compiler.
     *
     * @return This attribute matcher.
     */
    public C disallowSynthetic() {

        this.disallowedModifiers |= SYNTHETIC;
        if ((this.requiredModifiers & SYNTHETIC) != 0) {
            Logger.warn("Synthetic is both required and disallowed.");
        }

        return (C) this;
    }

    /**
     * Require that the attribute's main type match the given type exactly.
     * If this is chosen, then the main type must match the type of the
     * attribute exactly and cannot be an instance of the type.
     *
     * @return This attribute matcher.
     * @see Class#isAssignableFrom(Class)
     */
    public C requireExactMatch() {
        this.requireExactMatch = true;
        return (C) this;
    }

    /**
     * Match the given attribute to the parameters required
     * in this matcher.
     *
     * @param t The attribute to match to.
     * @return If the attribute did match the parameters.
     */
    public abstract boolean match(T t);

    /**
     * Match the given modifiers against the modifiers required
     * and/or disallowed via this matcher while also checking
     * the main type against the main type of the attribute.
     *
     * @param modifiers The modifiers to match against.
     * @param mainType The main type to match exactly.
     * @return If the type and modifiers successfully matched.
     */
    boolean match(int modifiers, Class<?> mainType) {
        // Make sure that it doesn't have any extra bits we don't know about
        modifiers &= this.availableModifiers;
        // For required if not all match, then it does not match
        // For disallowed if any do match, then it does not match
        if ((this.requiredModifiers & modifiers) != this.requiredModifiers || (this.disallowedModifiers & modifiers) != 0) {
            return false;
        }
        // If main type is null then anything can match
        return this.mainType == null ||
                // Test for exact match if it is required
                (this.requireExactMatch ? mainType == this.mainType :
                        // A.isAssignableFrom(B) -> B instanceOf A
                        mainType.isAssignableFrom(this.mainType));
    }

    /**
     * Build an {@link UncheckedReflectiveOperationException} that
     * details that this attribute could not be found given the
     * current parameters.
     *
     * @return The built exception.
     */
    abstract UncheckedReflectiveOperationException buildException();
}
