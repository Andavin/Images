package com.andavin.images.command;

import com.andavin.images.Images;
import com.andavin.images.image.CustomImage;
import com.andavin.images.legacy.LegacyImportManager;
import com.andavin.util.Scheduler;
import com.andavin.util.TimeoutMetadata;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @since September 23, 2019
 * @author Andavin
 */
public class ImportCommand extends BaseCommand {

    private static final String CONFIRM_META = "import-confirm";
    private static final long COOLDOWN = TimeUnit.MINUTES.toMillis(5);

    private long lastRun;

    ImportCommand() {
        super("import", "images.command.import");
        this.setAliases("legacyImport");
        this.setDesc("Import and destroy all legacy images and create new ones in the new format");
        this.setUsage("/image import");
    }

    @Override
    public void execute(Player player, String label, String[] args) {

        if (TimeoutMetadata.isExpired(player, CONFIRM_META)) {
            player.sendMessage("§eAre you sure you want to import legacy images?");
            player.sendMessage("§7This can not be undone unless you take a backup of your world.");
            player.sendMessage("§eRe-type the command to confirm");
            player.setMetadata(CONFIRM_META, new TimeoutMetadata(15, TimeUnit.SECONDS));
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastRun < COOLDOWN) {
            player.sendMessage("§cPlease wait 5 minutes before running that again.");
            return;
        }

        lastRun = now;
        player.sendMessage("§aImporting legacy images.\n" +
                "§eThis will cause sever lag. Please wait...");
        List<CustomImage> importedImages = LegacyImportManager.importImages(
                Images.getImagesDirectory(), Images.getDataManager());
        Scheduler.async(() -> {
            Images.addImages(importedImages);
            player.sendMessage("§aSuccessfully imported §f" + importedImages.size() + "§a images");
        });
    }
}
