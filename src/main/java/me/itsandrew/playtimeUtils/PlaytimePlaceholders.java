package me.itsandrew.playtimeUtils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaytimePlaceholders extends PlaceholderExpansion {
    private final PlaytimeUtils plugin;

    public PlaytimePlaceholders(PlaytimeUtils plugin) {
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
        //Use '%playtime_mainValue%' to display the playtime of a player
        if(params.equalsIgnoreCase("mainValue")) {
            return plugin.getPlaceholdersManager().getMainPlaytime(player.getUniqueId());
        }

        //Use '%playtime_tournamentValue%' to display the tournament playtime of a player
        if(params.equalsIgnoreCase("tournamentValue")) {
            return plugin.getPlaceholdersManager().getTournamentPlaytime(player.getUniqueId());
        }

        //Use '%playtime_top1ign%' to display the top 1 player IGN
        if(params.equalsIgnoreCase("top1ign")) {
            return plugin.getPlaceholdersManager().getCachedTop1IGN();
        }
        //Use '%playtime_top1tournamentign' to display the top 1 tournament player IGN
        if(params.equalsIgnoreCase("top1tournamentign")) {
            return plugin.getPlaceholdersManager().getCachedTop1TournamentIGN();
        }
        //Use '%playtime_top1tournament' to display the top 1 player tournament playtime
        if(params.equalsIgnoreCase("top1tournament")) {
            return plugin.getPlaceholdersManager().getCachedTop1TournamentPlaytime();
        }
        //Use '%playtime_top1%' to display the top 1 player main playtime
        if(params.equalsIgnoreCase("top1")) {
            return plugin.getPlaceholdersManager().getCachedTop1Playtime();
        }


        //Use '%playtime_top2ign%' to display the top 2 player IGN
        if(params.equalsIgnoreCase("top2ign")) {
            return plugin.getPlaceholdersManager().getCachedTop2IGN();
        }
        //Use '%playtime_top2tournamentign%' to display the top 2 tournament player IGN
        if(params.equalsIgnoreCase("top2tournamentign")) {
            return plugin.getPlaceholdersManager().getCachedTop2TournamentIGN();
        }
        //Use '%playtime_top2tournament%' to display the top 2 player tournament playtime
        if(params.equalsIgnoreCase("top2tournament")) {
            return plugin.getPlaceholdersManager().getCachedTop2TournamentPlaytime();
        }
        //Use '%playtime_top2%' to display the top 2 player main playtime
        if(params.equalsIgnoreCase("top2")) {
            return plugin.getPlaceholdersManager().getCachedTop2Playtime();
        }

        //Use '%playtime_top3tournamentign%' to display the top 3 tournament player IGN
        if(params.equalsIgnoreCase("top3tournamentign")) {
            return plugin.getPlaceholdersManager().getCachedTop3TournamentIGN();
        }
        //Use '%playtime_top3ign%' to display the top 3 player IGN
        if(params.equalsIgnoreCase("top3ign")) {
            return plugin.getPlaceholdersManager().getCachedTop3IGN();
        }
        //Use '%playtime_top3tournament%' to display the top 3 player tournament playtime
        if(params.equalsIgnoreCase("top3tournament")) {
            return plugin.getPlaceholdersManager().getCachedTop3TournamentPlaytime();
        }
        //Use '%playtime_top3' to display the top 3 player main playtime
        if(params.equalsIgnoreCase("top3")) {
            return plugin.getPlaceholdersManager().getCachedTop3Playtime();
        }

        return null;
    }
}
