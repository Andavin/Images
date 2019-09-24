package com.andavin.images.command;

import com.andavin.util.Logger;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

import static com.andavin.util.StringUtil.centerMessage;

@SuppressWarnings("Duplicates")
final class CommandExecutor extends org.bukkit.command.Command {

    private final BaseCommand command;

    CommandExecutor(BaseCommand command) {
        super(command.getName(), command.getDescription(),
                command.getUsage(), Arrays.asList(command.getAliases()));
        this.command = command;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {

        try {

            BaseCommand cmd = this.getExecutable(this.command, args);
            if (cmd != this.command) {
                args = this.trimArgs(cmd, args);
            }

            if (!cmd.hasPermission(sender, args)) {
                sender.sendMessage("§cInsufficient permission.");
                return true;
            }

            if (args.length < cmd.getMinimumArgs()) {
                sender.sendMessage(" §c§m----------------------------------------------------");
                sender.sendMessage(centerMessage("§cNot enough arguments!"));
                sender.sendMessage(centerMessage("§7Try §c" + cmd.getUsage()));
                sender.sendMessage(centerMessage("§e" + cmd.getDescription()));
                sender.sendMessage(" §c§m----------------------------------------------------");
                return true;
            }

            if (sender instanceof Player) {
                cmd.execute((Player) sender, label, args);
            } else {
                cmd.execute(sender, label, args);
            }
        } catch (Throwable e) {
            Logger.handle(e, sender::sendMessage);
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {

        if (args.length == 0) {
            return Collections.emptyList();
        }

        String last = args[args.length - 1].toLowerCase();
        BaseCommand cmd = this.getExecutable(this.command, args);

        Map<String, BaseCommand> children = cmd.getChildren();
        List<String> completions = new LinkedList<>();
        children.forEach((name, command) -> {

            if (name.startsWith(last) && !completions.contains(command.getName())) {
                completions.add(command.getName());
            }
        });

        // Ensure that there are arguments to tab complete for
        // the child command
        args = this.trimArgs(cmd, args);
        if (args.length > 0) {
            cmd.tabComplete(sender, args, completions);
        }

        return completions;
    }

    /**
     * Get an executable child command of the given command
     * with the given arguments. Basically getting the last
     * child command that matches the furthest argument in the array.
     *
     * @param cmd The command to get the child command of.
     * @param args The arguments given to get the matching children from.
     * @return An executable child command of the given command or the command if there is no children.
     */
    private BaseCommand getExecutable(BaseCommand cmd, String[] args) {

        for (String arg : args) {

            BaseCommand child = this.getChild(cmd, arg);
            if (child == null) {
                break;
            }

            cmd = child;
        }

        return cmd;
    }

    /**
     * Get the child command of the given command that matches the
     * argument given.
     *
     * @param cmd The command to get the child command of.
     * @param arg The argument to match the child command to.
     * @return A child command of the given command that matches the argument or null if none exists.
     */
    private BaseCommand getChild(BaseCommand cmd, String arg) {
        return cmd.getChildren().get(arg.toLowerCase());
    }

    /**
     * Trim the argument array to right after the given child's name.
     * So if the arguments given are { test, doTest, tests } and the
     * child command is "test" then the array returned will be
     * { doTest, tests }.
     *
     * @param child The child command to match.
     * @param args The arguments to trim.
     * @return A trimmed argument array.
     */
    private String[] trimArgs(BaseCommand child, String[] args) {

        for (int i = 0; i < args.length; ++i) {

            if (args[i].equalsIgnoreCase(child.getName())) {
                return (String[]) ArrayUtils.subarray(args, i + 1, args.length);
            }

            for (String alias : child.getAliases()) {

                if (args[i].equalsIgnoreCase(alias)) {
                    return (String[]) ArrayUtils.subarray(args, i + 1, args.length);
                }
            }
        }

        return args;
    }
}
