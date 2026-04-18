package me.itsandrew.playtimeUtils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
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

        List<Map.Entry<UUID, Integer>> top3Map = plugin.getDatabaseManager().getTop3Players();
        //Use '%playtime_top1ign%' to display the top 1 player IGN
        if(params.equalsIgnoreCase("top1ign")) {
            try{
                Map.Entry<UUID, Integer> top1 = top3Map.getFirst();
                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(top1.getKey());
                return targetPlayer.getName();
            } catch (Exception e){
                return " ";
            }
        }
        //Use '%playtime_top1%' to display the top 1 player playtime
        if(params.equalsIgnoreCase("top1")) {
            try{
                Map.Entry<UUID, Integer> top1 = top3Map.getFirst();
                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(top1.getKey());
                return plugin.getDatabaseManager().getPlaytimeString(targetPlayer.getUniqueId());
            }
            catch (Exception e){
                return " ";
            }
        }


        //Use '%playtime_top2ign%' to display the top 2 player IGN
        if(params.equalsIgnoreCase("top2ign")) {
            try{
                Map.Entry<UUID, Integer> top2 = top3Map.get(1);
                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(top2.getKey());
                return targetPlayer.getName();
            }catch (Exception e){
                return " ";
            }
        }
        //Use '%playtime_top2%' to display the top 2 player playtime
        if(params.equalsIgnoreCase("top2")) {
            try{
                Map.Entry<UUID, Integer> top2 = top3Map.get(1);
                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(top2.getKey());
                return plugin.getDatabaseManager().getPlaytimeString(targetPlayer.getUniqueId());
            } catch (Exception e){
                return " ";
            }
        }

        //Use '%playtime_top3ign%' to display the top 3 player IGN
        if(params.equalsIgnoreCase("top3ign")) {
            try{
                Map.Entry<UUID, Integer> top3 = top3Map.get(2);
                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(top3.getKey());
                return targetPlayer.getName();
            } catch (Exception e){
                return " ";
            }
        }
        //Use '%playtime_top3' to display the top 3 player playtime
        if(params.equalsIgnoreCase("top3")) {
            try{
                Map.Entry<UUID, Integer> top3 = top3Map.get(2);
                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(top3.getKey());
                return plugin.getDatabaseManager().getPlaytimeString(targetPlayer.getUniqueId());
            } catch (Exception e){
                return " ";
            }
        }

        return null;
    }
}
