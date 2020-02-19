/* Copyright (c) 2019 */
package com.andavin.images.command;

import com.andavin.reflect.exception.UncheckedReflectiveOperationException;
import com.andavin.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;

import java.util.Map;

import static com.andavin.reflect.Reflection.getFieldValue;
import static com.andavin.reflect.Reflection.invokeMethod;

/**
 * A manager for registering all {@link BaseCommand}s.
 */
public final class CommandRegistry {

    private static final CommandMap COMMAND_MAP = invokeMethod(Bukkit.getServer().getClass(),
            Bukkit.getServer(), "getCommandMap");

    /**
     * Register all the commands.
     */
    public static void registerCommands() {
        register(new ImageCommand());
    }

    /**
     * Register the given BaseCommand and allow it to
     * be executed by Bukkit.
     *
     * @param command The command to register.
     */
    public static void register(BaseCommand command) {
        Logger.debug("Registering command {}.", command);
        clearCommand(command);
        COMMAND_MAP.register("images", new CommandExecutor(command));
    }

    private static void clearCommand(BaseCommand command) {

        try {

            Map<String, Command> commands = getFieldValue(SimpleCommandMap.class,
                    COMMAND_MAP, "knownCommands");
            commands.remove(command.getName().toLowerCase());
            for (String alias : command.getAliases()) {
                commands.remove(alias.toLowerCase());
            }
        } catch (UncheckedReflectiveOperationException e) {
            Logger.severe(e);
        }
    }
}
