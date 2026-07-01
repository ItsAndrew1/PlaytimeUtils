//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils.RewardSystem.GUIs;

import me.itsandrew.playtimeUtils.PlaytimeUtils;
import me.itsandrew.playtimeUtils.RewardSystem.States.StaffState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
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

public class RemoveRewardsGUIs implements Listener {
    private final PlaytimeUtils plugin;
    private final NamespacedKey container;

    public RemoveRewardsGUIs(PlaytimeUtils plugin) {
        this.plugin = plugin;
        this.container = new NamespacedKey(plugin, "container");
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
            if(noItemsMeta != null){
                noItemsMeta.displayName(MiniMessage.miniMessage().deserialize("<red><b>There are no items configured"));
                noItemsMeta.getPersistentDataContainer().set(container, PersistentDataType.STRING, "no-items");
            }
            noItems.setItemMeta(noItemsMeta);
            GUI.setItem(22, noItems);
        }
        else{
            for(int i = 0; i < currentItems.size() && i < 36; i++){
                ItemStack item = currentItems.get(i);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.getPersistentDataContainer().set(container, PersistentDataType.STRING, "itemToRemove");
                GUI.setItem(i+9, item);
            }
        }

        player.openInventory(GUI);
    }

    private void openSecondGUI(Player player){
        Inventory GUI = plugin.getServer().createInventory(null, 36, Component.text("Remove Item?"));

        //Confirm Button
        ItemStack confirmButton = new ItemStack(Material.GREEN_CONCRETE);
        ItemMeta confirmButtonMeta = confirmButton.getItemMeta();
        if(confirmButtonMeta != null) confirmButtonMeta.displayName(MiniMessage.miniMessage().deserialize("<green><b>Confirm"));
        confirmButton.setItemMeta(confirmButtonMeta);
        GUI.setItem(24, confirmButton);

        //Return Button
        ItemStack returnButton = new ItemStack(Material.RED_CONCRETE);
        ItemMeta returnButtonMeta = returnButton.getItemMeta();
        if(returnButtonMeta != null) returnButtonMeta.displayName(MiniMessage.miniMessage().deserialize("<#ff003c><b>Return"));
        returnButton.setItemMeta(returnButtonMeta);
        GUI.setItem(20, returnButton);

        //Displaying the item to be removed
        StaffState state = plugin.getStaffStates().get(player);
        GUI.setItem(13, state.itemToRemove);

        player.openInventory(GUI);
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
        List<?> rawList = plugin.getConfig().getList("reward-system.rewards."+state.placement.toConfigFileForm()+".items");
        List<ItemStack> currentItems = new ArrayList<>();

        if(rawList != null){
            for(Object item : rawList){
                if(item instanceof ItemStack) currentItems.add((ItemStack) item);
            }
        }

        return currentItems;
    }

    @EventHandler
    public void onFirstGuiClick(InventoryClickEvent event){
        if(!event.getView().title().equals(Component.text("Remove An Item"))) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();

        ItemStack clickedItem = event.getCurrentItem();
        if(clickedItem == null) return;

        ItemMeta clickedMeta = clickedItem.getItemMeta();
        if(clickedMeta == null) return;

        if(clickedMeta.getPersistentDataContainer().has(container, PersistentDataType.STRING)){
            String data = clickedMeta.getPersistentDataContainer().get(container, PersistentDataType.STRING);
            switch(data){
                case "return" -> {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    plugin.getChoosePlaceGUI().openGUI(player);
                }
                case "itemToRemove" -> {
                    StaffState state = plugin.getStaffStates().get(player);
                    state.itemToRemove = clickedItem;

                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    openSecondGUI(player);
                }
                default -> event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSecondGuiClick(InventoryClickEvent event){
        if(!(event.getWhoClicked() instanceof Player player)) return;
        if(!event.getView().title().equals(Component.text("Remove Item?"))) return;
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if(clickedItem == null) return;

        ItemMeta clickedMeta = clickedItem.getItemMeta();
        if(clickedMeta == null) return;

        StaffState state = plugin.getStaffStates().get(player);

        if(clickedItem.getType().equals(Material.RED_CONCRETE)){
            //Deleting the item to be removed from the state
            state.itemToRemove = null;

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            openFirstGUI(player);
        }

        if(clickedItem.getType().equals(Material.GREEN_CONCRETE)){
            ItemStack itemToRemove = state.itemToRemove;

            //Removing the item from config.yml
            List<ItemStack> itemsList = getCurrentItems(state);
            itemsList.remove(itemToRemove);
            plugin.getConfig().set("reward-system.rewards."+state.placement.toConfigFileForm()+".items", itemsList);
            plugin.saveConfig();

            player.closeInventory();
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.4f);
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Removed <b>item</b> from "+state.placement.toColoredStringForm()+"<green>!"));

            plugin.getStaffStates().remove(player);
        }
    }
}
