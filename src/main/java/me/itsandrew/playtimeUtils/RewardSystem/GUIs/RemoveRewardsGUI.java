//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils.RewardSystem.GUIs;

import me.itsandrew.playtimeUtils.PlaytimeUtils;
import me.itsandrew.playtimeUtils.RewardSystem.States.StaffState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class RemoveRewardsGUI implements Listener {
    private final PlaytimeUtils plugin;
    private NamespacedKey container;

    public RemoveRewardsGUI(PlaytimeUtils plugin) {
        this.plugin = plugin;
    }

    public void openFirstGUI(Player player){
        Inventory GUI = plugin.getServer().createInventory(null, 54, Component.text("Remove An Item"));
        StaffState state = plugin.getStaffStates().get(player);

        //Decoration Glass
        for(int i = 0; i < 9; i++){
            ItemStack glass = setupGlassDeco();
            GUI.setItem(i, glass);
        }
        for(int i = 45; i < 54; i++){
            if(i == 49) continue;

            ItemStack glass = setupGlassDeco();
            GUI.setItem(i, glass);
        }

        //Return Button
        ItemStack returnButton = new ItemStack(Material.ARROW);
        ItemMeta returnButtonMeta = returnButton.getItemMeta();
        if(returnButtonMeta != null){
            returnButtonMeta.displayName(MiniMessage.miniMessage().deserialize("<#ff003c><b>Return"));
            returnButtonMeta.getPersistentDataContainer().set(container, PersistentDataType.STRING, "return");
        }
        returnButton.setItemMeta(returnButtonMeta);
        GUI.setItem(49, returnButton);

        //Displaying the Rewards
        List<ItemStack> currentItems = getCurrentItems(state);
        if(currentItems.isEmpty()){
            ItemStack noItems = new ItemStack(Material.BARRIER);
            ItemMeta noItemsMeta = noItems.getItemMeta();
            if(noItemsMeta != null) noItemsMeta.displayName(MiniMessage.miniMessage().deserialize("<red><b>There are no items configured"));
            noItems.setItemMeta(noItemsMeta);
            GUI.setItem(22, noItems);
        }
        else{
            for(int i = 9; i < 45; i++){
                ItemStack item = currentItems.get(i - 9);
                GUI.setItem(i, item);
            }
        }

        player.openInventory(GUI);
    }

    private void openSecondGUI(Player player){
        Inventory GUI = plugin.getServer().createInventory(null, 54, Component.text("Are You Sure?"));
    }

    private ItemStack setupGlassDeco(){
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if(glassMeta != null){
            glassMeta.displayName(Component.text(" "));
            glassMeta.getPersistentDataContainer().set(container, PersistentDataType.STRING, "deco");
        }
        glass.setItemMeta(glassMeta);
        return glass;
    }

    private List<ItemStack> getCurrentItems(StaffState state){
        List<?> rawList = plugin.getConfig().getList("reward-system.rewards."+state.placement.toStringForm()+".items");
        List<ItemStack> currentItems = new ArrayList<>();

        if(rawList != null){
            for(Object item : rawList){
                if(item instanceof ItemStack) currentItems.add((ItemStack) item);
            }
        }

        return currentItems;
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event){
        Component guiTitle = event.getView().title();
        if(!guiTitle.equals(Component.text("Remove An Item")) || !guiTitle.equals(Component.text("Are You Sure?"))) return;

        switch(guiTitle.toString()){
            case "Remove An Item" -> {
                ItemStack clickedItem = event.getCurrentItem();
                if(clickedItem == null || clickedItem.getType().equals(Material.BLACK_STAINED_GLASS_PANE)) return;

                ItemMeta clickedMeta = clickedItem.getItemMeta();
                if(clickedMeta == null) return;


            }
        }
    }
}
