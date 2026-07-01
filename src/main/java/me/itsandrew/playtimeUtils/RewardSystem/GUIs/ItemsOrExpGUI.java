//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils.RewardSystem.GUIs;

import me.itsandrew.playtimeUtils.PlaytimeUtils;
import me.itsandrew.playtimeUtils.RewardSystem.States.AddRemoveChoice;
import me.itsandrew.playtimeUtils.RewardSystem.States.RewardType;
import me.itsandrew.playtimeUtils.RewardSystem.States.StaffState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemsOrExpGUI implements Listener {
    private final PlaytimeUtils plugin;

    public ItemsOrExpGUI(PlaytimeUtils plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player){
        Inventory GUI = plugin.getServer().createInventory(null, 54, Component.text("Choose a Type of Reward"));

        //Setting some decoration
        plugin.decorationSetup(GUI);

        //Adding the Items Button
        ItemStack itemsButton = new ItemStack(Material.ENDER_CHEST);
        ItemMeta itemsButtonMeta = itemsButton.getItemMeta();
        if(itemsButtonMeta != null){
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(" "));

            String line = "Click to ";
            line += plugin.getStaffStates().get(player).choice == AddRemoveChoice.ADD ? "add" : "remove";
            line += " items to the reward pool.";
            lore.add(MiniMessage.miniMessage().deserialize("<gray>" + line));

            Component itemDisplayName = MiniMessage.miniMessage().deserialize("<gradient:#2fff05:#03ff9e><b>Items</gradient>");
            itemsButtonMeta.displayName(itemDisplayName);
            itemsButtonMeta.lore(lore);
            itemsButton.setItemMeta(itemsButtonMeta);
        }
        GUI.setItem(20, itemsButton);

        //Adding the Exp Button
        ItemStack expButton = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta expButtonMeta = expButton.getItemMeta();
        if(expButtonMeta != null){
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(" "));

            String line = "Click to ";
            line += plugin.getStaffStates().get(player).choice == AddRemoveChoice.ADD ? "add" : "remove";
            line += " experience levels to the reward pool.";
            lore.add(MiniMessage.miniMessage().deserialize("<gray>" + line));

            Component expDisplayName = MiniMessage.miniMessage().deserialize("<gradient:#21a6ff:#8b85ff><b>Experience Levels</gradient> ");
            expButtonMeta.displayName(expDisplayName);
            expButtonMeta.lore(lore);
            expButton.setItemMeta(expButtonMeta);
        }
        GUI.setItem(24, expButton);

        //Adding a close button
        ItemStack closeButton = new ItemStack(Material.RED_CONCRETE);
        ItemMeta closeButtonMeta = closeButton.getItemMeta();
        if(closeButtonMeta != null){
            closeButtonMeta.displayName(MiniMessage.miniMessage().deserialize("<#aa0000><b>Close"));
            closeButton.setItemMeta(closeButtonMeta);
        }
        GUI.setItem(40, closeButton);

        player.openInventory(GUI);
    }

    @EventHandler
    public void OnGuiClick(InventoryClickEvent event){
        if(!(event.getWhoClicked() instanceof Player player)) return;
        if(!event.getView().title().equals(Component.text("Choose a Type of Reward"))) return;
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if(clickedItem == null || clickedItem.getType().equals(Material.BLACK_STAINED_GLASS_PANE)) return;

        //Getting the player and it's StaffState
        StaffState playerStaffState = plugin.getStaffStates().get(player);

        Material clickedMat = clickedItem.getType();
        switch (clickedMat){
            case ENDER_CHEST -> {
                playerStaffState.rewardType = RewardType.ITEMS;

                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                plugin.getChoosePlaceGUI().openGUI(player);
            }
            case EXPERIENCE_BOTTLE -> {
                playerStaffState.rewardType = RewardType.EXP;

                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                plugin.getChoosePlaceGUI().openGUI(player);
            }
            case RED_CONCRETE -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                plugin.getStaffStates().remove(player);
                player.closeInventory();
                return;
            }
            default -> event.setCancelled(true);
        }
    }
}
