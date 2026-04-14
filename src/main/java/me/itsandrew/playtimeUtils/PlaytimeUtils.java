//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils;

import org.bukkit.plugin.java.JavaPlugin;

//Main plugin class.
public final class PlaytimeUtils extends JavaPlugin {
    private DbManager databaseManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        //Creating the needed objects.
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
    }

    @Override
    public void onDisable() {
        saveConfig();
        getLogger().info("[PlaytimeUtils] Plugin disabled successfully.");
    }

    //Getters
    public DbManager getDatabaseManager() {
        return databaseManager;
    }
}
