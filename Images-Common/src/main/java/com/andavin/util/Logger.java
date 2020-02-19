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
package com.andavin.util;

import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * A custom singleton logger class that wraps a logger from
 * a single plugin and makes it more friendly to use.
 * <p>
 * This automatically detects the plugin from which the logger
 * is called and logs under that plugin's logger.
 *
 * @author Andavin
 */
public final class Logger {

    private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger("");

    /**
     * Initialize this logger with the main {@link java.util.logging.Logger}
     * to use as a parent.
     *
     * @param logger The logger to use.
     * @throws IllegalStateException If the logger has already been initialized.
     */
    public static void initialize(java.util.logging.Logger logger) throws IllegalStateException {
        Logger.logger = logger;
    }

    /**
     * Log the value of <code>object.toString()</code>.
     *
     * @param obj The object to log.
     */
    public static void info(Object obj) {
        logger.log(Level.INFO, obj.toString());
    }

    /**
     * Log the given message.
     *
     * @param msg The message to log.
     */
    public static void info(String msg) {
        logger.log(Level.INFO, msg);
    }

    /**
     * Log the given message replacing the <code>"{}"</code> placeholders
     * with the given arguments.
     *
     * @param msg The message to log.
     * @param args The arguments to replace placeholders with.
     */
    public static void info(String msg, Object... args) {
        logger.log(Level.INFO, Logger.format(msg, args));
    }

    /**
     * Log the given throwable and a message replacing the <code>"{}"</code>
     * placeholders with the given arguments.
     *
     * @param throwable The throwable to log.
     * @param msg The message to log with it.
     * @param args The arguments to place into the message.
     */
    public static void info(Throwable throwable, String msg, Object... args) {
        logger.log(Level.INFO, Logger.format(msg, args), throwable);
    }

    /**
     * Log the given throwable.
     *
     * @param throwable The throwable to log.
     */
    public static void info(Throwable throwable) {
        logger.log(Level.INFO, throwable.getMessage(), throwable);
    }

    /**
     * Log the value of <code>object.toString()</code>.
     *
     * @param obj The object to log.
     */
    public static void warn(Object obj) {
        logger.log(Level.WARNING, obj.toString());
    }

    /**
     * Log the given message.
     *
     * @param msg The message to log.
     */
    public static void warn(String msg) {
        logger.log(Level.WARNING, msg);
    }

    /**
     * Log the given message replacing the <code>"{}"</code> placeholders
     * with the given arguments.
     *
     * @param msg The message to log.
     * @param args The arguments to replace placeholders with.
     */
    public static void warn(String msg, Object... args) {
        logger.log(Level.WARNING, Logger.format(msg, args));
    }

    /**
     * Log the given throwable and a message replacing the <code>"{}"</code>
     * placeholders with the given arguments.
     *
     * @param throwable The throwable to log.
     * @param msg The message to log with it.
     * @param args The arguments to place into the message.
     */
    public static void warn(Throwable throwable, String msg, Object... args) {
        logger.log(Level.WARNING, Logger.format(msg, args), throwable);
    }

    /**
     * Log the given throwable.
     *
     * @param throwable The throwable to log.
     */
    public static void warn(Throwable throwable) {
        logger.log(Level.WARNING, throwable.getMessage(), throwable);
    }

    /**
     * Log the value of <code>object.toString()</code>.
     *
     * @param obj The object to log.
     */
    public static void severe(Object obj) {
        logger.log(Level.SEVERE, obj.toString());
    }

    /**
     * Log the given message replacing the <code>"{}"</code> placeholders
     * with the given arguments.
     *
     * @param msg The message to log.
     * @param args The arguments to replace placeholders with.
     */
    public static void severe(String msg, Object... args) {
        logger.log(Level.SEVERE, Logger.format(msg, args));
    }

    /**
     * Log the given throwable and a message replacing the <code>"{}"</code>
     * placeholders with the given arguments.
     *
     * @param throwable The throwable to log.
     * @param msg The message to log with it.
     * @param args The arguments to place into the message.
     */
    public static void severe(Throwable throwable, String msg, Object... args) {
        logger.log(Level.SEVERE, Logger.format(msg, args), throwable);
    }

    /**
     * Log the given throwable.
     *
     * @param throwable The throwable to log.
     */
    public static void severe(Throwable throwable) {
        logger.log(Level.SEVERE, throwable.getMessage(), throwable);
    }

    /**
     * Log the value of <code>object.toString()</code>.
     *
     * @param obj The object to log.
     */
    public static void debug(Object obj) {
        logger.log(Level.CONFIG, obj.toString());
    }

