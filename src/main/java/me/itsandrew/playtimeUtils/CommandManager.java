//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils;

import com.mojang.brigadier.arguments.StringArgumentType;
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

import java.util.List;

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

        if(command.getName().equalsIgnoreCase("myplaytime")){
            //Checking if the sender has permission
            if(!player.hasPermission("playtimeutils.myplaytime")){
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                player.sendMessage(noPermissionMessage);
                return true;
            }

            String playtimeMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.my-playtime", "&aYour playtime is &e&l%playtime_value%&a!"));
            playtimeMessage = PlaceholderAPI.setPlaceholders(player, playtimeMessage);
            player.sendMessage(playtimeMessage);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            return true;
        }

        if(command.getName().equalsIgnoreCase("playtime")){
            //Checking if the sender has permission
            if(!player.hasPermission("playtimeutils.playtime")){
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                player.sendMessage(noPermissionMessage);
                return true;
            }

            if(args.length < 1){
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

            String playtimeMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.player-playtime", "&e%player%'s playtime is &e&l%playtime_value% &a!"));
            playtimeMessage = PlaceholderAPI.setPlaceholders(targetPlayer, playtimeMessage);
            player.sendMessage(playtimeMessage.replace("%player%", targetPlayer.getName()));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            return true;
        }

        if(command.getName().equalsIgnoreCase("topplaytime")){
            //Checking if the player has permission
            if(!player.hasPermission("playtimeutils.topplaytime")){
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                player.sendMessage(noPermissionMessage);
                return true;
            }

            List<String> rawMessage = plugin.getConfig().getStringList("messages.top-3-players");
            for(String line : rawMessage){
                line = ChatColor.translateAlternateColorCodes('&', line);
                line = PlaceholderAPI.setPlaceholders(player, line);
                player.sendMessage(line);
            }
        }

        if(command.getName().equalsIgnoreCase("ptutilsreload")){
            //Checking if the player has permission.
            if(!player.hasPermission("playtimeutils.reload")){
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                player.sendMessage(noPermissionMessage);
                return true;
            }

            plugin.reloadConfig();
            String chatPrefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("chat-prefix", "&f&l[&e&lPUtils&f&l]"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix + " &aPlaytimeUtils has been reloaded!"));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.4f);
            return true;
        }

        return false;
    }
}
