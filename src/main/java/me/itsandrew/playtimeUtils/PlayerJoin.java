//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

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
        if(!plugin.getDatabaseManager().isPlayerRegistered(player.getUniqueId())){
            plugin.getDatabaseManager().createPlayerRow(player.getUniqueId());

            if(toggleFirstJoin){
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
        }

        //Putting the player in the maps
        plugin.getLastActivity().put(player.getUniqueId(), System.currentTimeMillis());
        plugin.getPlaytimeMap().put(player.getUniqueId(), 0);

        //Opening the pending reward book if the player has any pending rewards
        boolean toggleRewardSystem = plugin.getConfig().getBoolean("reward-system.toggle", false);
        if(toggleRewardSystem){
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                int pendingFirstPlace = plugin.getDatabaseManager().getPendingRewardAmount(player.getUniqueId(), 1);
                int pendingSecondPlace = plugin.getDatabaseManager().getPendingRewardAmount(player.getUniqueId(), 2);
                int pendingThirdPlace = plugin.getDatabaseManager().getPendingRewardAmount(player.getUniqueId(), 3);

                if(pendingFirstPlace != 0 || pendingSecondPlace != 0 || pendingThirdPlace != 0){
                    //Opens the book
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        String title = plugin.getConfig().getString("reward-system.pending-reward-book.title");
                        String author = plugin.getConfig().getString("reward-system.pending-reward-book.author");
                        List<Component> pages = new ArrayList<>();
                        List<?> rawPages = plugin.getConfig().getList("reward-system.pending-reward-book.pages");

                        if(title == null || author == null || rawPages == null){
                            plugin.getLogger().severe("[PlaytimeUtils] The pending reward book is missing a required parameter! Set the book again using '/ptutils rewards tournament setbook'");
                            return;
                        }

                        //Adding the pages of the book
                        for(Object page : rawPages){
                            if(page instanceof Component) pages.add((Component) page);
                        }

                        Book finalBook = Book.book(Component.text(title), Component.text(author), pages);
                        player.openBook(finalBook);
                    });
                }
            });
        }
    }
}
