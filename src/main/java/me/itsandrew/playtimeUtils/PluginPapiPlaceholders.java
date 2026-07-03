package me.itsandrew.playtimeUtils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PluginPapiPlaceholders extends PlaceholderExpansion {
    private final PlaytimeUtils plugin;

    public PluginPapiPlaceholders(PlaytimeUtils plugin) {
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
            return plugin.getDatabaseManager().getMainPlaytimeString(player.getUniqueId());
        }

        //Getting the top 3 players from the database (main or tournament)
        List<Map.Entry<UUID, Integer>> top3MainPlaytimeMap = plugin.getDatabaseManager().getMainTop3Players();
        List<Map.Entry<UUID, Integer>> top3TournamentPlaytimeMap = plugin.getDatabaseManager().getTournamentTop3Players();

        //Use '%playtime_top1ign%' to display the top 1 player IGN
        if(params.equalsIgnoreCase("top1ign")) {
            try{
                Map.Entry<UUID, Integer> top1 = top3MainPlaytimeMap.getFirst();
                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(top1.getKey());
                return targetPlayer.getName();
            } catch (Exception e){
                return " ";
            }
        }
        //Use '%playtime_top1tournamentign' to display the top 1 tournament player IGN
        if(params.equalsIgnoreCase("top1tournamentign")) {
            try{
                Map.Entry<UUID, Integer> top1 = top3TournamentPlaytimeMap.getFirst();
                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(top1.getKey());
                return targetPlayer.getName();
            } catch (Exception e){
                return " ";
            }
        }
        //Use '%playtime_top1tournament' to display the top 1 player tournament playtime
        if(params.equalsIgnoreCase("top1tournament")) {
            try{
                Map.Entry<UUID, Integer> top1 = top3TournamentPlaytimeMap.getFirst();
                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(top1.getKey());
                return plugin.getDatabaseManager().getRewardPlaytimeString(targetPlayer.getUniqueId());
            } catch (Exception e){
                return " ";
            }
        }
        //Use '%playtime_top1%' to display the top 1 player main playtime
        if(params.equalsIgnoreCase("top1")) {
            try{
                Map.Entry<UUID, Integer> top1 = top3MainPlaytimeMap.getFirst();
                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(top1.getKey());
                return plugin.getDatabaseManager().getMainPlaytimeString(targetPlayer.getUniqueId());
            }
            catch (Exception e){
                return " ";
            }
        }


        //Use '%playtime_top2ign%' to display the top 2 player IGN
        if(params.equalsIgnoreCase("top2ign")) {
            try{
                Map.Entry<UUID, Integer> top2 = top3MainPlaytimeMap.get(1);
                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(top2.getKey());
                return targetPlayer.getName();
            }catch (Exception e){
                return " ";
            }
        }
        //Use '%playtime_top2tournamentign%' to display the top 2 tournament player IGN
        if(params.equalsIgnoreCase("top2tournamentign")) {
            try{
                Map.Entry<UUID, Integer> top2 = top3TournamentPlaytimeMap.get(1);
                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(top2.getKey());
                return targetPlayer.getName();
            } catch (Exception e){
                return " ";
            }
        }
        //Use '%playtime_top2tournament%' to display the top 2 player tournament playtime
        if(params.equalsIgnoreCase("top2tournament")) {
            try{
                Map.Entry<UUID, Integer> top2 = top3TournamentPlaytimeMap.get(1);
                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(top2.getKey());
                return plugin.getDatabaseManager().getRewardPlaytimeString(targetPlayer.getUniqueId());
            } catch (Exception e){
                return " ";
            }
        }
        //Use '%playtime_top2%' to display the top 2 player main playtime
        if(params.equalsIgnoreCase("top2")) {
            try{
                Map.Entry<UUID, Integer> top2 = top3MainPlaytimeMap.get(1);
                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(top2.getKey());
                return plugin.getDatabaseManager().getMainPlaytimeString(targetPlayer.getUniqueId());
            } catch (Exception e){
                return " ";
            }
        }

        //Use '%playtime_top3tournamentign%' to display the top 3 tournament player IGN
        if(params.equalsIgnoreCase("top3tournamentign")) {
            try{
                Map.Entry<UUID, Integer> top3 = top3TournamentPlaytimeMap.get(2);
                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(top3.getKey());
                return targetPlayer.getName();
            } catch (Exception e){
                return " ";
            }
        }
        //Use '%playtime_top3ign%' to display the top 3 player IGN
        if(params.equalsIgnoreCase("top3ign")) {
            try{
                Map.Entry<UUID, Integer> top3 = top3MainPlaytimeMap.get(2);
                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(top3.getKey());
                return targetPlayer.getName();
            } catch (Exception e){
                return " ";
            }
        }
        //Use '%playtime_top3tournament%' to display the top 3 player tournament playtime
        if(params.equalsIgnoreCase("top3tournament")) {
            try{
                Map.Entry<UUID, Integer> top3 = top3TournamentPlaytimeMap.get(2);
                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(top3.getKey());
                return plugin.getDatabaseManager().getRewardPlaytimeString(targetPlayer.getUniqueId());
            } catch (Exception e){
                return " ";
            }
        }
        //Use '%playtime_top3' to display the top 3 player main playtime
        if(params.equalsIgnoreCase("top3")) {
            try{
                Map.Entry<UUID, Integer> top3 = top3MainPlaytimeMap.get(2);
                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(top3.getKey());
                return plugin.getDatabaseManager().getMainPlaytimeString(targetPlayer.getUniqueId());
            } catch (Exception e){
                return " ";
            }
        }

        return null;
    }
}
