package me.itsandrew.playtimeUtils;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.concurrent.TimeUnit;

public class PlayerChatCheck implements Listener {
    private final PlaytimeUtils plugin;

    public PlayerChatCheck(PlaytimeUtils plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        boolean toggleNecessaryPlaytime = plugin.getConfig().getBoolean("toggle-needed-playtime-to-chat", false);
        if(!toggleNecessaryPlaytime) return;

        if(playerHasNecessaryPlaytime(event.getPlayer())) return;
        event.setCancelled(true);

        int secondsNeededForChat = plugin.getConfig().getInt("playtime-needed-to-chat", 600);
        String neededPlaytimeString = getNeededPlaytimeString(secondsNeededForChat);

        String rawMessage = plugin.getConfig().getString("not-enough-playtime-message", "&cYou need &l%needed_playtime% &cof playtime to chat!")
                .replace("%needed_playtime%", neededPlaytimeString);
        rawMessage = PlaceholderAPI.setPlaceholders(event.getPlayer(), rawMessage);
        Component neededPlaytimeMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(rawMessage);
        event.getPlayer().sendMessage(neededPlaytimeMessage);

        float soundVolume = plugin.getConfig().getInt("nps-volume", 1);
        float soundPitch = plugin.getConfig().getInt("nps-pitch", 1);
        Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(plugin.getConfig().getString("needed-playtime-sound", "entity.enderman.teleport").toLowerCase()));
        event.getPlayer().playSound(event.getPlayer().getLocation(), sound, soundVolume, soundPitch);
    }

    private String getNeededPlaytimeString(int seconds){
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

    private boolean playerHasNecessaryPlaytime(Player player){
        int secondsNeededForChat = plugin.getConfig().getInt("playtime-needed-to-chat", 600);
        return plugin.getMainPlaytimeMap().get(player.getUniqueId()) >= secondsNeededForChat;
    }
}
