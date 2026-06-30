//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils.RewardSystem.GUIs;

import me.itsandrew.playtimeUtils.PlaytimeUtils;
import me.itsandrew.playtimeUtils.RewardSystem.States.AddRemoveChoice;
import me.itsandrew.playtimeUtils.RewardSystem.States.PlacementChoice;
import me.itsandrew.playtimeUtils.RewardSystem.States.RewardType;
import me.itsandrew.playtimeUtils.RewardSystem.States.StaffState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ChoosePlaceGUI implements Listener {
    private final PlaytimeUtils plugin;

    public ChoosePlaceGUI(PlaytimeUtils plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player){
        Inventory GUI = plugin.getServer().createInventory(null, 54, Component.text("Choose The Placement"));

        //Decoration glass
        plugin.decorationSetup(GUI);

        //Return Button
        ItemStack returnButton = new ItemStack(Material.ARROW);
        ItemMeta returnButtonMeta = returnButton.getItemMeta();
        if(returnButtonMeta != null) returnButtonMeta.displayName(MiniMessage.miniMessage().deserialize("<#ff003c><b>Return"));
        returnButton.setItemMeta(returnButtonMeta);
        GUI.setItem(40, returnButton);

        StaffState staffState = plugin.getStaffStates().get(player);

        //First, Second and Third Place buttons
        ItemStack firstPlaceButton = new ItemStack(Material.GOLD_INGOT);
        ItemMeta firstPlaceButtonMeta = firstPlaceButton.getItemMeta();
        if(firstPlaceButtonMeta != null){
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(" "));
            String line = "Click to ";
            line += staffState.choice == AddRemoveChoice.ADD ? "add" : "remove";
            line += " rewards for the First Place.";
            lore.add(MiniMessage.miniMessage().deserialize("<gray>" + line));

            firstPlaceButtonMeta.displayName(PlacementChoice.FIRST.toDeserializedComponent());
            firstPlaceButtonMeta.lore(lore);
            firstPlaceButton.setItemMeta(firstPlaceButtonMeta);
            GUI.setItem(20, firstPlaceButton);
        }

        ItemStack secondPlaceButton = new ItemStack(Material.IRON_INGOT);
        ItemMeta secondPlaceButtonMeta = secondPlaceButton.getItemMeta();
        if(secondPlaceButtonMeta != null){
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(" "));
            String line = "Click to ";
            line += staffState.choice == AddRemoveChoice.ADD ? "add" : "remove";
            line += " rewards for the Second Place.";
            lore.add(MiniMessage.miniMessage().deserialize("<gray>" + line));

            secondPlaceButtonMeta.displayName(PlacementChoice.SECOND.toDeserializedComponent());
            secondPlaceButtonMeta.lore(lore);
            secondPlaceButton.setItemMeta(secondPlaceButtonMeta);
            GUI.setItem(22, secondPlaceButton);
        }

        ItemStack thirdPlaceButton = new ItemStack(Material.COAL);
        ItemMeta thirdPlaceButtonMeta = thirdPlaceButton.getItemMeta();
        if(thirdPlaceButtonMeta != null){
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(" "));
            String line = "Click to ";
            line += staffState.choice == AddRemoveChoice.ADD ? "add" : "remove";
            line += " rewards for the Third Place.";
            lore.add(MiniMessage.miniMessage().deserialize("<gray>" + line));

            thirdPlaceButtonMeta.displayName(PlacementChoice.THIRD.toDeserializedComponent());
            thirdPlaceButtonMeta.lore(lore);
            thirdPlaceButton.setItemMeta(thirdPlaceButtonMeta);
            GUI.setItem(24, thirdPlaceButton);
        }

        player.openInventory(GUI);
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event){
        if(!event.getView().title().contains(Component.text("Choose The Placement"))) return;
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if(clickedItem == null || clickedItem.getType().equals(Material.BLACK_STAINED_GLASS_PANE)) return;

        Player player = (Player) event.getWhoClicked();
        StaffState staffState = plugin.getStaffStates().get(player);

        Material clickedMat = clickedItem.getType();
        switch(clickedMat){
            case IRON_INGOT -> staffState.placement = PlacementChoice.SECOND;
            case GOLD_INGOT -> staffState.placement = PlacementChoice.FIRST;
            case COAL -> staffState.placement = PlacementChoice.THIRD;
            case ARROW -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                plugin.getItemsOrExpGUI().openGUI(player);
                return;
            }
        }

        if(staffState.choice == AddRemoveChoice.ADD)
            if(staffState.rewardType == RewardType.EXP){
                FileConfiguration config = plugin.getConfig();

                player.closeInventory();
                plugin.playerInput(player, message -> {
                    try{
                        double expLevels = Double.parseDouble(String.valueOf(message));
                        config.set("reward-system.rewards."+staffState.placement.toStringForm()+".exp-levels", expLevels);
                        plugin.saveConfig();

                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Saved <b>" + expLevels + "</b> Exp Levels for <b>"+staffState.placement.toString().toLowerCase()+" place</b>!"));
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.4f);
                    } catch (Exception e){
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Invalid input! Please enter a number of <b>exp levels</b>."));
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    }
                });

                plugin.getStaffStates().remove(player);
            }
            else plugin.getAddRewardsGUI().openGUI(player);

        else{
            if(staffState.rewardType == RewardType.EXP){
                FileConfiguration config = plugin.getConfig();

                config.set("reward-system.rewards."+staffState.placement.toStringForm()+".exp-levels", 0);
                plugin.saveConfig();

                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Removed <b>All Exp Levels</b> for <b>"+staffState.placement.toString().toLowerCase()+" place</b>!"));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.4f);

                plugin.getStaffStates().remove(player);
            }
            else plugin.getRemoveRewardsGUI().openFirstGUI(player);
        }
    }
}
