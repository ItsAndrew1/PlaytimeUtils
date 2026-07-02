//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils;

import me.clip.placeholderapi.PlaceholderAPI;
import me.itsandrew.playtimeUtils.RewardSystem.States.AddRemoveChoice;
import me.itsandrew.playtimeUtils.RewardSystem.States.StaffState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

        switch(command.getName()){
            case "myplaytime" -> {
                if(!player.hasPermission("playtimeutils.myplaytime")) noPermission(player);

                String playtimeMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.my-playtime", "&aYour playtime is &e&l%playtime_value%&a!"));
                playtimeMessage = PlaceholderAPI.setPlaceholders(player, playtimeMessage);
                player.sendMessage(playtimeMessage);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                return true;
            }

            case "playtime" -> {
                //Checking if the sender has permission
                if(!player.hasPermission("playtimeutils.playtime")) noPermission(player);

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

            case "topplaytime" -> {
                //Checking if the player has permission
                if(!player.hasPermission("playtimeutils.topplaytime")) noPermission(player);

                List<String> rawMessage = plugin.getConfig().getStringList("messages.top-3-players");
                for(String line : rawMessage){
                    line = ChatColor.translateAlternateColorCodes('&', line);
                    line = PlaceholderAPI.setPlaceholders(player, line);
                    player.sendMessage(line);
                }
            }

            case "ptutils" -> {
                if(args.length < 1){
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: <b>/ptutils <reload | rewards></b>"));
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    return true;
                }

                switch(args[0]){
                    case "reload" -> {
                        //Checking if the player has permission.
                        if(!player.hasPermission("playtimeutils.ptutils.reload")) noPermission(player);

                        plugin.reloadConfig();

                        //Checking if the reward system toggle is false;
                        boolean toggleRewardSystem = plugin.getConfig().getBoolean("reward-system.toggle", true);
                        if(!toggleRewardSystem){
                        }

                        String chatPrefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("chat-prefix", "&f&l[&e&lPUtils&f&l]"));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatPrefix + " &aPlaytimeUtils has been reloaded!"));
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.4f);
                        return true;
                    }

                    case "rewards" -> {
                        //Checking if the player has permission
                        if(!player.hasPermission("playtimeutils.ptutils.rewards")) noPermission(player);

                        if(args.length < 2){
                            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: <b>/ptutils rewards <tournament | add | remove></b>"));
                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                            return true;
                        }

                        switch(args[1]){
                            case "tournament" -> {
                                //Checking if the player has permission.
                                if(!player.hasPermission("playtimeutils.ptutils.rewards.tournament")) noPermission(player);

                                if(args.length < 3){
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: /ptutils rewards tournament <b><settimer | enable | disable></b>"));
                                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                    return true;
                                }

                                switch(args[2]){
                                    case "settimer" -> {
                                        //Checking if the player has permission.
                                        if(!player.hasPermission("playtimeutils.ptutils.rewards.tournament.settimer")) noPermission(player);

                                        if(args.length < 4){
                                            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: /ptutils rewards tournament settimer <b><days></b>"));
                                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                            return true;
                                        }

                                        String rawInput = args[3];
                                        int input;
                                        try{
                                            input = Integer.parseInt(rawInput);
                                        } catch (Exception e){
                                            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Invalid input! Please enter a number of <b>days</b>."));
                                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                            return true;
                                        }

                                        plugin.getConfig().set("reward-system.tournament-timer", input);
                                        plugin.saveConfig();
                                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>The tournament timer has been set to <b>" + input + "</b> days!"));
                                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                                    }

                                    case "enable" -> {
                                        //Checking if the player has permission.
                                        if(!player.hasPermission("playtimeutils.ptutils.rewards.tournament.enable")) noPermission(player);

                                        //Checking if the tournament isn't already enabled.
                                        boolean toggleRewardSystem = plugin.getConfig().getBoolean("reward-system.toggle", true);
                                        if(toggleRewardSystem){
                                            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>The tournament is already enabled!"));
                                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                            return true;
                                        }

                                        plugin.getConfig().set("reward-system.toggle", true);
                                        plugin.saveConfig();
                                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>The tournament has been enabled!"));
                                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                                        plugin.getLogger().info("[PlaytimeUtils] The tournament has been enabled!");
                                    }

                                    case "disable" -> {
                                        //Checking if the player has permission.
                                        if(!player.hasPermission("playtimeutils.ptutils.rewards.tournament.disable")) noPermission(player);

                                        //Checking if the tournament isn't already disabled.
                                        boolean toggleRewardSystem = plugin.getConfig().getBoolean("reward-system.toggle", true);
                                        if(!toggleRewardSystem){
                                            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>The tournament is already disabled!"));
                                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                            return true;
                                        }

                                        //Wiping the 'Reward Playtime' column in the db.
                                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                            plugin.getDatabaseManager().wipeRewardPlaytime();
                                        });
                                        plugin.getLogger().warning("[PlaytimeUtils] Reward System disabled. Reward playtime has been wiped.");

                                        plugin.getConfig().set("reward-system.toggle", false);
                                        plugin.saveConfig();
                                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>The tournament has been disabled!"));
                                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                                        plugin.getLogger().info("[PlaytimeUtils] The tournament has been disabled!");
                                    }
                                }
                            }

                            case "add" -> {
                                //Checking if the player has permission.
                                if(!player.hasPermission("playtimeutils.ptutils.rewards.add")) noPermission(player);

                                //Creating a new StaffState for the specific staff
                                StaffState newState = new StaffState(AddRemoveChoice.ADD, null, null, null);
                                plugin.getStaffStates().put(player, newState);

                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                                plugin.getItemsOrExpGUI().openGUI(player);
                                return true;
                            }

                            case "remove" -> {
                                //Checking if the player has permission.
                                if(!player.hasPermission("playtimeutils.ptutils.rewards.remove")) noPermission(player);

                                //Creating a new StaffState for the specific staff
                                StaffState newState = new StaffState(AddRemoveChoice.REMOVE, null, null, null);
                                plugin.getStaffStates().put(player, newState);

                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                                plugin.getItemsOrExpGUI().openGUI(player);
                                return true;
                            }

                            default -> {
                                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Unknown command."));
                                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                return true;
                            }
                        }
                    }
                }
            }

            default -> {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Unknown command."));
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                return true;
            }
        }

        return false;
    }

    private void noPermission(Player player){;
        String noPermissionMessage = LegacyComponentSerializer.legacyAmpersand().serialize(Component.text(plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to do that!")));
        noPermissionMessage = PlaceholderAPI.setPlaceholders(player, noPermissionMessage);

        player.sendMessage(noPermissionMessage);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
    }
}
