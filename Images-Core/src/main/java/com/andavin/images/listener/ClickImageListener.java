package com.andavin.images.listener;

import com.andavin.images.Images;
import com.andavin.images.PacketListener;
import com.andavin.images.image.CustomImage;
import com.andavin.util.Scheduler;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;

public class ClickImageListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Scheduler.laterAsync(() -> {
            Images.addListenerTaskAlawys(event.getPlayer(), (clicker, image, section, action, hand) -> {
                if (action == PacketListener.InteractType.RIGHT_CLICK) {
                    clicker.sendMessage(ChatColor.YELLOW + "" + ChatColor.ITALIC + "https://opensea.io/assets/" + image.getContract() + "/" + image.getTokenId());
                }
            });
        }, 60L);
    }

    @EventHandler
    public void onQuit(PlayerJoinEvent event) {
        Images.removeListenerTaskAlawys(event.getPlayer());
    }
}
