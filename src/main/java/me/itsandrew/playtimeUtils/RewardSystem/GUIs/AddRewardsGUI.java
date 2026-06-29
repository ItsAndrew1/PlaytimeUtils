package me.itsandrew.playtimeUtils.RewardSystem.GUIs;

import me.itsandrew.playtimeUtils.PlaytimeUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class AddRewardsGUI implements Listener {
    private final PlaytimeUtils plugin;

    public AddRewardsGUI(PlaytimeUtils plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player){
        Inventory GUI = plugin.getServer().createInventory(null, 54, Component.text("Add Items"));
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event){

    }
}
