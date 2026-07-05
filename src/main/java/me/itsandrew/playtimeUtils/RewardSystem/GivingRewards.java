//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils.RewardSystem;

import me.clip.placeholderapi.PlaceholderAPI;
import me.itsandrew.playtimeUtils.PlaytimeUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class GivingRewards {
    private final PlaytimeUtils plugin;
    private BukkitTask rewardingTask;
    private BukkitTask broadcastTask;
    private BukkitTask inventorySpaceWarningTask;

    public GivingRewards(PlaytimeUtils plugin) {
        this.plugin = plugin;

        startTasks();
    }

    public void startTasks(){
        //Checking if the reward system is enabled
        boolean toggleRewardSystem = plugin.getConfig().getBoolean("reward-system.toggle", false);
        if(!toggleRewardSystem) return;

        //Checking if the tournament has started
        long duration = plugin.getConfig().getLong("reward-system.tournament-duration", 0);
        if(duration == 0) return;

        //Booleans for messages/tournament ending
        AtomicBoolean oneThirdMessage = new AtomicBoolean(false);
        AtomicBoolean halfMessage = new AtomicBoolean(false);
        AtomicBoolean fiveSixthMessage = new AtomicBoolean(false);
        AtomicBoolean tournamentEnded = new AtomicBoolean(false);

        FileConfiguration mainConfig = plugin.getConfig();

        //Running an Async task to get the tournament timestamps from the database.
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            long tournamentDuration = plugin.getDatabaseManager().getTournamentTimestamp("duration");
            long tournamentStart = plugin.getDatabaseManager().getTournamentTimestamp("tournamentStart");
            long tournamentEnd = plugin.getDatabaseManager().getTournamentTimestamp("tournamentEnd");

            //Jumping back to the main thread to start the tasks
            Bukkit.getScheduler().runTask(plugin, () -> {
                rewardingTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    if(System.currentTimeMillis() >= tournamentEnd && !tournamentEnded.get()){
                        tournamentEnded.set(true);

                        //Sending the broadcast message/sound
                        String soundName = mainConfig.getString("reward-system.tournament-sounds.end.name", "ENTITY_PLAYER_LEVELUP");
                        double soundVolume = mainConfig.getDouble("reward-system.tournament-sounds.end.volume", 1);
                        double soundPitch = mainConfig.getDouble("reward-system.tournament-sounds.end.pitch", 1);
                        Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(soundName.toLowerCase()));
                        for(Player onlinePlayer : Bukkit.getOnlinePlayers()){
                            onlinePlayer.playSound(onlinePlayer.getLocation(), sound, (float) soundVolume, (float) soundPitch);

                            List<String> messageLines = plugin.getConfig().getStringList("reward-system.tournament-messages.end");
                            for(String line : messageLines){
                                line = PlaceholderAPI.setPlaceholders(onlinePlayer, line);
                                Component coloredLine = LegacyComponentSerializer.legacyAmpersand().deserialize(line);
                                coloredLine = replaceDiscordComponent(coloredLine);
                                onlinePlayer.sendMessage(coloredLine);
                            }
                        }

                        //Giving the rewards to the players
                        rewardWinners();

                        //Deleting the tournament timestamps from the database.
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getDatabaseManager().deleteTournamentTimestamps());
                        plugin.getLogger().info("Tournament has ended successfully!");

                        //Stopping the tasks
                        broadcastTask.cancel();
                        rewardingTask.cancel();
                    }
                }, 0, 20);

                broadcastTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    //Sending the broadcast message when the timer gets to a third of the time
                    if(System.currentTimeMillis() >= tournamentStart + tournamentDuration / 3 && !oneThirdMessage.get()){
                        oneThirdMessage.set(true);

                        String soundName = mainConfig.getString("reward-system.tournament-sounds.1/3-of-duration.name", "BLOCK_NOTE_BLOCK_PLING");
                        float soundVolume = mainConfig.getInt("reward-system.tournament-sounds.1/3-of-duration.volume", 1);
                        float soundPitch = mainConfig.getInt("reward-system.tournament-sounds.1/3-of-duration.pitch", 1);
                        Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(soundName.toLowerCase()));
                        for(Player onlinePlayer : Bukkit.getOnlinePlayers()){
                            onlinePlayer.playSound(onlinePlayer.getLocation(), sound, soundVolume, soundPitch);

                            List<String> messageLines = plugin.getConfig().getStringList("reward-system.tournament-messages.1/3-of-duration");
                            for(String line : messageLines){
                                line = PlaceholderAPI.setPlaceholders(onlinePlayer, line);
                                Component coloredLine = LegacyComponentSerializer.legacyAmpersand().deserialize(line);
                                coloredLine = replaceDiscordComponent(coloredLine);

                                onlinePlayer.sendMessage(coloredLine);
                            }
                        }
                    }

                    //Sending the broadcast message when the timer gets to half the time
                    if(System.currentTimeMillis() >= tournamentStart + tournamentDuration / 2 && !halfMessage.get()){
                        halfMessage.set(true);

                        String soundName = mainConfig.getString("reward-system.tournament-sounds.half-of-duration.name", "BLOCK_NOTE_BLOCK_PLING");
                        float soundVolume = mainConfig.getInt("reward-system.tournament-sounds.half-of-duration.volume", 1);
                        float soundPitch = mainConfig.getInt("reward-system.tournament-sounds.half-of-duration.pitch", 1);
                        Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(soundName.toLowerCase()));
                        for(Player onlinePlayer : Bukkit.getOnlinePlayers()){
                            onlinePlayer.playSound(onlinePlayer.getLocation(), sound, soundVolume, soundPitch);

                            List<String> messageLines = plugin.getConfig().getStringList("reward-system.tournament-messages.half-of-duration");
                            for(String line : messageLines){
                                line = PlaceholderAPI.setPlaceholders(onlinePlayer, line);
                                Component coloredLine = LegacyComponentSerializer.legacyAmpersand().deserialize(line);
                                coloredLine = replaceDiscordComponent(coloredLine);

                                onlinePlayer.sendMessage(coloredLine);
                            }
                        }
                    }

                    //Sending the broadcast message when the timer gets to 5/6 of the time
                    if(System.currentTimeMillis() >= tournamentStart + 5 * tournamentDuration / 6 && !fiveSixthMessage.get()){
                        fiveSixthMessage.set(true);

                        String soundName = mainConfig.getString("reward-system.tournament-sounds.5/6-of-duration.name", "BLOCK_NOTE_BLOCK_PLING");
                        float soundVolume = mainConfig.getInt("reward-system.tournament-sounds.5/6-of-duration.volume", 1);
                        float soundPitch = mainConfig.getInt("reward-system.tournament-sounds.5/6-of-duration.pitch", 1);
                        Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(soundName.toLowerCase()));
                        for(Player onlinePlayer : Bukkit.getOnlinePlayers()){
                            onlinePlayer.playSound(onlinePlayer.getLocation(), sound, soundVolume, soundPitch);

                            List<String> messageLines = plugin.getConfig().getStringList("reward-system.tournament-messages.5/6-of-duration");
                            for(String line : messageLines){
                                line = PlaceholderAPI.setPlaceholders(onlinePlayer, line);
                                Component coloredLine = LegacyComponentSerializer.legacyAmpersand().deserialize(line);
                                coloredLine = replaceDiscordComponent(coloredLine);

                                onlinePlayer.sendMessage(coloredLine);
                            }
                        }
                    }
                }, 0, 20);
            });
        });
    }

    private void rewardWinners(){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
           List<Map.Entry<UUID, Integer>>top3tournament = plugin.getDatabaseManager().getTournamentTop3Players();

           //Running the task to give the rewards on the main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                for(int i = 0; i<top3tournament.size(); i++){
                    int rank = i + 1;
                    OfflinePlayer winner = Bukkit.getOfflinePlayer(top3tournament.get(i).getKey());
                    UUID winnerUUID = winner.getUniqueId();

                    //Checking if the winner is online or not.
                    if(winner.isOnline()){
                        //Giving the winner a 'Rewards Item' which he can interact with to get the rewards.
                        ItemStack rewardsItem = new ItemStack(Material.valueOf(plugin.getConfig().getString("reward-system.rewards-item.material", "DIAMOND_SWORD").toUpperCase()), 1);
                        ItemMeta riMeta = rewardsItem.getItemMeta();

                        if(riMeta != null){
                            NamespacedKey rewardItemKey = NamespacedKey.minecraft("reward_item");

                            //Setting the display name
                            String DisplayName = plugin.getConfig().getString("reward-system.rewards-item.display-name", "%player_tournamentValue% &a&lReward");
                            DisplayName = PlaceholderAPI.setPlaceholders(winner, DisplayName);
                            Component realDisplayName = LegacyComponentSerializer.legacyAmpersand().deserialize(DisplayName);
                            realDisplayName = realDisplayName.replaceText(TextReplacementConfig.builder().match("%player_tournamentValue%").replacement(getFormattedPlacementString(rank)).build());
                            riMeta.displayName(realDisplayName);

                            //Setting the lore
                            List<String> rawLore = plugin.getConfig().getStringList("reward-system.rewards-item.lore");
                            List<Component> lore = new ArrayList<>();
                            for(String line : rawLore){
                                line = PlaceholderAPI.setPlaceholders(winner, line);
                                Component coloredLine = LegacyComponentSerializer.legacyAmpersand().deserialize(line);
                                lore.add(coloredLine);
                            }
                            riMeta.lore(lore);

                            //Adding persistent data to the item.
                            riMeta.getPersistentDataContainer().set(rewardItemKey, PersistentDataType.INTEGER, rank);
                        }

                        rewardsItem.setItemMeta(riMeta);

                        //Checking if the player has inventory space
                        HashMap<Integer,ItemStack> attemptToAdd = winner.getPlayer().getInventory().addItem(rewardsItem);
                        if(!attemptToAdd.isEmpty()){
                            //Starting a task in order to tell the winner to open the rewards menu to collect the reward
                            inventorySpaceWarningTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                                AtomicLong pendingRewardAmount = new AtomicLong();
                                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> pendingRewardAmount.set(plugin.getDatabaseManager().getPendingRewardAmount(winnerUUID, rank)));

                                if(pendingRewardAmount.get() != 0){

                                }
                                else inventorySpaceWarningTask.cancel();
                            }, 0, 1200);
                        }
                        else winner.getPlayer().getInventory().addItem(rewardsItem);
                    }
                    //If the winner is offline, adding his UUID to the Pending Rewards Table in the DB.
                    else Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getDatabaseManager().insertPendingReward(winnerUUID, rank));
                }
            });
        });
    }

    private String getStringRank(int rank){
        String rankString = "";
        switch(rank){
            case 1 -> rankString = "first-place";
            case 2 -> rankString = "second-place";
            case 3 -> rankString = "third-place";
        }

        return rankString;
    }

    private Component getFormattedPlacementString(int rank){
        Component formattedPlacementString = Component.empty();
        switch(rank){
            case 1 -> formattedPlacementString = MiniMessage.miniMessage().deserialize("<gradient:#ffee55:#ffaa00><b>1st Place");
            case 2 -> formattedPlacementString = MiniMessage.miniMessage().deserialize("<gradient:#ffffff:#bbbacc><b>2nd Place");
            case 3 -> formattedPlacementString = MiniMessage.miniMessage().deserialize("<gradient:#ccc923:#e6765a><b>3rd Place");
        }

        return formattedPlacementString;
    }

    private boolean isUrlValid(String url) {
        try {
            URI uri = new URI(url);
            return uri.getScheme() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private Component replaceDiscordComponent(Component component){
        //Adding a component designed to have a hover and click event (to open the discord link)
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

    //Getter for the Task
    public BukkitTask getRewardingTask() {
        return rewardingTask;
    }
    public BukkitTask getBroadcastTask() {
        return broadcastTask;
    }
}
