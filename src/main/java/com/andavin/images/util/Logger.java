package com.andavin.images.util;

import com.andavin.images.Images;

import java.util.logging.Level;

/**
 * A custom singleton logger class that wraps a logger from
 * a single plugin and makes it more friendly to use.
 */
public final class Logger {

    private static final java.util.logging.Logger LOGGER = Images.getInstance().getLogger();

    /**
     * Log the value of <code>object.toString()</code>.
     *
     * @param obj The object to log.
     */
    public static synchronized void info(Object obj) {
        Logger.LOGGER.log(Level.INFO, obj.toString());
    }

    /**
     * Log the given message.
     *
     * @param msg The message to log.
     */
    public static synchronized void info(String msg) {
        Logger.LOGGER.log(Level.INFO, msg);
    }

    /**
     * Log the given message replacing the <code>"{}"</code> placeholders
     * with the given arguments.
     *
     * @param msg The message to log.
     * @param args The arguments to replace placeholders with.
     */
    public static synchronized void info(String msg, Object... args) {
        Logger.LOGGER.log(Level.INFO, Logger.format(msg, args));
    }

    /**
     * Log the given throwable and a message replacing the <code>"{}"</code>
     * placeholders with the given arguments.
     *
     * @param throwable The throwable to log.
     * @param msg The message to log with it.
     * @param args The arguments to place into the message.
     */
    public static synchronized void info(Throwable throwable, String msg, Object... args) {
        Logger.LOGGER.log(Level.INFO, Logger.format(msg, args), throwable);
    }

    /**
     * Log the given throwable.
     *
     * @param throwable The throwable to log.
     */
    public static synchronized void info(Throwable throwable) {
        Logger.LOGGER.log(Level.INFO, throwable.getMessage(), throwable);
    }

    /**
     * Log the value of <code>object.toString()</code>.
     *
     * @param obj The object to log.
     */
    public static synchronized void warn(Object obj) {
        Logger.LOGGER.log(Level.WARNING, obj.toString());
    }

    /**
     * Log the given message replacing the <code>"{}"</code> placeholders
     * with the given arguments.
     *
     * @param msg The message to log.
     * @param args The arguments to replace placeholders with.
     */
    public static synchronized void warn(String msg, Object... args) {
        Logger.LOGGER.log(Level.WARNING, Logger.format(msg, args));
    }

    /**
     * Log the given throwable and a message replacing the <code>"{}"</code>
     * placeholders with the given arguments.
     *
     * @param throwable The throwable to log.
     * @param msg The message to log with it.
     * @param args The arguments to place into the message.
     */
    public static synchronized void warn(Throwable throwable, String msg, Object... args) {
        Logger.LOGGER.log(Level.WARNING, Logger.format(msg, args), throwable);
    }

    /**
     * Log the given throwable.
     *
     * @param throwable The throwable to log.
     */
    public static synchronized void warn(Throwable throwable) {
        Logger.LOGGER.log(Level.WARNING, throwable.getMessage(), throwable);
    }

    /**
     * Log the value of <code>object.toString()</code>.
     *
     * @param obj The object to log.
     */
    public static synchronized void severe(Object obj) {
        Logger.LOGGER.log(Level.SEVERE, obj.toString());
    }

    /**
     * Log the given message replacing the <code>"{}"</code> placeholders
     * with the given arguments.
     *
     * @param msg The message to log.
     * @param args The arguments to replace placeholders with.
     */
    public static synchronized void severe(String msg, Object... args) {
        Logger.LOGGER.log(Level.SEVERE, Logger.format(msg, args));
    }

    /**
     * Log the given throwable and a message replacing the <code>"{}"</code>
     * placeholders with the given arguments.
     *
     * @param throwable The throwable to log.
     * @param msg The message to log with it.
     * @param args The arguments to place into the message.
     */
    public static synchronized void severe(Throwable throwable, String msg, Object... args) {
        Logger.LOGGER.log(Level.SEVERE, Logger.format(msg, args), throwable);
    }

    /**
     * Log the given throwable.
     *
     * @param throwable The throwable to log.
     */
    public static synchronized void severe(Throwable throwable) {
        Logger.LOGGER.log(Level.SEVERE, throwable.getMessage(), throwable);
    }

    /**
     * Log the value of <code>object.toString()</code>.
     *
     * @param obj The object to log.
     */
    public static synchronized void debug(Object obj) {
        Logger.LOGGER.log(Level.CONFIG, obj.toString());
    }

    /**
     * Log the given message replacing the <code>"{}"</code> placeholders
     * with the given arguments.
     *
     * @param msg The message to log.
     * @param args The arguments to replace placeholders with.
     */
    public static synchronized void debug(String msg, Object... args) {
        Logger.LOGGER.log(Level.CONFIG, Logger.format(msg, args));
    }

    /**
     * Log the given throwable and a message replacing the <code>"{}"</code>
     * placeholders with the given arguments.
     *
     * @param throwable The throwable to log.
     * @param msg The message to log with it.
     * @param args The arguments to place into the message.
     */
    public static synchronized void debug(Throwable throwable, String msg, Object... args) {
        Logger.LOGGER.log(Level.CONFIG, Logger.format(msg, args), throwable);
    }

    /**
     * Log the given throwable.
     *
     * @param throwable The throwable to log.
     */
    public static synchronized void debug(Throwable throwable) {
        Logger.LOGGER.log(Level.CONFIG, throwable.getMessage(), throwable);
    }

    /**
     * Method from TinyLogger logging framework. Replace <code>"{}"</code>
     * placeholders with the given arguments.
     *
     * @param message The message to place arguments into.
     * @param arguments The arguments to place into the message.
     * @return The message that has been formatted.
     */
    private static String format(String message, Object... arguments) {

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
