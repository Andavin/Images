package com.andavin.images.data;

import com.andavin.images.image.CustomImage;
import com.andavin.reflect.exception.UncheckedClassNotFoundException;
import com.andavin.util.Logger;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since September 21, 2019
 * @author Andavin
 */
abstract class SQLDataManager implements DataManager {

    static final String TABLE_NAME = "`custom_images`";
    private final String url;
    private final Properties properties = new Properties();

    SQLDataManager(String url) {
        this.url = checkNotNull(url, "url");
    }

    SQLDataManager(String url, String user, String password) {
        this(url);
        this.properties.put("user", checkNotNull(user, "user"));
        this.properties.put("password", checkNotNull(password, "password"));
    }

    @Override
    public List<CustomImage> load() {

        List<CustomImage> images = new ArrayList<>();
        try (Connection connection = this.getConnection();
             PreparedStatement select = connection.prepareStatement(
                     "SELECT `id`, `data` FROM " + TABLE_NAME)) {

            try (ResultSet result = select.executeQuery()) {

                while (result.next()) {
                    CustomImage image = toImage(result.getBytes("data"));
                    image.setId(result.getInt("id"));
                    images.add(image);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return images;
    }

    @Override
    public void save(CustomImage image) {

        try (Connection connection = this.getConnection()) {
            save(connection, image);
        } catch (SQLException e) {
            Logger.severe(e);
        }
    }

    @Override
    public void saveAll(List<CustomImage> images) {

        if (images.isEmpty()) {
            return;
        }

        try (Connection connection = this.getConnection()) {

            for (CustomImage image : images) {
                save(connection, image);
            }
        } catch (SQLException e) {
            Logger.severe(e);
        }
    }

    /**
     * Create a new {@link Connection} to the SQL database
     * with the given properties and credentials specified
     * at creation of this data manager.
     *
     * @return The newly created connection.
     * @throws SQLException If something goes wrong while creating
     *                      the connection.
     */
    Connection getConnection() throws SQLException {
        return DriverManager.getConnection(this.url, this.properties);
    }

    private synchronized void save(Connection connection, CustomImage image) throws SQLException {

        if (image.getId() == -1) {

            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO " + TABLE_NAME + "(`data`) VALUES (?)",
                    Statement.RETURN_GENERATED_KEYS)) {

                insert.setObject(1, toByteArray(image));
                insert.execute();
                try (ResultSet result = insert.getGeneratedKeys()) {

                    if (result.next()) {
                        image.setId(result.getInt(1));
                        Logger.info("Set ID to {}", image.getId());
                    } else {
                        Logger.info("nothing returned");
                    }
                }
            }

            return;
        }

        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE " + TABLE_NAME + " SET `data` = ? WHERE `id` = ?")) {
            update.setBytes(1, toByteArray(image));
            update.setInt(2, image.getId());
            update.executeUpdate();
        }
    }

    private CustomImage toImage(byte[] bytes) {

        ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
        try (ObjectInputStream stream = new ObjectInputStream(byteStream)) {
            return (CustomImage) stream.readObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ClassNotFoundException e) {
            throw new UncheckedClassNotFoundException(e.getMessage(), e);
        }
    }

    private byte[] toByteArray(CustomImage image) {

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(); // Doesn't need to be closed
        try (ObjectOutputStream stream = new ObjectOutputStream(byteStream)) {
            stream.writeObject(image);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return byteStream.toByteArray();
    }
}
