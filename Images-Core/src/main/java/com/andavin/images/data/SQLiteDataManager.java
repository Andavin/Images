package com.andavin.images.data;

import com.andavin.util.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
