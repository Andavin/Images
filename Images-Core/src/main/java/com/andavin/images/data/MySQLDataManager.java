/* Copyright (c) 2019 */
package com.andavin.images.data;

import com.andavin.util.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.andavin.reflect.Reflection.findClass;

/**
 * @since September 22, 2019
 * @author Andavin
 */
public class MySQLDataManager extends SQLDataManager {

    private static final String FORMAT = "jdbc:mysql://%s:%d/%s";

    public MySQLDataManager(String host, int port, String schema, String user, String password) {
        super(String.format(FORMAT, host, port, schema), user, password);
    }

    @Override
    public void initialize() {
        // Initialize the driver for versions earlier than JDBC 4
        // (i.e. before Minecraft 1.11 MySQL did not auto load)
        findClass("com.mysql.jdbc.Driver");
        try (Connection connection = this.getConnection();
             PreparedStatement create = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                             "`data` MEDIUMBLOB NOT NULL)")) {
            create.executeUpdate();
        } catch (SQLException e) {
            Logger.severe(e);
        }
    }
}
