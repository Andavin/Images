package com.andavin.images.command;

import com.andavin.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;

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
        COMMAND_MAP.register("images", new CommandExecutor(command));
    }
}
