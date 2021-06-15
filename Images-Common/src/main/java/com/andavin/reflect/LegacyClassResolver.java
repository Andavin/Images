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
import com.andavin.util.Logger;

import java.lang.reflect.Method;

import static com.andavin.reflect.Reflection.findMethod;
import static com.andavin.reflect.Reflection.invokeMethod;

/**
 * @since October 31, 2018
 * @author Andavin
 */
public class LegacyClassResolver implements ClassResolver {

    private static final int DEPTH_ADDITION = 3;
    private final Method stackTraceMethod;

    LegacyClassResolver() {
        stackTraceMethod = getStackTraceMethod();
    }

    private static Method getStackTraceMethod() {

        try {
            Method method = findMethod(Throwable.class, "getStackTraceElement", int.class);
            StackTraceElement element = invokeMethod(method, new Throwable(), 0);
            return LegacyClassResolver.class.getName().equals(element.getClassName()) ? method : null;
        } catch (UncheckedNoSuchMethodException e) {
            return null;
        }
    }

    @Override
    public String resolve(int depth) {

        Throwable throwable = new Throwable();
        if (stackTraceMethod != null) {

            try {
                return Reflection.<StackTraceElement>invokeMethod(this.stackTraceMethod,
                        throwable, depth + DEPTH_ADDITION).getClassName();
            } catch (Exception e) {
                Logger.severe(e, "Failed to get single stack trace element from throwable");
            }
        }

        StackTraceElement[] trace = throwable.getStackTrace();
        return trace[Math.min(depth + DEPTH_ADDITION, trace.length - 1)].getClassName();
    }
}
