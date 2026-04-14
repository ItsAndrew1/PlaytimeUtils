//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//Main plugin class.
public final class PlaytimeUtils extends JavaPlugin {
    private DbManager databaseManager;
    private Map<UUID, Integer> playtimeMap = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        //Creating the necessary objects.
        databaseManager = new DbManager(this);

        //Registering commands.
        getCommand("myplaytime").setExecutor(new CommandManager(this));
        getCommand("playtime").setExecutor(new CommandManager(this));

        //Registering events.
        getServer().getPluginManager().registerEvents(new PlayerJoin(this), this);

        //Connecting the database
        try{
            if(!databaseManager.connectDb()){
                getLogger().severe("[PlaytimeUtils] Failed to connect to the database. Shutting down the plugin.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        } catch (Exception e){
            getLogger().severe("[PlaytimeUtils] Failed to connect to the database. Shutting down the plugin. See message below for more details: ");
            getLogger().severe("[PlaytimeUtils] " + e.getMessage());
            return;
        }

        getLogger().info("[PlaytimeUtils] Plugin enabled successfully.");

        //Starting the task to track the playtime of players
        getServer().getScheduler().runTaskTimer(this, () -> {
            for(Player player: Bukkit.getOnlinePlayers()){


                playtimeMap.compute(player.getUniqueId(), (k, playtime) -> playtime + 1);
            }
        }, 0, 20);
    }

    @Override
    public void onDisable() {
        saveConfig();

        //Saving the playtime of all players to the database
        for(UUID playerUUID : playtimeMap.keySet()){
            databaseManager.updatePlayerPlaytime(playerUUID, playtimeMap.get(playerUUID));
        }

        getLogger().info("[PlaytimeUtils] Plugin disabled successfully.");
    }

    //Getters
    public DbManager getDatabaseManager() {
        return databaseManager;
    }
}
