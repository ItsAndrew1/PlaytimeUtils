//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils;

import java.io.File;
import java.sql.*;
import java.util.*;
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
                    mainPlaytime INTEGER,
                    rewardPlaytime INTEGER
                )
                """;
        try(PreparedStatement statement = dbConnection.prepareStatement(playtimeTable)) {
            statement.executeUpdate();
            return true;
        }
    }

    public String getMainPlaytimeString(UUID playerUUID){
        //Getting the playtime of the player from the db
        long seconds = getMainPlaytime(playerUUID);

        //Also adding the seconds from the playtime map
        seconds += plugin.getPlaytimeMap().getOrDefault(playerUUID, 0);

        //Building the string
        StringBuilder time = new StringBuilder();
        long days = TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);

        if (days > 0) time.append(days).append("d ");
        if (hours > 0) time.append(hours).append("h ");

        if(minutes > 0 && seconds > 60) time.append(minutes).append("m");
        else if (minutes > 0) time.append(minutes).append("m ");

        if(seconds < 60) time.append(seconds).append("s");

        return time.toString();
    }

    public int getMainPlaytime(UUID playerUUID){
        String statement = "SELECT mainPlaytime FROM playersPlaytime WHERE uuid = ?";
        try(PreparedStatement ps = dbConnection.prepareStatement(statement)){
            ps.setString(1, playerUUID.toString());
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return rs.getInt("mainPlaytime");
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return 0;
    }

    public List<Map.Entry<UUID, Integer>> getTop3Players(){
        Map<UUID, Integer> top3PlayersSeconds = new HashMap<>();

        //Getting the map in seconds
        String statement = "SELECT rewardPlaytime, uuid FROM playersPlaytime ORDER BY rewardPlaytime DESC LIMIT 3";
        try(PreparedStatement ps = dbConnection.prepareStatement(statement)){
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    UUID playerUUID = UUID.fromString(rs.getString("uuid"));
                    int playtime = rs.getInt("rewardPlaytime") + plugin.getPlaytimeMap().getOrDefault(playerUUID, 0);
                    top3PlayersSeconds.put(playerUUID, playtime);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Sorting the map by seconds
        List<Map.Entry<UUID, Integer>> sortedList = new ArrayList<>(top3PlayersSeconds.entrySet());
        sortedList.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        if(sortedList.size() > 3) sortedList.subList(0, 3);

        return sortedList;
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
        String statement = "INSERT INTO playersPlaytime (uuid, mainPlaytime, rewardPlaytime) VALUES (?, ?, ?)";
        try(PreparedStatement ps = dbConnection.prepareStatement(statement)){
            ps.setString(1, playerUUID.toString());
            ps.setInt(2, 0);
            ps.setInt(3, 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getRewardPlaytime(UUID playerUUID){
        String SQL = "SELECT rewardPlaytime FROM playersPlaytime WHERE uuid = ?";
        try(PreparedStatement ps = dbConnection.prepareStatement(SQL)){
            ps.setString(1, playerUUID.toString());
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return rs.getInt("rewardPlaytime");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void wipeRewardPlaytime(){
        String SQL = "UPDATE playersPlaytime SET rewardPlaytime = 0";
        try(PreparedStatement ps = dbConnection.prepareStatement(SQL)){
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updatePlayerMainPlaytime(UUID playerUUID, int seconds){
        String statement = "UPDATE playersPlaytime SET mainPlaytime = ? WHERE uuid = ?";
        try(PreparedStatement ps = dbConnection.prepareStatement(statement)){
            ps.setInt(1, seconds + getMainPlaytime(playerUUID));
            ps.setString(2, playerUUID.toString());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updatePlayerRewardPlaytime(UUID playerUUID, int seconds){
        String statement = "UPDATE playersPlaytime SET rewardPlaytime = ? WHERE uuid = ?";
        try(PreparedStatement ps = dbConnection.prepareStatement(statement)){
            ps.setInt(1, seconds + getRewardPlaytime(playerUUID));
            ps.setString(2, playerUUID.toString());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
