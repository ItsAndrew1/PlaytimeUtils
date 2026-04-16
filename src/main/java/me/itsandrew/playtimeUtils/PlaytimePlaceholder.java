package me.itsandrew.playtimeUtils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaytimePlaceholder extends PlaceholderExpansion {
    private final PlaytimeUtils plugin;

    public  PlaytimePlaceholder(PlaytimeUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "playtime";
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        //Use '%playtime_value%' to display the playtime of a player
        if(params.equalsIgnoreCase("value")) {
            return plugin.getDatabaseManager().getPlaytimeString(player.getUniqueId()) + plugin.getPlaytimeMap().get(player.getUniqueId());
        }

        return null;
    }
}
