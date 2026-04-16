//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//Main plugin class.
public final class PlaytimeUtils extends JavaPlugin implements Listener {
    private DbManager databaseManager;

    private Map<UUID, Integer> playtimeMap = new HashMap<>();
    private Map<UUID, Long> lastActivity = new HashMap<>();
    private Map<UUID, Boolean> afkMap = new HashMap<>();

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
        getServer().getPluginManager().registerEvents(this, this);

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
            for(Player player : Bukkit.getOnlinePlayers()){
                //Adding the player to the playtime map if they aren't already
                if(!playtimeMap.containsKey(player.getUniqueId())){
                    playtimeMap.put(player.getUniqueId(), 0);
                    continue;
                }

                //Adding the player in the last activity map
                lastActivity.put(player.getUniqueId(), 0L);

                //Skipping if the player is already AFK
                if(afkMap.containsKey(player.getUniqueId())) continue;

                //Checking if the player is now AFK (4 secs)
                if(isPlayerAFK(player.getUniqueId())){
                    String chatMessage = getConfig().getString("messages.player-afk", "&7You are now AFK! Note that the playtime won't be counted while you are AFK.");
                    chatMessage = PlaceholderAPI.setPlaceholders(player, chatMessage);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatMessage));

                    afkMap.put(player.getUniqueId(), true);
                    continue;
                }

                playtimeMap.compute(player.getUniqueId(), (k, playtime) -> playtime + 1);


                //This was done for testing purposes
                String message = "Playtime: %playtime_value%";
                message = PlaceholderAPI.setPlaceholders(player, message);
                player.sendMessage(message);
            }
        }, 0, 20);
    }

    @Override
    public void onDisable() {
        saveConfig();

        //Saving the playtime of all players to the database
        for(UUID playerUUID : playtimeMap.keySet()){
            databaseManager.updatePlayerPlaytime(playerUUID, databaseManager.getPlaytime(playerUUID) + playtimeMap.get(playerUUID));
        }

        getLogger().info("[PlaytimeUtils] Plugin disabled successfully.");
    }

    private boolean isPlayerAFK(UUID playerUUID){
        int afkSeconds = getConfig().getInt("afk-seconds", 4);
        return System.currentTimeMillis() - lastActivity.get(playerUUID) > afkSeconds * 1000L;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        //Checking if the player is already AFK
        if(isPlayerAFK(event.getPlayer().getUniqueId())){
            String message = getConfig().getString("messages.player-no-more-afk", "&7You are not AFK anymore.");
            message = PlaceholderAPI.setPlaceholders(event.getPlayer(), message);
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', message));

            afkMap.remove(event.getPlayer().getUniqueId());
        }

        //Checking if the player changes their location or looks around.
        if(event.getFrom().distanceSquared(event.getTo()) > 0 ||
                event.getFrom().getYaw() != event.getTo().getYaw() ||
                event.getFrom().getPitch() != event.getTo().getPitch()
        ){
            lastActivity.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
            event.getPlayer().sendMessage("Salut");
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event){
        //Checking if the player is already AFK
        if(isPlayerAFK(event.getPlayer().getUniqueId())){
            String message = getConfig().getString("messages.player-no-more-afk", "&7You are not AFK anymore.");
            message = PlaceholderAPI.setPlaceholders(event.getPlayer(), message);
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', message));

            afkMap.remove(event.getPlayer().getUniqueId());
        }

        lastActivity.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();

        //Saving the playtime in the database
        if(databaseManager.isPlayerRegistered(player.getUniqueId())) databaseManager.updatePlayerPlaytime(player.getUniqueId(), playtimeMap.get(player.getUniqueId()));
        else{
            databaseManager.createPlayerRow(player.getUniqueId());
            databaseManager.updatePlayerPlaytime(player.getUniqueId(), playtimeMap.get(player.getUniqueId()));
        }

        //Removing the player from the Map
        playtimeMap.remove(player.getUniqueId());
    }

    //Getters
    public DbManager getDatabaseManager() {
        return databaseManager;
    }
    public Map<UUID, Integer> getPlaytimeMap() {
        return playtimeMap;
    }
}
