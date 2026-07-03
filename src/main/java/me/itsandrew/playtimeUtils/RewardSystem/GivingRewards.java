//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils.RewardSystem;

import me.itsandrew.playtimeUtils.PlaytimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;

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
        boolean oneThirdMessage = false;
        boolean halfMessage = false;
        boolean fiveSixthMessage = false;
        boolean tournamentEnded = false;

        FileConfiguration mainConfig = plugin.getConfig();
        long tournamentDuration = mainConfig.getLong("reward-system.tournament-duration");
        long tournamentStart = mainConfig.getLong("reward-system.tournament-start");
        rewardingTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if(System.currentTimeMillis() >= tournamentStart + tournamentDuration && !tournamentEnded){

            }

            
        }, 0, 20);

        broadcastTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            //Sending the broadcast message when the timer gets to a third of the time
            if(System.currentTimeMillis() >= tournamentStart + tournamentDuration /3 && !oneThirdMessage){

            }

            //Sending the broadcast message when the timer gets to half the time
            if(System.currentTimeMillis() >= tournamentStart + tournamentDuration /2 && !halfMessage){

            }

            //Sending the broadcast message when the timer gets to 5/6 of the time
            if(System.currentTimeMillis() >= tournamentStart + 5 * tournamentDuration /6 && !fiveSixthMessage){

            }
        }, 0, 20);
    }

    private void rewardPlayer(OfflinePlayer player){

    }

    //Getter for the Task
    public BukkitTask getRewardingTask() {
        return rewardingTask;
    }
}
