//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

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
            for(Player player: Bukkit.getOnlinePlayers()){
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
        return System.currentTimeMillis() - lastActivity.get(playerUUID) > 4000;
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
        ) lastActivity.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
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

    //Getters
    public DbManager getDatabaseManager() {
        return databaseManager;
    }
}
