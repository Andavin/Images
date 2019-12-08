package com.andavin.images;

import com.andavin.images.image.CustomImage;
import com.andavin.images.image.CustomImageSection;
import com.andavin.util.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.function.Supplier;

import static com.andavin.images.image.CustomImageSection.DEFAULT_STARTING_ID;

/**
 * @since September 21, 2019
 * @author Andavin
 */
public abstract class PacketListener<T, U> implements Versioned {

    static Supplier<List<CustomImage>> getImages;

    /**
     * Create a new entity listener to the given player's
     * packet listener.
     *
     * @param player The player to create the listener for.
     * @param listener The listener to set to.
     */
    public void createEntityListener(Player player, ImageListener listener) {

        Plugin protocolLib = Bukkit.getPluginManager().getPlugin("ProtocolLib");
        if (protocolLib != null) { // ProtocolLib is present so use it for higher stability
            new ProtocolLibListener(protocolLib, listener, this);
        } else {
            setEntityListener(player, listener);
        }
    }

    /**
     * Set a new entity listener to the given player's
     * packet listener.
     *
     * @param player The player to set the listener for.
     * @param listener The listener to set to.
     */
    protected abstract void setEntityListener(Player player, ImageListener listener);

    /**
     * Handle an incoming use entity packet that signifies
     * when a player interacts with another entity in any way.
     *
     * @param player The player that the packet is coming from.
     * @param listener The listener to call once the
     *                 packet is processed.
     * @param packet The packet to handle.
     */
    protected abstract void handle(Player player, ImageListener listener, T packet);

    /**
     * Handle an incoming creative set slot packet that
     * signifies when a player "gets" an entity while
     * in creative mode.
     *
     * @param player The player that the packet is coming from.
     * @param packet The packet to handle.
     */
    protected abstract void handle(Player player, U packet);

    /**
     * Get the {@link CustomImageSection} that has the
     * map ID from an {@link CustomImage}.
     *
     * @param mapId The map ID to get the section for.
     * @return The section or {@code null} if the
     *         section is not found.
     */
    public static CustomImageSection getImageSection(int mapId) {

        List<CustomImage> images = getImages.get();
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (images) {

            for (CustomImage image : images) {

                CustomImageSection section = image.getSectionByMap(mapId);
                if (section != null) {
                    return section;
                }
            }

            return null;
        }
    }

    /**
     * Call the given listener for the player.
     *
     * @param player The player that interacted.
     * @param entityId The entity the player interacted with.
     * @param action The action that the player used to interact.
     * @param hand The hand the player used if applicable.
     * @param listener The listener to call.
     */
    public static void call(Player player, int entityId, InteractType action, Hand hand, ImageListener listener) {

        if (entityId < DEFAULT_STARTING_ID) {
            return;
        }

        Scheduler.async(() -> {

            List<CustomImage> images = getImages.get();
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (images) {

                for (CustomImage image : images) {

                    CustomImageSection section = image.getSection(entityId);
                    if (section != null) {
                        listener.click(player, image, section, action, hand);
                        return;
                    }
                }
            }
        });
    }

    public enum Hand {
        OFF_HAND, MAIN_HAND
    }

    public enum InteractType {
        LEFT_CLICK, RIGHT_CLICK
    }

    public interface ImageListener {

        /**
         * Accept and interaction with and entity.
         *
         * @param player The player that interacted.
         * @param image The image the player interacted with.
         * @param section The specific section that the player interacted with.
         * @param action The action that the player took when interacted.
         * @param hand The hand the player used to interact with.
         */
        void click(Player player, CustomImage image, CustomImageSection section,
                   InteractType action, Hand hand);
    }
}
