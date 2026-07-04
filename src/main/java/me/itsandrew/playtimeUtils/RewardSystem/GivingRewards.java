//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils.RewardSystem;

import me.clip.placeholderapi.PlaceholderAPI;
import me.itsandrew.playtimeUtils.PlaytimeUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class GivingRewards {
    private final PlaytimeUtils plugin;
    private BukkitTask rewardingTask;
    private BukkitTask broadcastTask;

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
        long tournamentDuration = mainConfig.getLong("reward-system.tournament-duration");
        long tournamentStart = mainConfig.getLong("reward-system.tournament-start");
        rewardingTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if(System.currentTimeMillis() >= tournamentStart + tournamentDuration && !tournamentEnded.get()){
                tournamentEnded.set(true);

                //Sending the broadcast message/sound
                String soundName = mainConfig.getString("reward-system.tournament-sounds.end.name", "ENTITY_PLAYER_LEVELUP");
                float soundVolume = mainConfig.getInt("reward-system.tournament-sounds.end.volume", 1);
                float soundPitch = mainConfig.getInt("reward-system.tournament-sounds.end.pitch", 1);
                Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(soundName.toUpperCase()));
                for(Player onlinePlayer : Bukkit.getOnlinePlayers()){
                    onlinePlayer.playSound(onlinePlayer.getLocation(), sound, soundVolume, soundPitch);

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

                //Resetting the tournament settings
                mainConfig.set("reward-system.tournament-duration", null);
                mainConfig.set("reward-system.tournament-start", null);
                mainConfig.set("reward-system.tournament-end", null);
                plugin.saveConfig();
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
                Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(soundName.toUpperCase()));
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
                Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(soundName.toUpperCase()));
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
                Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(soundName.toUpperCase()));
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
    }

    private void rewardWinners(){

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