    /**
     * Log the given message.
     *
     * @param msg The message to log.
     */
    public static void debug(String msg) {
        logger.log(Level.CONFIG, msg);
    }

    /**
     * Log the given message replacing the <code>"{}"</code> placeholders
     * with the given arguments.
     *
     * @param msg The message to log.
     * @param args The arguments to replace placeholders with.
     */
    public static void debug(String msg, Object... args) {
        logger.log(Level.CONFIG, Logger.format(msg, args));
    }

    /**
     * Log the given throwable and a message replacing the <code>"{}"</code>
     * placeholders with the given arguments.
     *
     * @param throwable The throwable to log.
     * @param msg The message to log with it.
     * @param args The arguments to place into the message.
     */
    public static void debug(Throwable throwable, String msg, Object... args) {
        logger.log(Level.CONFIG, Logger.format(msg, args), throwable);
    }

    /**
     * Log the given throwable.
     *
     * @param throwable The throwable to log.
     */
    public static void debug(Throwable throwable) {
        logger.log(Level.CONFIG, throwable.getMessage(), throwable);
    }


    /**
     * Handle the {@link Throwable} for a {@link Consumer handler}. More
     * precisely, if the message for the Throwable or any of the linked
     * cases of the Throwable start with the {@code '§'}, then the message
     * will be send to the CommandSender. Otherwise, the Throwable will
     * be thrown again as a {@link RuntimeException}.
     * <p>
     * If the Throwable is an instance of RuntimeException, then it will be
     * cast and thrown exactly how it is, otherwise it will be wrapped in a
     * new RuntimeException and thrown that way.
     *
     * @param throwable The Throwable to handle.
     * @param handler The handler to send the message to.
     * @throws RuntimeException If the Throwable is not an exception to send
     *                          to the sender.
     */
    public static void handle(Throwable throwable, Consumer<String> handler) throws RuntimeException {
        handle(throwable, handler, false);
    }

    /**
     * Handle the {@link Throwable} for a {@link Consumer handler}. More
     * precisely, if the message for the Throwable or any of the linked
     * cases of the Throwable start with the {@code '§'}, then the message
     * will be send to the CommandSender. Otherwise, the Throwable will
     * be thrown again as a {@link RuntimeException}.
     * <p>
     * If the Throwable is an instance of RuntimeException, then it will be
     * cast and thrown exactly how it is, otherwise it will be wrapped in a
     * new RuntimeException and thrown that way.
     *
     * @param throwable The Throwable to handle.
     * @param handler The handler to send the message to.
     * @param log If the Throwable should be logged instead of thrown when it
     *            cannot be sent to the CommandSender.
     * @throws RuntimeException If the Throwable is not an exception to send
     *                          to the CommandSender and {@code log} is false.
     */
    public static void handle(Throwable throwable, Consumer<String> handler, boolean log) throws RuntimeException {

        String message = throwable.getMessage();
        if (message != null && message.trim().charAt(0) == '§') {
            handler.accept(message);
            return;
        }

        for (Throwable cause = throwable.getCause(); cause != null; cause = cause.getCause()) {

            message = cause.getMessage();
            if (message != null && message.trim().charAt(0) == '§') {
                handler.accept(message);
                return;
            }
        }

        handler.accept("§cAn error has occurred during command execution.");
        if (log) {
            Logger.severe(throwable);
        } else if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        } else {
            throw new RuntimeException(throwable);
        }
    }

    /**
     * Method from TinyLogger logging framework. Replaces <code>{}</code>
     * placeholders with the given arguments.
     *
     * @param message The message to place arguments into.
     * @param arguments The arguments to place into the message.
     * @return The message that has been formatted.
     */
    public static String format(String message, Object... arguments) {

        if (arguments == null || arguments.length == 0) {
            return message;
        }

        int start = 0, argumentIndex = 0, openBraces = 0;
        StringBuilder builder = new StringBuilder(message.length() + arguments.length * 16);
        for (int index = 0; index < message.length(); ++index) {

            char character = message.charAt(index);
            if (character == '{') {

                if (openBraces++ == 0 && start < index) {
                    builder.append(message, start, index);
                    start = index;
                }
            } else if (character == '}' && openBraces > 0) {

                if (--openBraces == 0) {

                    if (argumentIndex < arguments.length) {

                        Object argument = arguments[argumentIndex++];
                        if (index == start + 1) {
                            builder.append(argument);
                        } else {
                            builder.append(format(message.substring(start + 1, index), argument));
                        }
                    } else {
                        builder.append(message, start, index + 1);
                    }

                    start = index + 1;
                }
            }
        }

        if (start < message.length()) {
            builder.append(message, start, message.length());
        }

        return builder.toString();
    }
}
