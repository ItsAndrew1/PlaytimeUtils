//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {
    private final PlaytimeUtils plugin;

    public PlayerJoin(PlaytimeUtils plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        //Checking if the player is registered or not in the db
        boolean toggleFirstJoin = plugin.getConfig().getBoolean("first-join.toggle", true);
        if(!plugin.getDatabaseManager().isPlayerRegistered(player.getUniqueId()) && toggleFirstJoin){
            String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("first-join.title", "&aWelcome to the server!"));
            title = PlaceholderAPI.setPlaceholders(player, title);

            String subtitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("first-join.subtitle", "&aEnjoy your stay!"));
            subtitle = PlaceholderAPI.setPlaceholders(player, subtitle);

            player.sendTitle(title, subtitle);


            Sound firstJoinSound = Registry.SOUNDS.get(new NamespacedKey(plugin, plugin.getConfig().getString("first-join.sound", "ENTITY_PLAYER_LEVELUP").toUpperCase()));
            float fjsVolume = plugin.getConfig().getInt("first-join.sound-volume", 1);
            float fjsPitch = plugin.getConfig().getInt("first-join.sound-pitch", 1);
            player.playSound(player.getLocation(), firstJoinSound, fjsVolume, fjsPitch);
        }

        //Putting the player in the maps
        plugin.getLastActivity().put(player.getUniqueId(), System.currentTimeMillis());
        plugin.getPlaytimeMap().put(player.getUniqueId(), 0);
    }
}
