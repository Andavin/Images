package com.andavin.images.command;

import com.andavin.images.util.Logger;
import com.andavin.images.util.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;

/**
 * A manager for registering all {@link BaseCommand}s.
 */
public final class CmdRegistry {

    private static final CommandMap COMMAND_MAP = Reflection.invokeMethod(Bukkit.getServer().getClass(), Bukkit.getServer(), "getCommandMap");

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
        COMMAND_MAP.register("images", new CmdExecutor(command));
    }
}
