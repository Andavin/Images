package com.andavin.images.command;

import com.andavin.images.Images;
import com.andavin.images.data.DataManager;
import com.andavin.images.data.FileDataManager;
import com.andavin.images.data.MySQLDataManager;
import com.andavin.images.data.SQLiteDataManager;
import com.andavin.images.image.CustomImage;
import com.andavin.util.Logger;
import com.andavin.util.Scheduler;
import com.andavin.util.TimeoutMetadata;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * @since July 05, 2020
 * @author Andavin
 */
public final class TransferCommand extends BaseCommand {

    private static final String KEY = "transfer.check";

    public TransferCommand() {
        super("transfer", "images.command.transfer");
        this.setAliases("datatransfer");
        this.setMinimumArgs(1);
        this.setDesc("Transition all data to a different configured database");
    }

    @Override
    public void execute(Player player, String label, String[] args) {

        if (TimeoutMetadata.isExpired(player, KEY)) {
            player.setMetadata(KEY, new TimeoutMetadata(20, TimeUnit.SECONDS));
            player.sendMessage("§cRunning this command may overwrite any data\n" +
                    "§cin the destination database\n" +
                    "§cThis will also stop the server");
            player.sendMessage("§eRerun the command to confirm");
            return;
        }

        player.removeMetadata(KEY, Images.getInstance());
        DataManager current = Images.getDataManager(), to;
        String type = args[0].toUpperCase(Locale.ENGLISH);
        switch (type) {
            case "MYSQL":

                if (current instanceof MySQLDataManager) {
                    player.sendMessage("§cAlready using MySQL. Please choose another...");
                    return;
                }

                FileConfiguration config = Images.getInstance().getConfig();
                to = new MySQLDataManager(
                        config.getString("database.host"),
                        config.getInt("database.port"),
                        config.getString("database.schema"),
                        config.getString("database.user"),
                        config.getString("database.password")
                );

                break;
            case "SQLITE":

                if (current instanceof SQLiteDataManager) {
                    player.sendMessage("§cAlready using SQLite. Please choose another...");
                    return;
                }

                to = new SQLiteDataManager(new File(Images.getImagesDirectory(), "images.db"));
                break;
            case "FILE":

                if (current instanceof FileDataManager) {
                    player.sendMessage("§cAlready using file data. Please choose another...");
                    return;
                }

                to = new FileDataManager(new File(Images.getImagesDirectory(), "images.cimg"));
                break;
            default:
                player.sendMessage("§cUnknown database type §f" + args[0]);
                player.sendMessage("§7Try MySQL or SQLite");
                return;
        }

        Scheduler.async(() -> {

            player.sendMessage("§aInitializing new database...");
            to.initialize();
            List<CustomImage> images = Images.getMatchingImages(i -> true);
            images.forEach(image -> image.setId(-1)); // Reset the ID of the image so it can be reset
            player.sendMessage("§aSaving §f" + images.size() + "§a to new database...");
            try {
                to.saveAll(images);
                player.sendMessage("§aSuccessfully transferred all images!");
                player.sendMessage("§eYou may now change the database configuration\n" +
                        "§eto the new database and restart your server");
                Logger.info("Successfully transferred all images to {}", type);
                Logger.info("You may now change the database configuration to {}", type);
            } catch (Exception e) {
                player.sendMessage("§cAn error occurred while transferring!");
                player.sendMessage("§eIf you are using MySQL ensure that it is configured properly.");
                player.sendMessage("§eOtherwise, contact the developer");
                Logger.severe(e);
            }
            // No matter what shutdown
            Bukkit.shutdown();
        });
    }
}
