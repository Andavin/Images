package com.andavin.images.command;

import com.andavin.images.Images;
import com.andavin.images.util.Reflection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created on February 14, 2018
 *
 * @author Andavin
 */
final class DeleteCommand extends BaseCommand {

    private final Object packet;
    private final BaseComponent[] components = new ComponentBuilder("Click on an image to delete").color(ChatColor.RED).create();

    DeleteCommand() {

        super("delete");
        this.setAliases("remove", "unload");
        this.setUsage("/image delete");
        this.setDesc("Delete an existing image by clicking on it.");

        if (Reflection.VERSION_NUMBER < 1120) {
            Object comp = Reflection.getInstance(Reflection.getMcClass("ChatComponentText"), "§cClick on an image to delete");
            this.packet = Reflection.getInstance(ImageCommand.PACKET, comp, (byte) 2);
        } else {
            this.packet = null;
        }
    }

    @Override
    public void execute(Player player, String label, String[] args) {

        player.setMetadata(ImageCommand.DELETE_META, new FixedMetadataValue(Images.getInstance(), 0));
        player.sendMessage("§eLeft click any block to cancel.");

        Object connection = null;
        if (Reflection.VERSION_NUMBER < 1120) {
            Object entityPlayer = Reflection.invokeMethod(ImageCommand.GET_HANDLE, player);
            connection = Reflection.getValue(ImageCommand.CONNECTION, entityPlayer);
        }

        Object finalConn = connection;
        new BukkitRunnable() {

            @Override
            public void run() {

                if (player.hasMetadata(ImageCommand.DELETE_META)) {

                    if (Reflection.VERSION_NUMBER >= 1120) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, DeleteCommand.this.components);
                    } else {
                        Reflection.invokeMethod(ImageCommand.SEND_PACKET, finalConn, DeleteCommand.this.packet);
                    }
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimerAsynchronously(Images.getInstance(), 5L, 20L);
    }

    @Override
    public boolean hasPermission(Player player, String[] args) {
        return player.hasPermission("image.manage.delete");
    }
}
