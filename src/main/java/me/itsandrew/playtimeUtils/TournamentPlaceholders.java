package me.itsandrew.playtimeUtils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class TournamentPlaceholders extends PlaceholderExpansion {
    private final PlaytimeUtils plugin;

    public TournamentPlaceholders(PlaytimeUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "tournament";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NonNull String params){
        //Use '%tournament_duration%' to display the formatted duration (e.g.: 5d 12h 30m)
        if(params.equalsIgnoreCase("duration")){
            return plugin.getPlaceholdersManager().getCachedTournamentDuration();
        }

        //Use '%tournament_enddate%' to display the formatted date (e.g.: 24 Mar 2026)
        if(params.equalsIgnoreCase("enddate")){
            return plugin.getPlaceholdersManager().getCachedTournamentEndDate();
        }

        return null;
    }
}
