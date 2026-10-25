//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils;

import me.clip.placeholderapi.PlaceholderAPI;
import me.itsandrew.playtimeUtils.RewardSystem.States.AddRemoveChoice;
import me.itsandrew.playtimeUtils.RewardSystem.States.StaffState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandManager implements CommandExecutor, Listener {
    private final PlaytimeUtils plugin;

    //Sets for caching the staff's uuid when doing the operations
    private Set<UUID> resetPlaytimesUuidSet;
    private Set<UUID> setPlaytimesUuidSet;

    public CommandManager(PlaytimeUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        Player player = (Player) sender;

        switch(command.getName()){
            case "myplaytime" -> {
                if(!player.hasPermission("playtimeutils.myplaytime")) {
                    noPermission(player);
                    return true;
                }

                if(args.length < 1){
                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cUsage: &l/myplaytime <main | tournament | rewards>"));
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    return true;
                }

                switch(args[0]){
                    case "main" -> {
                        //Checking if the player has permission to use the command
                        if(!player.hasPermission("playtimeutils.myplaytime.main")){
                            noPermission(player);
                            return true;
                        }

                        String message = plugin.getConfig().getString("messages.my-playtime.main", "&aYour playtime is: &e&l%playtime_mainValue%");
                        message = plugin.setPP(message, player);
                        Component playtimeMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
                        player.sendMessage(playtimeMessage);
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    }

                    case "tournament" -> {
                        //Checking if the player has permission to use the command
                        if (!player.hasPermission("playtimeutils.myplaytime.tournament")) {
                            noPermission(player);
                            return true;
                        }

                        //Checking if the tournament is enabled
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                            long tournamentDuration = plugin.getDatabaseManager().getTournamentTimestamp("duration");
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                if (tournamentDuration == 0) {
                                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cThe Playtime Tournament is not active yet! Be on the lookout for &ethe next one&c!"));
                                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                } else {
                                    String message = plugin.getConfig().getString("messages.my-playtime.tournament", "&aYour tournament playtime is: &e&l%playtime_tournamentValue%");
                                    message = plugin.setPP(message, player);
                                    Component playtimeMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
                                    player.sendMessage(playtimeMessage);
                                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                                }
                            });
                        });
                    }

                    case "rewards" -> {
                        //Checking if the player has permission
                        if(!player.hasPermission("playtimeutils.myplaytime.rewards")){
                            noPermission(player);
                            return true;
                        }

                        plugin.getPlaytimeRewardsGUI().openGUI(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    }

                    default -> {
                        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cUnknown command."));
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    }
                }
                return true;
            }

            case "playtime" -> {
                //Checking if the sender has permission
                if(!player.hasPermission("playtimeutils.playtime")) {
                    noPermission(player);
                    return true;
                }

                if(args.length < 2){
                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cUsage: &l/playtime <main | tournament> <player>"));
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    return true;
                }

                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[1]);
                if(!targetPlayer.hasPlayedBefore()){
                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cPlayer &e&l" + targetPlayer.getName() + " &chas never played before!"));
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    return true;
                }

                switch(args[0]){
                    case "main" -> {
                        //Checking if the player has permission to run the command
                        if(!player.hasPermission("playtimeutils.playtime.main")){
                            noPermission(player);
                            return true;
                        }

                        String message = plugin.getConfig().getString("messages.player-playtime.main", "&e%player%'s playtime is: &e&l%playtime_mainValue%");
                        message = plugin.setPP(message, targetPlayer);
                        message = message.replace("%player%", targetPlayer.getName());
                        Component playtimeMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
                        player.sendMessage(playtimeMessage);
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    }

                    case "tournament" -> {
                        //Checking if the player has permission
                        if(!player.hasPermission("playtimeutils.playtime.tournament")){
                            noPermission(player);
                            return true;
                        }

                        AtomicLong duration = new AtomicLong();
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->{
                            duration.set(plugin.getDatabaseManager().getTournamentTimestamp("duration"));

                            Bukkit.getScheduler().runTask(plugin, () -> {
                                if(duration.get() == 0){
                                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cThe Playtime Tournament is not active yet!"));
                                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                    return;
                                }

                                String message = plugin.getConfig().getString("messages.player-playtime.tournament", "&e%player%'s tournament playtime is: &e&l%playtime_tournamentValue%");
                                message = plugin.setPP(message, targetPlayer);
                                message = message.replace("%player%", targetPlayer.getName());
                                Component playtimeMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
                                player.sendMessage(playtimeMessage);
                            });
                        });
                    }

                    default -> {
                        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cUnknown command."));
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    }
                }
                return true;
            }

            case "topplaytime" -> {
                //Checking if the player has permission
                if(!player.hasPermission("playtimeutils.topplaytime")) {
                    noPermission(player);
                    return true;
                }

                if(args.length < 1){
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cUsage: &l/topplaytime <main | tournament>"));
                    return true;
                }

                switch(args[0]){
                    case "main" -> {
                        //Checking if the player has permission
                        if(!player.hasPermission("playtimeutils.topplaytime.main")){
                            noPermission(player);
                            return true;
                        }

                        List<String> rawMessage = plugin.getConfig().getStringList("messages.top-3-main-players");
                        for(String line : rawMessage){
                            line = plugin.setPP(line, player);
                            Component coloredLine = LegacyComponentSerializer.legacyAmpersand().deserialize(line);
                            player.sendMessage(coloredLine);
                        }
                    }

                    case "tournament" -> {
                        //Checking if the player has permission
                        if(!player.hasPermission("playtimeutils.topplaytime.tournament")){
                            noPermission(player);
                            return true;
                        }

                        AtomicLong duration = new AtomicLong();
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->{
                            //Checking if the tournament is enabled
                            duration.set(plugin.getDatabaseManager().getTournamentTimestamp("duration"));
                            if(duration.get() == 0){
                                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cThe Playtime Tournament is not active yet! Be on the lookout for &ethe next one&c!"));
                                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                return;
                            }

                            List<String> rawMessage = plugin.getConfig().getStringList("messages.top-3-tournament-players");
                            for(String line : rawMessage){
                                line = plugin.setPP(line, player);
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
                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cUsage: &l/ptutils <reload | rewards>"));
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    return true;
                }

                switch(args[0]){
                    case "reload" -> {
                        //Checking if the player has permission.
                        if(!player.hasPermission("playtimeutils.ptutils.reload")){
                            noPermission(player);
                            return true;
                        }

                        plugin.reloadConfig();

                        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&aThe configuration has been reloaded!"));
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.4f);
                        return true;
                    }

                    case "playtimetochat" -> {
                        //Checking for permission
                        if(!player.hasPermission("playtimeutils.ptutils.playtimetochat")){
                            noPermission(player);
                            return true;
                        }

                        if(args.length < 2){
                            player.sendMessage(LegacyComponentSerializer.legacySection().deserialize("&cUsage: &l/ptutils playtimetochat <enable | disable | set>"));
                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                            return true;
                        }

                        switch(args[1]){
                            case "enable" -> {
                                //Check permission
                                if(!player.hasPermission("playtimeutils.ptutils.playtimetochat.enable")){
                                    noPermission(player);
                                    return true;
                                }

                                boolean togglePlaytimeToChat = plugin.getConfig().getBoolean("toggle-needed-playtime-to-chat", false);
                                if(togglePlaytimeToChat){
                                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cFeature already enabled!"));
                                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                    return true;
                                }

                                plugin.getConfig().set("toggle-needed-playtime-to-chat", true);
                                plugin.saveConfig();

                                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&aToggled ON feature."));
                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.4f);
                                return true;
                            }

                            case "disable" -> {
                                //Check permission
                                if(!player.hasPermission("playtimeutils.ptutils.playtimetochat.disable")){
                                    noPermission(player);
                                    return true;
                                }

                                boolean togglePlaytimeToChat = plugin.getConfig().getBoolean("toggle-needed-playtime-to-chat", false);
                                if(!togglePlaytimeToChat){
                                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cFeature already disabled!"));
                                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                    return true;
                                }

                                plugin.getConfig().set("toggle-needed-playtime-to-chat", false);
                                plugin.saveConfig();

                                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&aToggled OFF feature."));
                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.4f);
                                return true;
                            }

                            case "set" -> {
                                //Check permission
                                if(!player.hasPermission("playtimeutils.ptutils.playtimetochat.set")){
                                    noPermission(player);
                                    return true;
                                }

                                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&aEnter a needed playtime (in seconds!): "));
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                                plugin.playerInput(player, message -> {
                                    String rawMessage = LegacyComponentSerializer.legacyAmpersand().serialize(message);
                                    try{
                                        int seconds = Integer.parseInt(rawMessage);
                                        if(seconds < 0){
                                            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cInvalid value."));
                                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                        }

                                        plugin.getConfig().set("playtime-needed-to-chat", seconds);
                                        plugin.saveConfig();

                                        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&aSaved playtime successfully!"));
                                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.4f);
                                    } catch (Exception e) {
                                        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cInvalid value."));
                                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                    }
                                });
                            }
                        }

                        return true;
                    }

                    case "rewards" -> {
                        //Checking if the player has permission
                        if(!player.hasPermission("playtimeutils.ptutils.rewards")) {
                            noPermission(player);
                            return true;
                        }

                        if(args.length < 2){
                            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cUsage: &l/ptutils rewards <tournament | add | remove>"));
                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                            return true;
                        }

                        switch(args[1]){
                            case "tournament" -> {
                                //Checking if the player has permission.
                                if(!player.hasPermission("playtimeutils.ptutils.rewards.tournament")){
                                    noPermission(player);
                                    return true;
                                }

                                if(args.length < 3){
                                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cUsage: &l/ptutils rewards tournament <settimer | setbook | enable | disable>"));
                                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                    return true;
                                }

                                switch(args[2]){
                                    case "settimer" -> {
                                        //Checking if the player has permission.
                                        if(!player.hasPermission("playtimeutils.ptutils.rewards.tournament.settimer")){
                                            noPermission(player);
                                            return true;
                                        }

                                        if(args.length < 4){
                                            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cUsage: &l/ptutils rewards tournament settimer <days>"));
                                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                            return true;
                                        }

                                        String rawInput = args[3];
                                        int input;
                                        try{
                                            input = Integer.parseInt(rawInput);
                                        } catch (Exception e){
                                            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cInvalid input! Please enter a number of &ldays."));
                                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                            return true;
                                        }

                                        plugin.getConfig().set("reward-system.tournament-timer", input);
                                        plugin.saveConfig();
                                        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&aThe tournament timer has been set to &l" + input + " &adays!"));
                                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                                    }

                                    case "setbook" -> {
                                        if(!player.hasPermission("playtimeutils.ptutils.rewards.tournament.setbook")){
                                            noPermission(player);
                                            return true;
                                        }

                                        //Getting the player's item in hand
                                        ItemStack itemInHand = player.getInventory().getItemInMainHand();
                                        if(itemInHand.getType() != Material.WRITTEN_BOOK){
                                            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cYou must hold a &lwritten book&c in order to set the tournament book!"));
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

                                        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&aThe tournament book has been set!"));
                                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.4f);
                                    }

                                    case "enable" -> {
                                        //Checking if the player has permission.
                                        if(!player.hasPermission("playtimeutils.ptutils.rewards.tournament.enable")){
                                            noPermission(player);
                                            return true;
                                        }

                                        //Checking if the Reward System is enabled
                                        boolean toggleRewardSystem = plugin.getConfig().getBoolean("reward-system.toggle", false);
                                        if(!toggleRewardSystem){
                                            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cThe &lReward System&c is disabled! Enable it in &e&lconfig.yml &cto use this command."));
                                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                            return true;
                                        }

                                        AtomicLong duration = new AtomicLong();
                                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->{
                                            duration.set(plugin.getDatabaseManager().getTournamentTimestamp("duration"));

                                            //Checking if the tournament is already enabled
                                            Bukkit.getScheduler().runTask(plugin, () -> {
                                                if(duration.get() != 0){
                                                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cThe tournament is already enabled!"));
                                                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                                    return;
                                                }

                                                //Checking if the rewards are set up
                                                List<?> firstPlaceRewards = plugin.getConfig().getList("reward-system.rewards.first-place.items");
                                                List<?> secondPlaceRewards = plugin.getConfig().getList("reward-system.rewards.second-place.items");
                                                List<?> thirdPlaceRewards = plugin.getConfig().getList("reward-system.rewards.third-place.items");
                                                if(firstPlaceRewards == null || secondPlaceRewards == null || thirdPlaceRewards == null){
                                                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cThe rewards (items) are not set up properly! Set them up using &e/ptutils rewards add&c!"));
                                                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                                    return;
                                                }

                                                //Checking if the Pending Reward Book is set up properly
                                                String title = plugin.getConfig().getString("reward-system.pending-reward-book.title");
                                                String author = plugin.getConfig().getString("reward-system.pending-reward-book.author");
                                                List<String> pages = plugin.getConfig().getStringList("reward-system.pending-reward-book.pages");
                                                if(title == null || author == null || pages.isEmpty()){
                                                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cThe pending reward book is not set up properly! Set it up using &e/ptutils rewards tournament setbook&c!"));
                                                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                                    return;
                                                }

                                                //Saving the value when the tournament will end in the config
                                                int numberOfDays = plugin.getConfig().getInt("reward-system.tournament-timer", 7);
                                                long millis = numberOfDays * 24 * 60 * 60 * 1000L;
                                                Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->{
                                                    plugin.getDatabaseManager().setTournamentTimestamps(System.currentTimeMillis(), millis, System.currentTimeMillis() + millis);

                                                    Bukkit.getScheduler().runTask(plugin, () -> {
                                                        plugin.getPlaceholdersManager().setCachedTournamentDuration(plugin.formatTime(millis));
                                                        plugin.getPlaceholdersManager().setCachedTournamentEndDate(plugin.formatDate(System.currentTimeMillis() + millis));

                                                        //Broadcasting the tournament start message to everyone
                                                        for(Player onlinePlayer : Bukkit.getOnlinePlayers()){
                                                            String startSoundName = plugin.getConfig().getString("reward-system.tournament-sounds.start.name", "block.note_block.pling");
                                                            double startSoundVolume = plugin.getConfig().getDouble("reward-system.tournament-sounds.start.volume", 1.0);
                                                            double startSoundPitch = plugin.getConfig().getDouble("reward-system.tournament-sounds.start.pitch", 1.0);
                                                            Sound startSound = Registry.SOUNDS.get(NamespacedKey.minecraft(startSoundName.toLowerCase()));
                                                            onlinePlayer.playSound(onlinePlayer.getLocation(), startSound, (float) startSoundVolume, (float) startSoundPitch);

                                                            List<String> messageLines = plugin.getConfig().getStringList("reward-system.tournament-messages.start");
                                                            for(String line : messageLines){
                                                                line = plugin.setPP(line, player);
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
                                                        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&aThe tournament has been enabled!"));
                                                        plugin.getLogger().info("[PlaytimeUtils] The playtime tournament has been enabled!");
                                                    });
                                                });
                                            });
                                        });
                                    }

                                    case "disable" -> {
                                        //Checking if the player has permission.
                                        if(!player.hasPermission("playtimeutils.ptutils.rewards.tournament.disable")){
                                            noPermission(player);
                                            return true;
                                        }

                                        //Checking reward system is enabled
                                        boolean toggleRewardSystem = plugin.getConfig().getBoolean("reward-system.toggle", false);
                                        if(!toggleRewardSystem){
                                            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cThe &lReward System&c is disabled! Enable it in &e&lconfig.yml &cto use this command."));
                                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                            return true;
                                        }

                                        //Checking if the tournament is already disabled
                                        AtomicLong duration = new AtomicLong();
                                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->{
                                            duration.set(plugin.getDatabaseManager().getTournamentTimestamp("duration"));

                                            Bukkit.getScheduler().runTask(plugin, () -> {
                                                if(duration.get() == 0){
                                                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cThe tournament is already disabled!"));
                                                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                                    return;
                                                }

                                                //Wiping the 'Tournament Playtime' column in the db.
                                                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getDatabaseManager().wipeTournamentPlaytime());
                                                plugin.getLogger().warning("[PlaytimeUtils] Reward System disabled. Reward playtime has been wiped.");

                                                //Broadcasting the Tournament Disabled Message
                                                for(Player onlinePlayer : Bukkit.getOnlinePlayers()){
                                                    onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);

                                                    List<String> messageLines = plugin.getConfig().getStringList("reward-system.tournament-messages.disabled");
                                                    for(String line : messageLines){
                                                        line = plugin.setPP(line, onlinePlayer);
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

                                                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&aThe tournament has been disabled!"));
                                                plugin.getLogger().info("The tournament has been disabled!");
                                            });
                                        });
                                    }
                                }
                            }

                            case "add" -> {
                                //Checking if the player has permission.
                                if(!player.hasPermission("playtimeutils.ptutils.rewards.add")){
                                    noPermission(player);
                                    return true;
                                }

                                //Creating a new StaffState for the specific staff
                                StaffState newState = new StaffState(AddRemoveChoice.ADD, null, null, null);
                                plugin.getStaffStates().put(player.getUniqueId(), newState);

                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                                plugin.getItemsOrExpGUI().openGUI(player);
                                return true;
                            }

                            case "remove" -> {
                                //Checking if the player has permission.
                                if(!player.hasPermission("playtimeutils.ptutils.rewards.remove")) {
                                    noPermission(player);
                                    return true;
                                }

                                //Creating a new StaffState for the specific staff
                                StaffState newState = new StaffState(AddRemoveChoice.REMOVE, null, null, null);
                                plugin.getStaffStates().put(player.getUniqueId(), newState);

                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                                plugin.getItemsOrExpGUI().openGUI(player);
                                return true;
                            }

                            default -> {
                                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cUnknown command."));
                                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                                return true;
                            }
                        }
                    }

                    case "resetplaytimes" -> {
                        //Checking if the player has permission to do this.
                        if(!player.hasPermission("playtimeutils.ptutils.resetplaytimes")){
                            noPermission(player);
                            return true;
                        }

                        if(resetPlaytimesUuidSet.contains(player.getUniqueId())){
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                //Wiping out the main playtime from the DB
                                String SQL = "UPDATE playersPlaytime SET mainPlaytime = 0";
                                try(Connection connection = plugin.getDatabaseManager().getDataSource().getConnection(); PreparedStatement ps = connection.prepareStatement(SQL)){
                                    ps.executeUpdate();
                                } catch (Exception e) {
                                    plugin.getLogger().warning("Wiping main playtime failed! See message: "+e.getMessage());
                                    return;
                                }
                                //Wiping from the main playtime map
                                plugin.getMainPlaytimeMap().clear();
                                for(Player p : Bukkit.getOnlinePlayers()) plugin.getMainPlaytimeMap().put(p.getUniqueId(), 0);
                                resetPlaytimesUuidSet.remove(player.getUniqueId());

                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    Component warningMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(plugin.getConfig().getString("resetplaytimes-command.done-chat-message", "&a&lSuccessfully wiped out the &lmain playtimes&a."));
                                    player.sendMessage(warningMessage);

                                    String soundName = plugin.getConfig().getString("resetplaytimes-command.done-sound.name", "entity.player.levelup");
                                    float soundVol = plugin.getConfig().getInt("resetplaytimes-command.done-sound.volume", 1);
                                    float soundPitch = plugin.getConfig().getInt("resetplaytimes-command.done-sound.pitch", 1);
                                    Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(soundName.toLowerCase()));
                                    player.playSound(player.getLocation(), sound, soundPitch, soundVol);

                                    //Sending an alert in the server's logger.
                                    plugin.getLogger().warning("[!] Staff: "+player.getName()+" wiped out every player's MAIN PLAYTIME.");
                                });
                            });
                        }
                        else{
                            Component warningMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(plugin.getConfig().getString("resetplaytimes-command.warning-chat-message", "&7&l[&4&l!&7&l] &eThis will wipe out every player's &lmain &eplaytime! If you wish to continue, &crun this command again."));
                            player.sendMessage(warningMessage);

                            String soundName = plugin.getConfig().getString("resetplaytimes-command.warning-sound.name", "block.note_block.pling");
                            float soundVol = plugin.getConfig().getInt("resetplaytimes-command.warning-sound.volume", 1);
                            float soundPitch = plugin.getConfig().getInt("resetplaytimes-command.warning-sound.pitch", 1);
                            Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(soundName.toLowerCase()));
                            player.playSound(player.getLocation(), sound, soundPitch, soundVol);

                            resetPlaytimesUuidSet.add(player.getUniqueId());
                        }
                    }

                    // /ptu setplaytime <ign> <playtime>
                    case "setplaytime" -> {
                        if(!player.hasPermission("playtimeutils.ptutils.setplaytime")){
                            noPermission(player);
                            return true;
                        }

                        if(args.length < 3){
                            player.sendMessage(LegacyComponentSerializer.legacySection().deserialize("&cUsage: &l/ptutils setplaytime <IGN> <playtime &e-> ex: 2d 4h 30m 35s>"));
                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                            return true;
                        }

                        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[1]);
                        if(!targetPlayer.hasPlayedBefore()){
                            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cThis player hasn't played on the server before!"));
                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                            return true;
                        }

                        //Getting the seconds from the input
                        StringBuilder playtimeBuilder = new StringBuilder();
                        for(int i = 2; i < args.length; i++){
                            playtimeBuilder.append(args[i]).append(" ");
                        }
                        String playtime = playtimeBuilder.toString().trim();
                        long secondsFromPlaytime = getSecondsFromPlaytimeString(playtime);

                        if(setPlaytimesUuidSet.contains(player.getUniqueId())){
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                //Changing the main playtime of the target player in DB
                                String SQL = "UPDATE playersPlaytime SET mainPlaytime = ? WHERE uuid = ?";
                                try(Connection connection = plugin.getDatabaseManager().getDataSource().getConnection(); PreparedStatement ps = connection.prepareStatement(SQL)){
                                    ps.setLong(1, secondsFromPlaytime);
                                    ps.setString(2, targetPlayer.getUniqueId().toString());

                                } catch (Exception e){
                                    plugin.getLogger().warning("Wiping playtime for player failed! See message: "+e.getMessage());
                                    return;
                                }

                                //Wiping the current value from the main playtime map
                                if(targetPlayer.isOnline()){
                                    plugin.getMainPlaytimeMap().remove(targetPlayer.getUniqueId());
                                    plugin.getMainPlaytimeMap().put(targetPlayer.getUniqueId(), 0);
                                }

                                //Removing the staff from the caching set
                                setPlaytimesUuidSet.remove(targetPlayer.getUniqueId());

                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    String message = plugin.getConfig().getString("setplaytime-command.done-chat-message", "&a&lSuccessfully set the playtime of &e&l%player_name% &ato &e&l%playtime_value%&a!")
                                            .replace("%player_name%", targetPlayer.getName())
                                            .replace("%playtime_value%", playtime);
                                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));

                                    String soundName = plugin.getConfig().getString("setplaytime-command.done-sound.name", "entity.player.levelup");
                                    float volume = plugin.getConfig().getInt("setplaytime-command.done-sound.volume", 1);
                                    float pitch = plugin.getConfig().getInt("setplaytime-command.done-sound.pitch", 1);
                                    Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(soundName.toLowerCase()));
                                    player.playSound(player.getLocation(), sound, volume, pitch);

                                    //Sending a warning log.
                                    plugin.getLogger().warning("[!] Staff: "+player.getName()+" set "+targetPlayer.getName()+"'s main playtime to "+playtime+".");
                                });
                            });
                        }
                        else{
                            setPlaytimesUuidSet.add(player.getUniqueId());

                            String message = plugin.getConfig().getString("setplaytime-command.warning-chat-message", "&7&l[&4&l!&7&l] &eThis will set the player's playtime to &l%playtime_value%&e! If you wish to continue, &crun this command again.")
                                    .replace("%playtime_value%", playtime);
                            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));

                            String soundName = plugin.getConfig().getString("setplaytime-command.warning-sound.name", "block.note_block.pling");
                            float volume = plugin.getConfig().getInt("setplaytime-command.warning-sound.volume", 1);
                            float pitch = plugin.getConfig().getInt("setplaytime-command.warning-sound.pitch", 1);
                            Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(soundName.toLowerCase()));
                            player.playSound(player.getLocation(), sound, volume, pitch);
                        }
                    }
                }
            }

            default -> {
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cUnknown command."));
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                return true;
            }
        }

        return false;
    }

    private boolean isUrlValid(String url){
        if(!url.contains("discord.gg")) return false;

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
            component = component.replaceText(TextReplacementConfig.builder().match("Discord").replacement(discordWord).build());
        }
        return component;
    }

    private void noPermission(Player player){
        String noPermissionMessage = LegacyComponentSerializer.legacyAmpersand().serialize(Component.text(plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to do that!")));
        noPermissionMessage = plugin.setPP(noPermissionMessage, player);

        player.sendMessage(noPermissionMessage);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
    }

    private long getSecondsFromPlaytimeString(String playtime){
        long total_seconds = 0;

        Pattern Time_Pattern = Pattern.compile("(\\\\d+)\\\\s*([dhmsDHMS])");
        Matcher matcher = Time_Pattern.matcher(playtime);

        while(matcher.find()){
            long value = Long.parseLong(matcher.group(1));
            char unit = matcher.group(2).toLowerCase().charAt(0);

            switch (unit) {
                case 'd' -> total_seconds += value * 86400L;
                case 'h' -> total_seconds += value * 3600L;
                case 'm' -> total_seconds += value * 60L;
                case 's' -> total_seconds += value;
            }
        }

        return total_seconds;
    }
}
