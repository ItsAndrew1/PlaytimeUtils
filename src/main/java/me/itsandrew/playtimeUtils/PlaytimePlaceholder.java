package me.itsandrew.playtimeUtils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class PlaytimePlaceholder extends PlaceholderExpansion {
    private final PlaytimeUtils plugin;

    public PlaytimePlaceholder(PlaytimeUtils plugin) {
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
            return plugin.getDatabaseManager().getPlaytimeString(player.getUniqueId());
        }

        Map<UUID, String> top3Map = plugin.getDatabaseManager().getTop3Players();
        //Use '%playtime_top1ign%' to display the top 1 player IGN
        if(params.equalsIgnoreCase("top1ign")) {
            Map.Entry<UUID, String> top1 = top3Map.entrySet().stream().toList().getFirst();
            if(top1 == null) return " ";

            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(top1.getKey());
            return targetPlayer.getName();
        }
        //Use '%playtime_top1%' to display the top 1 player playtime
        if(params.equalsIgnoreCase("top1")) {
            Map.Entry<UUID, String> top1 = top3Map.entrySet().stream().toList().getFirst();
            if(top1 == null) return " ";

            return top1.getValue();
        }


        //Use '%playtime_top2ign%' to display the top 2 player IGN
        if(params.equalsIgnoreCase("top2ign")) {
            Map.Entry<UUID, String> top2 = top3Map.entrySet().stream().toList().get(1);
            if(top2 == null) return " ";

            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(top2.getKey());
            return targetPlayer.getName();
        }
        //Use '%playtime_top2%' to display the top 2 player playtime
        if(params.equalsIgnoreCase("top2")) {
            Map.Entry<UUID, String> top2 = top3Map.entrySet().stream().toList().get(1);
            if(top2 == null) return " ";
            return top2.getValue();
        }

        //Use '%playtime_top3ign%' to display the top 3 player IGN
        if(params.equalsIgnoreCase("top3ign")) {
            Map.Entry<UUID, String> top3 = top3Map.entrySet().stream().toList().get(2);
            if(top3 == null) return " ";

            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(top3.getKey());
            return targetPlayer.getName();
        }
        //Use '%playtime_top3' to display the top 3 player playtime
        if(params.equalsIgnoreCase("top3")) {
            Map.Entry<UUID, String> top3 = top3Map.entrySet().stream().toList().get(2);
            if(top3 == null) return " ";
            return top3.getValue();
        }



        return null;
    }
}
