//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils.RewardSystem.GUIs;

import me.itsandrew.playtimeUtils.PlaytimeUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class RemoveRewardsGUI implements Listener {
    private final PlaytimeUtils plugin;

    public RemoveRewardsGUI(PlaytimeUtils plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player){

    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event){

    }
}
