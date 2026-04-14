//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandManager implements CommandExecutor {
    private final PlaytimeUtils plugin;

    public CommandManager(PlaytimeUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        Player player = (Player) sender;
        String noPermissionMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to do that!"));
        noPermissionMessage = PlaceholderAPI.setPlaceholders(player, noPermissionMessage);
        DbManager dbManager = plugin.getDatabaseManager();

        if(command.getName().equalsIgnoreCase("myplaytime")){
            //Checking if the sender has permission
            if(player.hasPermission("playtimeutils.myplaytime")){
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                player.sendMessage(noPermissionMessage);
                return true;
            }

            String playtimeMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.your-playtime", "&aYour playtime is &e&l%playtime% &a!"));
            playtimeMessage = PlaceholderAPI.setPlaceholders(player, playtimeMessage);
            player.sendMessage(playtimeMessage.replace("%playtime%", dbManager.getPlaytime(player.getUniqueId())));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            return true;
        }

        if(command.getName().equalsIgnoreCase("playtime")){
            //Checking if the sender has permission
            if(player.hasPermission("playtimeutils.playtime")){
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                player.sendMessage(noPermissionMessage);
                return true;
            }

            if(args.length == 0){
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUsage: &l/playtime <player>"));
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                return true;
            }

            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[0]);
            if(!targetPlayer.hasPlayedBefore()){
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aPlayer &e"+targetPlayer.getName()+" &ahas never played before!"));
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                return true;
            }

            String playtimeMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.player-playtime", "&e%player%'s playtime is &e&l%playtime% &a!"));
            playtimeMessage = playtimeMessage.replace("%player%", targetPlayer.getName())
                    .replace("%playtime%", dbManager.getPlaytime(targetPlayer.getUniqueId()));
            playtimeMessage = PlaceholderAPI.setPlaceholders(targetPlayer, playtimeMessage);
            player.sendMessage(playtimeMessage);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            return true;
        }

        return false;
    }
}
