/*
 * MIT License
 *
 * Copyright (c) 2020 Mark
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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

    private static final String FORMAT = "jdbc:mysql://%s:%d/%s?useSSL=%s";

    public MySQLDataManager(String host, int port, String schema, String user, String password, String useSSL) {
        super(String.format(FORMAT, host, port, schema, useSSL), user, password);
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
