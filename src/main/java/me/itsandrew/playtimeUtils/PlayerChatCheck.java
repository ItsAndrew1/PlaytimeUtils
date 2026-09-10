package me.itsandrew.playtimeUtils;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerChatCheck implements Listener {
    private final PlaytimeUtils plugin;

    public PlayerChatCheck(PlaytimeUtils plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        //TO DO: Continue Here
    }

    private boolean playerHasNecessaryPlaytime(Player player){
        int secondsNeededForChat = plugin.getConfig().getInt("playtime-needed-for-chat", 600);
        return plugin.getMainPlaytimeMap().get(player.getUniqueId()) >= secondsNeededForChat;
    }
}
