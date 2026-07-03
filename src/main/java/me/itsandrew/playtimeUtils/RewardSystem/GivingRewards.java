//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils.RewardSystem;

import me.clip.placeholderapi.PlaceholderAPI;
import me.itsandrew.playtimeUtils.PlaytimeUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.net.URI;
import java.net.URL;
import java.util.List;
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


            }
        }, 0, 20);

        broadcastTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            //Sending the broadcast message when the timer gets to a third of the time
            if(System.currentTimeMillis() >= tournamentStart + tournamentDuration / 3 && !oneThirdMessage.get()){
                oneThirdMessage.set(true);

                for(Player onlinePlayer : Bukkit.getOnlinePlayers()){
                    onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);

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

                for(Player onlinePlayer : Bukkit.getOnlinePlayers()){
                    onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);

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

                for(Player onlinePlayer : Bukkit.getOnlinePlayers()){
                    onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);

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

    private void rewardPlayer(OfflinePlayer player){

    }

    private boolean isUrlValid(String url) {
        try {
            URI uri = new URI(url);
            URL realUrl = uri.toURL();
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
