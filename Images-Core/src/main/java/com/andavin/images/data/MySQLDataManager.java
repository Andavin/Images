package com.andavin.images.data;

/**
 * @since September 22, 2019
 * @author Andavin
 */
public class MySQLDataManager extends SQLDataManager {

    private static final String FORMAT = "jdbc:mysql://%s:%d/%s";

    public MySQLDataManager(String host, int port, String schema, String user, String password) {
        super(String.format(FORMAT, host, port, schema), user, password);
    }
}
