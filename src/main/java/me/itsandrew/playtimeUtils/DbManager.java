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

        //Creating the playtime table
        String playtimeTable = """
                CREATE TABLE IF NOT EXISTS playersPlaytime (
                    uuid TEXT PRIMARY KEY,
                    mainPlaytime INTEGER,
                    tournamentPlaytime INTEGER
                )
                """;
        try(PreparedStatement statement = dbConnection.prepareStatement(playtimeTable)) {
            statement.executeUpdate();
        }

        //Creating the tournament timestamps table
        String tournamentTimestampsTable = """
                CREATE TABLE IF NOT EXISTS tournamentTimestamps (
                    tournamentStart BIGINT,
                    duration BIGINT,
                    tournamentEnd BIGINT
                )
                """;
        try(PreparedStatement statement = dbConnection.prepareStatement(tournamentTimestampsTable)){
            statement.executeUpdate();
        }

        //Creating the reward pending table (in case a tournament winner is not online when giving out the rewards)
        String pendingRewardsTable = """
                CREATE TABLE IF NOT EXISTS pendingRewards (
                    uuid TEXT PRIMARY KEY,
                    placement INTEGER PRIMARY KEY,
                    amount INTEGER
                )
                """;
        try(PreparedStatement statement = dbConnection.prepareStatement(pendingRewardsTable)){
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
            plugin.getLogger().severe("[PlaytimeUtils] Failed to get player main playtime: " + e.getMessage());
        }

        return 0;
    }

    public List<Map.Entry<UUID, Integer>> getMainTop3Players(){
        Map<UUID, Integer> top3PlayersSeconds = new HashMap<>();

        //Getting the map in seconds
        String statement = "SELECT mainPlaytime, uuid FROM playersPlaytime ORDER BY mainPlaytime DESC LIMIT 3";
        try(PreparedStatement ps = dbConnection.prepareStatement(statement)){
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    UUID playerUUID = UUID.fromString(rs.getString("uuid"));
                    int playtime = rs.getInt("mainPlaytime") + plugin.getPlaytimeMap().getOrDefault(playerUUID, 0);
                    top3PlayersSeconds.put(playerUUID, playtime);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("[PlaytimeUtils] Failed to get Main top 3 players: " + e.getMessage());
            return null;
        }

        //Sorting the map by seconds
        List<Map.Entry<UUID, Integer>> sortedList = new ArrayList<>(top3PlayersSeconds.entrySet());
        sortedList.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        if(sortedList.size() > 3) sortedList.subList(0, 3);

        return sortedList;
    }

    public List<Map.Entry<UUID, Integer>> getTournamentTop3Players(){
        Map<UUID, Integer> top3PlayersSeconds = new HashMap<>();

        String statement = "SELECT tournamentPlaytime, uuid FROM playersPlaytime ORDER BY tournamentPlaytime DESC LIMIT 3";
        try(PreparedStatement ps = dbConnection.prepareStatement(statement)){
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    UUID playerUUID = UUID.fromString(rs.getString("uuid"));
                    int playtime = rs.getInt("tournamentPlaytime") + plugin.getPlaytimeMap().getOrDefault(playerUUID, 0);
                    top3PlayersSeconds.put(playerUUID, playtime);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("[PlaytimeUtils] Failed to get Tournament top 3 players: " + e.getMessage());
            return null;
        }

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
            plugin.getLogger().severe("[PlaytimeUtils] Failed to check if player is registered: " + e.getMessage());
        }

        return false;
    }

    public void createPlayerRow(UUID playerUUID){
        String statement = "INSERT INTO playersPlaytime (uuid, mainPlaytime, tournamentPlaytime) VALUES (?, ?, ?)";
        try(PreparedStatement ps = dbConnection.prepareStatement(statement)){
            ps.setString(1, playerUUID.toString());
            ps.setInt(2, 0);
            ps.setInt(3, 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[PlaytimeUtils] Failed to create player row in Playtime Table: " + e.getMessage());
        }
    }

    public int getTournamentPlaytime(UUID playerUUID){
        String SQL = "SELECT tournamentPlaytime FROM playersPlaytime WHERE uuid = ?";
        try(PreparedStatement ps = dbConnection.prepareStatement(SQL)){
            ps.setString(1, playerUUID.toString());
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return rs.getInt("tournamentPlaytime");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[PlaytimeUtils] Failed to get player tournament playtime: " + e.getMessage());
        }
        return 0;
    }
    public String getTournamentPlaytimeString(UUID playerUUID){
        //Getting the playtime of the player from the db
        long seconds = getTournamentPlaytime(playerUUID);

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

    public void wipeTournamentPlaytime(){
        String SQL = "UPDATE playersPlaytime SET tournamentPlaytime = 0";
        try(PreparedStatement ps = dbConnection.prepareStatement(SQL)){
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[PlaytimeUtils] Failed to wipe tournament playtime: " + e.getMessage());
        }
    }

    public void updatePlayerMainPlaytime(UUID playerUUID, int seconds){
        String statement = "UPDATE playersPlaytime SET mainPlaytime = ? WHERE uuid = ?";
        try(PreparedStatement ps = dbConnection.prepareStatement(statement)){
            ps.setInt(1, seconds + getMainPlaytime(playerUUID));
            ps.setString(2, playerUUID.toString());

            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[PlaytimeUtils] Failed to update player main playtime: " + e.getMessage());
        }
    }

    public void updatePlayerTournamentPlaytime(UUID playerUUID, int seconds){
        String statement = "UPDATE playersPlaytime SET tournamentPlaytime = ? WHERE uuid = ?";
        try(PreparedStatement ps = dbConnection.prepareStatement(statement)){
            ps.setInt(1, seconds + getTournamentPlaytime(playerUUID));
            ps.setString(2, playerUUID.toString());

            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[PlaytimeUtils] Failed to update player tournament playtime: " + e.getMessage());
        }
    }

    public void setTournamentTimestamps(long tournamentStart, long duration, long tournamentEnd){
        String statement = "INSERT INTO tournamentTimestamps (tournamentStart, duration, tournamentEnd) VALUES (?, ?, ?)";
        try(PreparedStatement ps = dbConnection.prepareStatement(statement)){
            ps.setLong(1, tournamentStart);
            ps.setLong(2, duration);
            ps.setLong(3, tournamentEnd);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[PlaytimeUtils] Failed to set tournament timestamps: " + e.getMessage());
        }
    }
    public void deleteTournamentTimestamps(){
        String statement = "DELETE FROM tournamentTimestamps";
        try(PreparedStatement ps = dbConnection.prepareStatement(statement)){
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[PlaytimeUtils] Failed to delete tournament timestamps: " + e.getMessage());
        }
    }
    public long getTournamentTimestamp(String option){
        String statement = "SELECT " + option + " FROM tournamentTimestamps";
        try(PreparedStatement ps = dbConnection.prepareStatement(statement)){
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return rs.getLong(option);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[PlaytimeUtils] Failed to get tournament timestamp: " + e.getMessage());
        }
        return 0;
    }

    public int getPendingRewardAmount(UUID playerUUID, int placement){
        String statement = "SELECT amount FROM pendingRewards WHERE uuid = ? AND placement = ?";
        try(PreparedStatement ps = dbConnection.prepareStatement(statement)){
            ps.setString(1, playerUUID.toString());
            ps.setInt(2, placement);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return rs.getInt("amount");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[PlaytimeUtils] Failed to get pending reward amount: " + e.getMessage());
        }
        return 0;
    }
    public void insertPendingReward(UUID playerUUID, int placement){
        int pendingAmount = getPendingRewardAmount(playerUUID, placement);

        if(pendingAmount == 0){
            String statement = "INSERT INTO pendingRewards (uuid, placement, amount) VALUES (?, ?, ?)";
            try(PreparedStatement ps = dbConnection.prepareStatement(statement)){
                ps.setString(1, playerUUID.toString());
                ps.setInt(2, placement);
                ps.setInt(3, 1);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("[PlaytimeUtils] Failed to insert pending reward: " + e.getMessage());
            }
        }
        else{
            String statement = "UPDATE pendingRewards SET amount = ? WHERE uuid = ? AND placement = ?";
            try(PreparedStatement ps = dbConnection.prepareStatement(statement)){
                ps.setInt(1, pendingAmount + 1);
                ps.setString(2, playerUUID.toString());
                ps.setInt(3, placement);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("[PlaytimeUtils] Failed to update pending reward amount: " + e.getMessage());
            }
        }
    }
    public void removePendingReward(UUID playerUUID, int placement){
        int pendingAmount = getPendingRewardAmount(playerUUID, placement);

        if(pendingAmount == 1){
            String statement = "DELETE FROM pendingRewards WHERE uuid = ? AND placement = ?";
            try(PreparedStatement ps = dbConnection.prepareStatement(statement)){
                ps.setString(1, playerUUID.toString());
                ps.setInt(2, placement);
                ps.executeUpdate();
            }catch (Exception e){
                plugin.getLogger().severe("[PlaytimeUtils] Failed to remove player from pending rewards list: " + e.getMessage());
            }
        }
        else{
            String statement = "UPDATE pendingRewards SET amount = ? WHERE uuid = ? AND placement = ?";
            try(PreparedStatement ps = dbConnection.prepareStatement(statement)){
                ps.setInt(1, pendingAmount - 1);
                ps.setString(2, playerUUID.toString());
                ps.setInt(3, placement);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("[PlaytimeUtils] Failed to update pending reward amount: " + e.getMessage());
            }
        }
    }
}
