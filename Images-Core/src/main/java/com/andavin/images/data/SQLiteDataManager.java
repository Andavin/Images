/* Copyright (c) 2019 */
package com.andavin.images.data;

import com.andavin.util.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.andavin.reflect.Reflection.findClass;

/**
 * @since September 22, 2019
 * @author Andavin
 */
public class SQLiteDataManager extends SQLDataManager {

    public SQLiteDataManager(File databaseFile) {
        super("jdbc:sqlite:" + databaseFile.getAbsolutePath());
    }

    @Override
    public void initialize() {
        // Initialize the driver for versions earlier than JDBC 4
        // (i.e. before Minecraft 1.11 SQLite did not auto load)
        findClass("org.sqlite.JDBC");
        try (Connection connection = this.getConnection();
             PreparedStatement create = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                             "`id` INTEGER PRIMARY KEY," +
                             "`data` BLOB NOT NULL)")) {
            create.executeUpdate();
        } catch (SQLException e) {
            Logger.severe(e);
        }
    }
}
