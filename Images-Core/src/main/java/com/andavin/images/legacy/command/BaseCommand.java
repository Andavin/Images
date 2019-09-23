package com.andavin.images.legacy.command;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({ "SameParameterValue", "WeakerAccess" })
public abstract class BaseCommand {

    private int min;
    private final String name;
    private String desc = "", usage;
    private String[] aliases = new String[0];
    private final Map<String, BaseCommand> children = new HashMap<>();

    /**
     * Create a new BaseCommand with the given name.
     *
     * @param name The name of the command to create.
     */
    protected BaseCommand(String name) {
        this.name = name;
        this.usage = '/' + name;
    }

    /**
     * Execute this command given the player who is executing the
     * command and the arguments given. The permission and minimum
     * arguments required are verified before this method is invoked.
     *
     * @param player The player that is executing this command.
     * @param label The exact string the player typed in whether it was an alias for the command name.
     * @param args The arguments given by the player.
     */
    public abstract void execute(Player player, String label, String[] args);

    /**
     * Execute this command for any other kind of sender if this command is
     * not executed by a player.
     *
     * @param sender The sender the dispatched the command.
     * @param label The exact string the sender typed in whether it was an alias for the command name.
     * @param args The arguments given by the sender.
     */
    public void execute(CommandSender sender, String label, String[] args) {
        sender.sendMessage("Sorry, /" + this.name + " is player only.");
    }

    /**
     * Tell whether the player has permission to execute this command.
     * This can be a check for the player's rank, a custom permission
     * system, where the player is standing, pretty much any permission
     * check possible.
     *
     * @param player The player attempting to execute this command.
     * @param args The arguments given in the command.
     * @return Whether the player has permission to use this command.
     */
    public abstract boolean hasPermission(Player player, String[] args);

    /**
     * Tab complete for the specific block command. Using the last
     * word given by the command sender.
     *
     * @param sender The command sender that is typing.
     * @param args The arguments currently
     * @param completions All of the current completions to be added to.
     */
    public void tabComplete(CommandSender sender, String[] args, List<String> completions) {

        String first = args[0].toLowerCase();
        if (first.isEmpty()) {
            completions.add(sender.getName());
            return;
        }

        Player player = Bukkit.getPlayer(first);
        if (player == null) {

            Bukkit.getOnlinePlayers().forEach(pl -> {

                if (sender instanceof Player && !((Player) sender).canSee(pl)) {
                    return;
                }

                String name = pl.getName();
                if (name.toLowerCase().startsWith(first)) {
                    completions.add(name);
                }
            });
        } else {
            completions.add(player.getName());
        }
    }

    /**
     * Get the child commands of this BaseCommand.
     *
     * @return A Map of the command keyed by their name and aliases.
     */
    Map<String, BaseCommand> getChildren() {
        return this.children;
    }

    /**
     * Add a new child command to this command.
     *
     * @param child The child command to add.
     */
    protected void addChild(BaseCommand child) {

        Preconditions.checkNotNull(child);
        this.children.put(child.getName().toLowerCase(), child);
        for (String alias : child.getAliases()) {
            this.children.put(alias.toLowerCase(), child);
        }
    }

    /**
     * Set the minimum arguments required to execute this command.
     *
     * @param min The minimum arguments to set to.
     */
    protected void setMinimumArgs(int min) {
        this.min = min;
    }

    /**
     * Set the aliases for this command.
     *
     * @param aliases The aliases to set to.
     */
    protected void setAliases(String... aliases) {
        this.aliases = aliases;
    }

    /**
     * Set the description of this command.
     *
     * @param desc The description to set to.
     */
    protected void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * Set the usage of this command.
     *
     * @param usage The usage to set to.
     */
    protected void setUsage(String usage) {
        this.usage = usage.replace("<command>", this.name);
    }

    /**
     * Get the name of this command used to
     * register and execute the command.
     *
     * @return This command's name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the description of this command.
     *
     * @return The description.
     */
    public String getDescription() {
        return this.desc;
    }

    /**
     * Get the usage string for this command. This is
     * the structure of the command how the command
     * sender should use it.
     *
     * @return The usage for this command.
     */
    public String getUsage() {
        return this.usage;
    }

    /**
     * Get the minimum arguments required to
     * execute this command.
     *
     * @return The minimum arguments.
     */
    public int getMinimumArgs() {
        return this.min;
    }

    /**
     * Get an array of the aliases for this command.
     *
     * @return The aliases for this command.
     */
    public String[] getAliases() {
        return this.aliases;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) {
            return true;
        }

        if (o == null || !(o instanceof BaseCommand)) {
            return false;
        }

        BaseCommand cmd = ((BaseCommand) o);
        if (cmd.getName().equalsIgnoreCase(this.name)) {
            return true;
        }

        for (String alias : cmd.getAliases()) {

            if (alias.equalsIgnoreCase(this.name)) {
                return true;
            }

            for (String alias1 : this.getAliases()) {

                // Check all aliases against each other and the command name
                if (alias.equalsIgnoreCase(alias1)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() + Arrays.hashCode(this.aliases) * 17;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
