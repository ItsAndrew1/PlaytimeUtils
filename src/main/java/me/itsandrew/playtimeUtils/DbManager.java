//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

//Main class for managing the database.
public class DbManager {
    private final PlaytimeUtils plugin;
    private Connection dbConnection;

    public DbManager(PlaytimeUtils plugin) {
        this.plugin = plugin;
    }

    public boolean connectDb() throws SQLException {
        //Getting the type of database. By default, it is set to 'sqlite'
        String databaseType = plugin.getConfig().getString("database.type", "sqlite");

        //Setting up the connection to the database.
        try{
            switch (databaseType){
                case "sqlite" -> {
                    String fileName = plugin.getConfig().getString("database.file-name", "database.db");
                    File dbFile = new File(plugin.getDataFolder(), fileName);

                    String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
                    dbConnection = DriverManager.getConnection(url);
                }

                case "mysql" -> {
                    //Getting the necessary data for MySQL db
                    String host = plugin.getConfig().getString("database-system.host");
                    String port = plugin.getConfig().getString("database-system.port");
                    String database = plugin.getConfig().getString("database-system.database");
                    String username = plugin.getConfig().getString("database-system.username");
                    String password = plugin.getConfig().getString("database-system.password");

                    String url = "jdbc:mysql://" + host + ":" + port + "/" + database +
                            "?useSSL=false&autoReconnect=true&characterEncoding=utf8";
                    dbConnection = DriverManager.getConnection(url, username, password);
                }

                default -> {
                    plugin.getLogger().severe("[PlaytimeUtils] Invalid database type. Please check your config.yml file.");
                    return false;
                }
            }
            if(dbConnection == null) return false;
        } catch (Exception e){
            plugin.getLogger().severe("[PlaytimeUtils] Failed to connect to the database. See message below for more details: ");
            plugin.getLogger().severe("[PlaytimeUtils] " + e.getMessage());
            return false;
        }

        //Creating the table
        String playtimeTable = """
                CREATE TABLE IF NOT EXISTS playersPlaytime (
                    uuid TEXT PRIMARY KEY,
                    playtime INTEGER
                )
                """;
        try(PreparedStatement statement = dbConnection.prepareStatement(playtimeTable)) {
            statement.executeUpdate();
            return true;
        }
    }

    public String getPlaytime(UUID playerUUID){
        //Getting the playtime of the player (in seconds)
        String statement = "SELECT playtime FROM playersPlaytime WHERE uuid = ?";
        long seconds = 0;
        try(PreparedStatement ps = dbConnection.prepareStatement(statement)){
            ps.setString(1, playerUUID.toString());
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    seconds = rs.getInt("playtime");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Building the string
        StringBuilder time = new StringBuilder();
        long days = TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);

        if (days > 0) time.append(days).append("d ");
        if (hours > 0) time.append(hours).append("h ");
        if (minutes > 0) time.append(minutes).append("m ");

        return time.toString();
    }

    public boolean isPlayerRegistered(UUID playerUUID){
        String statement = "SELECT 1 FROM playersPlaytime WHERE uuid = ?";
        try(PreparedStatement ps = dbConnection.prepareStatement(statement)){
            ps.setString(1, playerUUID.toString());
            try(ResultSet rs = ps.executeQuery()){
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void createPlayerRow(UUID playerUUID){
        String statement = "INSERT INTO playersPlaytime (uuid, playtime) VALUES (?, ?)";
        try(PreparedStatement ps = dbConnection.prepareStatement(statement)){
            ps.setString(1, playerUUID.toString());
            ps.setInt(2, 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
