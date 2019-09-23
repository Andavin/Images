package com.andavin.images.data;

import java.io.File;

/**
 * @since September 22, 2019
 * @author Andavin
 */
public class SQLiteDataManager extends SQLDataManager {

    public SQLiteDataManager(File databaseFile) {
        super("jdbc:sqlite:" + databaseFile.getAbsolutePath());
    }
}
