package me.itsandrew.playtimeUtils.RewardSystem.GUIs;

import me.itsandrew.playtimeUtils.PlaytimeUtils;
import me.itsandrew.playtimeUtils.RewardSystem.States.StaffState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class AddRewardsGUI implements Listener {
    private final PlaytimeUtils plugin;
    private final NamespacedKey container;

    public AddRewardsGUI(PlaytimeUtils plugin) {
        this.plugin = plugin;
        this.container = new NamespacedKey(plugin, "container");
    }

    public void openGUI(Player player){
        Inventory GUI = plugin.getServer().createInventory(null, 27, Component.text("Add Items"));
        StaffState state = plugin.getStaffStates().get(player);

        //Return Button
        ItemStack returnButton = new ItemStack(Material.ARROW);
        ItemMeta returnButtonItemMeta = returnButton.getItemMeta();
        if(returnButtonItemMeta != null) {
            returnButtonItemMeta.getPersistentDataContainer().set(container, PersistentDataType.STRING, "return");
            returnButtonItemMeta.displayName(MiniMessage.miniMessage().deserialize("<#ff003c><b>Return"));
        }
        returnButton.setItemMeta(returnButtonItemMeta);
        GUI.setItem(21, returnButton);

        //Save Button
        ItemStack saveButton = new ItemStack(Material.GREEN_CONCRETE);
        ItemMeta saveButtonMeta = saveButton.getItemMeta();
        if(saveButtonMeta != null){
            saveButtonMeta.displayName(MiniMessage.miniMessage().deserialize("<green><b>Save Rewards for "+state.placement.toColoredStringForm()+"<green><b>!"));
            saveButtonMeta.getPersistentDataContainer().set(container, PersistentDataType.STRING, "save");
        }
        saveButton.setItemMeta(saveButtonMeta);
        GUI.setItem(23, saveButton);

        player.openInventory(GUI);
    }

    @EventHandler
    public void onGuiClose(InventoryCloseEvent event){
        if(!event.getView().title().equals(Component.text("Add Items"))) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player player = (Player) event.getPlayer();
            InventoryView currentView = player.getOpenInventory();
            Component title = currentView.title();

            if(!title.equals(Component.text("Choose The Placement"))) plugin.getStaffStates().remove(player);
        }, 1);
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event){
        if(!event.getView().title().equals(Component.text("Add Items"))) return;

        ItemStack clickedItem = event.getCurrentItem();
        if(clickedItem == null || clickedItem.getType().equals(Material.BLACK_STAINED_GLASS_PANE)) return;

        ItemMeta clickedMeta = clickedItem.getItemMeta();
        if(clickedMeta == null) return;

        Player player = (Player) event.getWhoClicked();

        if(clickedMeta.getPersistentDataContainer().has(container, PersistentDataType.STRING)){
            event.setCancelled(true);

            String action = clickedMeta.getPersistentDataContainer().get(container, PersistentDataType.STRING);
            switch(action){
                case "return" -> {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    plugin.getChoosePlaceGUI().openGUI(player);
                }
                case "save" -> {
                    Inventory GUI = event.getView().getTopInventory();
                    FileConfiguration mainConfig = plugin.getConfig();
                    StaffState state = plugin.getStaffStates().get(player);

                    List<ItemStack> items = new ArrayList<>();

                    for(int i = 0; i < GUI.getSize() - 9; i++){
                        ItemStack item = GUI.getItem(i);
                        if(item == null) continue;

                        items.add(item);
                    }

                    //Checking if there are any items in the GUI
                    if(items.isEmpty()){
                        player.closeInventory();
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You must put at least <b>1 item</b> in the GUI!"));
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                        return;
                    }

                    //Saving the items in the config.yml
                    List<?> rawList = mainConfig.getList("reward-system.rewards."+state.placement.toConfigFileForm()+".items");
                    List<ItemStack> currentItems = new ArrayList<>();
                    if(rawList != null){
                        for(Object item : rawList){
                            if(item instanceof ItemStack) currentItems.add((ItemStack) item);
                            else plugin.getLogger().warning("[PlaytimeUtils] Found invalid item in the "+state.placement.toString()+" place reward list.");
                        }
                    }

                    currentItems.addAll(items);
                    mainConfig.set("reward-system.rewards."+state.placement.toConfigFileForm()+".items", currentItems);
                    plugin.saveConfig();

                    player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Saved <b>"+items.size()+" item(s)</b> for "+state.placement.toColoredStringForm()+"<green>!"));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.4f);
                    player.closeInventory();
                }
                case null, default -> event.setCancelled(true);
            }
        }
    }
}
