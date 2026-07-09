//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils;

import me.clip.placeholderapi.PlaceholderAPI;
import me.itsandrew.playtimeUtils.RewardSystem.States.AddRemoveChoice;
import me.itsandrew.playtimeUtils.RewardSystem.States.StaffState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

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

                if(args.length < 1){
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: /myplaytime <tournament | main>"));
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    return true;
                }

                switch(args[0]){
                    case "main" -> {
                        //Checking if the player has permission to use the command
                        if(!player.hasPermission("playtimeutils.myplaytime.main")) noPermission(player);

                        String message = plugin.getConfig().getString("messages.my-playtime.main", "&aYour playtime is &e&l%playtime_mainValue%&a!");
                        message = PlaceholderAPI.setPlaceholders(player, message);
                        Component playtimeMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
                        player.sendMessage(playtimeMessage);
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    }

                    case "tournament" -> {
                        //Checking if the player has permission to use the command
                        if (!player.hasPermission("playtimeutils.myplaytime.tournament")) noPermission(player);

                        //Checking if the tournament is enabled
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                            long tournamentDuration = plugin.getDatabaseManager().getTournamentTimestamp("duration");
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                if (tournamentDuration == 0) {
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>The Playtime Tournament is not active yet! Be on the lookout for <yellow>the next one<red>!"));
                                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                } else {
                                    String message = plugin.getConfig().getString("messages.my-playtime.tournament", "&aYour tournament playtime is &e&l%playtime_tournamentValue%&a!");
                                    message = PlaceholderAPI.setPlaceholders(player, message);
                                    Component playtimeMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
                                    player.sendMessage(playtimeMessage);
                                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                                }
                            });
                        });
                    }

                    case "rewards" -> {
                        //Checking if the player has permission
                        if(!player.hasPermission("playtimeutils.myplaytime.rewards")) noPermission(player);

                        plugin.getPlaytimeRewardsGUI().openGUI(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    }

                    default -> {
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Unknown command."));
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    }
                }
                return true;
            }

            case "playtime" -> {
                //Checking if the sender has permission
                if(!player.hasPermission("playtimeutils.playtime")) noPermission(player);

                if(args.length < 2){
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: /playtime <main | tournament> <player>"));
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    return true;
                }

                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[1]);
                if(!targetPlayer.hasPlayedBefore()){
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Player <yellow><b>"+targetPlayer.getName()+"<red> has never played before!"));
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    return true;
                }

                switch(args[0]){
                    case "main" -> {
                        //Checking if the player has permission to run the command
                        if(!player.hasPermission("playtimeutils.playtime.main")) noPermission(player);

                        String message = plugin.getConfig().getString("messages.player-playtime.main", "&e%player%'s playtime is &e&l%playtime_mainValue%&a!");
                        message = PlaceholderAPI.setPlaceholders(targetPlayer, message);
                        message = message.replace("%player%", targetPlayer.getName());
                        Component playtimeMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
                        player.sendMessage(playtimeMessage);
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    }

                    case "tournament" -> {
                        //Checking if the player has permission
                        if(!player.hasPermission("playtimeutils.playtime.tournament")) noPermission(player);

                        AtomicLong duration = new AtomicLong();
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->{
                            duration.set(plugin.getDatabaseManager().getTournamentTimestamp("duration"));

                            Bukkit.getScheduler().runTask(plugin, () -> {
                                if(duration.get() == 0){
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>The Playtime Tournament is not active yet!"));
                                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                    return;
                                }

                                String message = plugin.getConfig().getString("messages.player-playtime.tournament", "&e%player%'s tournament playtime is &e&l%playtime_tournamentValue%&a!");
                                message = PlaceholderAPI.setPlaceholders(targetPlayer, message);
                                message = message.replace("%player%", targetPlayer.getName());
                                Component playtimeMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
                                player.sendMessage(playtimeMessage);
                            });
                        });
                    }

                    default -> {
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Unknown command."));
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    }
                }
                return true;
            }

            case "topplaytime" -> {
                //Checking if the player has permission
                if(!player.hasPermission("playtimeutils.topplaytime")) noPermission(player);

                if(args.length < 1){
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: /topplaytime <main | tournament>"));
                    return true;
                }

                switch(args[0]){
                    case "main" -> {
                        //Checking if the player has permission
                        if(!player.hasPermission("playtimeutils.topplaytime.main")) noPermission(player);

                        List<String> rawMessage = plugin.getConfig().getStringList("messages.top-3-main-players");
                        for(String line : rawMessage){
                            line = PlaceholderAPI.setPlaceholders(player, line);
                            Component coloredLine = LegacyComponentSerializer.legacyAmpersand().deserialize(line);
                            player.sendMessage(coloredLine);
                        }
                    }

                    case "tournament" -> {
                        //Checking if the player has permission
                        if(!player.hasPermission("playtimeutils.topplaytime.tournament")) noPermission(player);

                        AtomicLong duration = new AtomicLong();
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->{
                            //Checking if the tournament is enabled
                            duration.set(plugin.getDatabaseManager().getTournamentTimestamp("duration"));
                            if(duration.get() == 0){
                                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>The Playtime Tournament is not active yet! Be on the lookout for <yellow>the next one<red>!"));
                                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                return;
                            }

                            List<String> rawMessage = plugin.getConfig().getStringList("messages.top-3-tournament-players");
                            for(String line : rawMessage){
                                line = PlaceholderAPI.setPlaceholders(player, line);
                                Component coloredLine = LegacyComponentSerializer.legacyAmpersand().deserialize(line);
                                player.sendMessage(coloredLine);
                            }
                        });
                    }
                }
                return true;
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

                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>The configuration has been reloaded!"));
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

                                    case "setbook" -> {
                                        if(!player.hasPermission("playtimeutils.ptutils.rewards.tournament.setbook")) noPermission(player);

                                        //Getting the player's item in hand
                                        ItemStack itemInHand = player.getInventory().getItemInMainHand();
                                        if(itemInHand.getType() != Material.WRITTEN_BOOK){
                                            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You must hold a <b>written book</b> in order to set the tournament book!"));
                                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                            return true;
                                        }

                                        BookMeta bookMeta = (BookMeta) itemInHand.getItemMeta();
                                        String author = bookMeta.getAuthor();
                                        String title = bookMeta.getTitle();
                                        List<String> pages = bookMeta.getPages();

                                        plugin.getConfig().set("reward-system.pending-reward-book.author", author);
                                        plugin.getConfig().set("reward-system.pending-reward-book.title", title);
                                        plugin.getConfig().set("reward-system.pending-reward-book.pages", pages);
                                        plugin.saveConfig();

                                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>The tournament book has been set!"));
                                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.4f);
                                    }

                                    case "enable" -> {
                                        //Checking if the player has permission.
                                        if(!player.hasPermission("playtimeutils.ptutils.rewards.tournament.enable")) noPermission(player);

                                        //Checking if the Reward System is enabled
                                        boolean toggleRewardSystem = plugin.getConfig().getBoolean("reward-system.toggle", false);
                                        if(!toggleRewardSystem){
                                            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>The <b>Reward System</b> is disabled! Enable it in <yellow><b>config.yml <red>to use this command."));
                                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                            return true;
                                        }

                                        AtomicLong duration = new AtomicLong();
                                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->{
                                            duration.set(plugin.getDatabaseManager().getTournamentTimestamp("duration"));

                                            //Checking if the tournament is already enabled
                                            Bukkit.getScheduler().runTask(plugin, () -> {
                                                if(duration.get() != 0){
                                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>The tournament is already enabled!"));
                                                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                                    return;
                                                }

                                                //Checking if the rewards are set up
                                                List<?> firstPlaceRewards = plugin.getConfig().getList("reward-system.rewards.first-place.items");
                                                List<?> secondPlaceRewards = plugin.getConfig().getList("reward-system.rewards.second-place.items");
                                                List<?> thirdPlaceRewards = plugin.getConfig().getList("reward-system.rewards.third-place.items");
                                                if(firstPlaceRewards == null || secondPlaceRewards == null || thirdPlaceRewards == null){
                                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>The rewards (items) are not set up properly! Set them up using <yellow>/ptutils rewards add<red>!"));
                                                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                                    return;
                                                }

                                                //Checking if the Pending Reward Book is set up properly
                                                String title = plugin.getConfig().getString("reward-system.pending-reward-book.title");
                                                String author = plugin.getConfig().getString("reward-system.pending-reward-book.author");
                                                List<String> pages = plugin.getConfig().getStringList("reward-system.pending-reward-book.pages");
                                                if(title == null || author == null || pages.isEmpty()){
                                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>The pending reward book is not set up properly! Set it up using <yellow>/ptutils rewards tournament setbook<red>!"));
                                                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                                    return;
                                                }

                                                //Saving the value when the tournament will end in the config
                                                int numberOfDays = plugin.getConfig().getInt("reward-system.tournament-timer", 7);
                                                long millis = numberOfDays * 60 * 1000L;
                                                Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->{
                                                    plugin.getDatabaseManager().setTournamentTimestamps(System.currentTimeMillis(), millis, System.currentTimeMillis() + millis);

                                                    Bukkit.getScheduler().runTask(plugin, () -> {
                                                        plugin.getPlaceholdersManager().setCachedTournamentDuration(plugin.formatTime(millis));
                                                        plugin.getPlaceholdersManager().setCachedTournamentEndDate(plugin.formatDate(System.currentTimeMillis() + millis));

                                                        //Broadcasting the tournament start message to everyone
                                                        for(Player onlinePlayer : Bukkit.getOnlinePlayers()){
                                                            String startSoundName = plugin.getConfig().getString("reward-system.tournament-sounds.start.name", "BLOCK_NOTE_BLOCK_PLING");
                                                            float startSoundVolume = plugin.getConfig().getInt("reward-system.tournament-sounds.start.volume", 1);
                                                            float startSoundPitch = plugin.getConfig().getInt("reward-system.tournament-sounds.start.pitch", 1);
                                                            Sound startSound = Registry.SOUNDS.get(NamespacedKey.minecraft(startSoundName.toLowerCase()));
                                                            onlinePlayer.playSound(onlinePlayer.getLocation(), startSound, startSoundVolume, startSoundPitch);

                                                            List<String> messageLines = plugin.getConfig().getStringList("reward-system.tournament-messages.start");
                                                            for(String line : messageLines){
                                                                line = PlaceholderAPI.setPlaceholders(onlinePlayer, line);
                                                                Component coloredLine = LegacyComponentSerializer.legacyAmpersand().deserialize(line);

                                                                //Adding a component designed to have a hover and click event (to open the discord link)
                                                                coloredLine = replaceDiscordComponent(coloredLine);

                                                                onlinePlayer.sendMessage(coloredLine);
                                                            }

                                                            //Also putting the players in the tournament playtime map
                                                            UUID playerUUID = onlinePlayer.getUniqueId();
                                                            plugin.getTournamentPlaytimeMap().put(playerUUID, 0);
                                                        }

                                                        plugin.getGivingRewardsSystem().startTasks();
                                                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>The tournament has been enabled!"));
                                                        plugin.getLogger().info("[PlaytimeUtils] The playtime tournament has been enabled!");
                                                    });
                                                });
                                            });
                                        });
                                    }

                                    case "disable" -> {
                                        //Checking if the player has permission.
                                        if(!player.hasPermission("playtimeutils.ptutils.rewards.tournament.disable")) noPermission(player);

                                        //Checking reward system is enabled
                                        boolean toggleRewardSystem = plugin.getConfig().getBoolean("reward-system.toggle", false);
                                        if(!toggleRewardSystem){
                                            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>The <b>Reward System</b> is disabled! Enable it in <yellow><b>config.yml <red>to use this command."));
                                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                            return true;
                                        }

                                        //Checking if the tournament is already disabled
                                        AtomicLong duration = new AtomicLong();
                                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->{
                                            duration.set(plugin.getDatabaseManager().getTournamentTimestamp("duration"));

                                            Bukkit.getScheduler().runTask(plugin, () -> {
                                                if(duration.get() == 0){
                                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>The tournament is already disabled!"));
                                                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                                    return;
                                                }

                                                //Wiping the 'Reward Playtime' column in the db.
                                                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getDatabaseManager().wipeTournamentPlaytime());
                                                plugin.getLogger().warning("[PlaytimeUtils] Reward System disabled. Reward playtime has been wiped.");

                                                //Broadcasting the Tournament Disabled Message
                                                for(Player onlinePlayer : Bukkit.getOnlinePlayers()){
                                                    onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);

                                                    List<String> messageLines = plugin.getConfig().getStringList("reward-system.tournament-messages.disabled");
                                                    for(String line : messageLines){
                                                        line = PlaceholderAPI.setPlaceholders(onlinePlayer, line);
                                                        Component coloredLine = LegacyComponentSerializer.legacyAmpersand().deserialize(line);

                                                        //Adding a component designed to have a hover and click event (to open the discord link)
                                                        coloredLine = replaceDiscordComponent(coloredLine);

                                                        onlinePlayer.sendMessage(coloredLine);
                                                    }
                                                }

                                                //Deleting the tournament timestamps from the database.
                                                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getDatabaseManager().deleteTournamentTimestamps());

                                                //Stopping the 2 tasks
                                                plugin.getGivingRewardsSystem().getRewardingTask().cancel();
                                                plugin.getGivingRewardsSystem().getBroadcastTask().cancel();

                                                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>The tournament has been disabled!"));
                                                plugin.getLogger().info("The tournament has been disabled!");
                                            });
                                        });
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

    private boolean isUrlValid(String url){
        try {
            URI uri = new URI(url);
            return uri.getScheme() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private Component replaceDiscordComponent(Component component){
        String discordLink = plugin.getConfig().getString("reward-system.tournament-messages.discord-link");
        String hoverText = plugin.getConfig().getString("reward-system.tournament-messages.discord-hover-text", "Click here to join our Discord Server");
        if(discordLink != null && isUrlValid(discordLink)){
            Component discordWord = Component.text("Discord")
                    .hoverEvent(HoverEvent.showText(Component.text(hoverText)))
                    .clickEvent(ClickEvent.openUrl(discordLink));
            component = component.replaceText(TextReplacementConfig.builder().match("discord").replacement(discordWord).build());
        }
        return component;
    }

    private void noPermission(Player player){
        String noPermissionMessage = LegacyComponentSerializer.legacyAmpersand().serialize(Component.text(plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to do that!")));
        noPermissionMessage = PlaceholderAPI.setPlaceholders(player, noPermissionMessage);

        player.sendMessage(noPermissionMessage);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
    }
}
