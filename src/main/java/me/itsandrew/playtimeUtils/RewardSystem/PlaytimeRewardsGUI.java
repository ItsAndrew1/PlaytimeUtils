//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils.RewardSystem;

import me.clip.placeholderapi.PlaceholderAPI;
import me.itsandrew.playtimeUtils.PlaytimeUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PlaytimeRewardsGUI implements Listener {
    private final PlaytimeUtils plugin;
    private final NamespacedKey dataContainer;

    public PlaytimeRewardsGUI(PlaytimeUtils plugin) {
        this.plugin = plugin;
        this.dataContainer = plugin.getGivingRewardsSystem().getDataContainer();
    }

    public void openGUI(Player player){
        FileConfiguration config = plugin.getConfig();

        int size = config.getInt("reward-system.playtime-rewards-menu.size", 54);
        if(size % 9 != 0 || size < 0) size = 54;
        Component title = Component.text(config.getString("reward-system.playtime-rewards-menu.title", "Your Playtime Rewards"));

        Inventory GUI = plugin.getServer().createInventory(null, size, title);

        //Menu Decoration
        boolean toggleDeco = config.getBoolean("reward-system.playtime-rewards-menu.decoration.toggle", true);
        if(toggleDeco) {
            String decoDisplayName = config.getString("reward-system.playtime-rewards-menu.decoration.display-name", " ");
            Material decoMat = Material.valueOf(config.getString("reward-system.playtime-rewards-menu.decoration.material", "BLACK_STAINED_GLASS_PANE").toUpperCase());
            List<String> decoLore = config.getStringList("reward-system.playtime-rewards-menu.decoration.lore");
            for(int i = 0; i < 9; i++){
                ItemStack decoItem = createItem(decoMat, decoDisplayName, decoLore, player, "deco");
                GUI.setItem(i, decoItem);
            }
            for(int i = 45; i < 54; i++){
                ItemStack decoItem = createItem(decoMat, decoDisplayName, decoLore, player, "deco");
                GUI.setItem(i, decoItem);
            }
        }

        //Return Button
        Material returnMat = Material.valueOf(config.getString("reward-system.playtime-rewards-menu.return-button.material", "ARROW").toUpperCase());
        int returnSlot = config.getInt("reward-system.playtime-rewards-menu.return-button.slot", 40);
        if(returnSlot < 0 || returnSlot > 54) returnSlot = 40;
        String returnDisplayName = config.getString("reward-system.playtime-rewards-menu.return-button.display-name", "&e&lReturn");
        List<String> returnLore = config.getStringList("reward-system.playtime-rewards-menu.return-button.lore");
        ItemStack returnItem = createItem(returnMat, returnDisplayName, returnLore, player, "return");
        GUI.setItem(returnSlot, returnItem);

        //Getting the Pending Rewards amount
        AtomicInteger firstPlacePendingRewards = new AtomicInteger();
        AtomicInteger secondPlacePendingRewards = new AtomicInteger();
        AtomicInteger thirdPlacePendingRewards = new AtomicInteger();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            firstPlacePendingRewards.set(plugin.getDatabaseManager().getPendingRewardAmount(player.getUniqueId(), 1));
            secondPlacePendingRewards.set(plugin.getDatabaseManager().getPendingRewardAmount(player.getUniqueId(), 2));
            thirdPlacePendingRewards.set(plugin.getDatabaseManager().getPendingRewardAmount(player.getUniqueId(), 3));
        });

        //Displaying the No Pending Rewards Item if there are no rewards.
        if(firstPlacePendingRewards.get() == 0 && secondPlacePendingRewards.get() == 0 && thirdPlacePendingRewards.get() == 0){
            Material noPendingRewardsMat = Material.valueOf(config.getString("reward-system.playtime-rewards-menu.no-pending-rewards-item.material", "BARRIER").toUpperCase());
            String displayName = config.getString("reward-system.playtime-rewards-menu.no-pending-rewards-item.display-name", "&cYou have no pending rewards!");
            List<String> lore = config.getStringList("reward-system.playtime-rewards-menu.no-pending-rewards.lore");
            ItemStack noPendingRewardsItem = createItem(noPendingRewardsMat, displayName, lore, player, "no-pending-rewards");

            int slot = config.getInt("reward-system.playtime-rewards-menu.no-pending-rewards-item.slot", 22);
            if(slot < 0 || slot > 54) slot = 22;
            GUI.setItem(slot, noPendingRewardsItem);
        }
        //Displaying the Pending Rewards Items if there are rewards.
        else{
            int savingSlot;

            List<String> rawLore = config.getStringList("reward-system.rewards-item.playtime-rewards-menu-lore");
            List<Component> lore = new ArrayList<>();
            for(String line : rawLore){
                line = PlaceholderAPI.setPlaceholders(player, line);
                Component coloredLine = LegacyComponentSerializer.legacyAmpersand().deserialize(line);
                lore.add(coloredLine);
            }
            Material rewardItemMat = Material.valueOf(config.getString("reward-system.rewards-item.material", "ENDER_CHEST").toUpperCase());

            ItemStack rewardItem = new ItemStack(rewardItemMat);
            ItemMeta rewardItemMeta = rewardItem.getItemMeta();

            for(int i = 0; i < firstPlacePendingRewards.get(); i++){
                savingSlot = i + 9;

                String rewardDisplayName = config.getString("reward-system.rewards-item.display-name", "%player_tournamentPlace% &a&lReward");
                rewardDisplayName = PlaceholderAPI.setPlaceholders(player, rewardDisplayName);
                Component rewardDisplayNameComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(rewardDisplayName)
                        .replaceText(TextReplacementConfig.builder().match("%player_tournamentPlace%").replacement(plugin.getGivingRewardsSystem().getFormattedPlacementString(1)).build())
                        ;
                rewardItemMeta.displayName(rewardDisplayNameComponent);
                rewardItemMeta.lore(lore);

                //Adding a persistent data to the reward item.
                rewardItemMeta.getPersistentDataContainer().set(dataContainer, PersistentDataType.STRING, "first-place");

                rewardItem.setItemMeta(rewardItemMeta);
                GUI.setItem(savingSlot, rewardItem);
            }

            for(int i = 0; i < secondPlacePendingRewards.get(); i++){
                savingSlot = firstPlacePendingRewards.get() + i + 9;

                String rewardDisplayName = config.getString("reward-system.rewards-item.display-name", "%player_tournamentPlace% &a&lReward");
                rewardDisplayName = PlaceholderAPI.setPlaceholders(player, rewardDisplayName);
                Component rewardDisplayNameComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(rewardDisplayName)
                        .replaceText(TextReplacementConfig.builder().match("%player_tournamentPlace%").replacement(plugin.getGivingRewardsSystem().getFormattedPlacementString(2)).build())
                        ;
                rewardItemMeta.displayName(rewardDisplayNameComponent);
                rewardItemMeta.lore(lore);

                //Adding a persistent data to the reward item.
                rewardItemMeta.getPersistentDataContainer().set(dataContainer, PersistentDataType.STRING, "second-place");

                rewardItem.setItemMeta(rewardItemMeta);
                GUI.setItem(savingSlot, rewardItem);
            }

            for(int i = 0; i < thirdPlacePendingRewards.get(); i++){
                savingSlot = firstPlacePendingRewards.get() + secondPlacePendingRewards.get() + i + 9;

                String rewardDisplayName = config.getString("reward-system.rewards-item.display-name", "%player_tournamentPlace% &a&lReward");
                rewardDisplayName = PlaceholderAPI.setPlaceholders(player, rewardDisplayName);
                Component rewardDisplayNameComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(rewardDisplayName)
                        .replaceText(TextReplacementConfig.builder().match("%player_tournamentPlace%").replacement(plugin.getGivingRewardsSystem().getFormattedPlacementString(3)).build())
                        ;
                rewardItemMeta.displayName(rewardDisplayNameComponent);
                rewardItemMeta.lore(lore);

                //Adding a persistent data to the reward item.
                rewardItemMeta.getPersistentDataContainer().set(dataContainer, PersistentDataType.STRING, "third-place");

                rewardItem.setItemMeta(rewardItemMeta);
                GUI.setItem(savingSlot, rewardItem);
            }
        }

        player.openInventory(GUI);
    }

    private ItemStack createItem(Material material, String displayName, List<String> rawLore, Player player, String itemData){
        ItemStack newItem = new ItemStack(material);
        ItemMeta itemMeta = newItem.getItemMeta();

        displayName = PlaceholderAPI.setPlaceholders(player, displayName);
        itemMeta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(displayName));

        if(rawLore != null){
            List<Component> lore = new ArrayList<>();
            for(String line : rawLore){
                line = PlaceholderAPI.setPlaceholders(player, line);
                lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize(line));
            }
            itemMeta.lore(lore);
        }

        if(itemData != null) itemMeta.getPersistentDataContainer().set(dataContainer, PersistentDataType.STRING, itemData);

        newItem.setItemMeta(itemMeta);
        return newItem;
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event){
        FileConfiguration config = plugin.getConfig();
        Component title = Component.text(config.getString("reward-system.playtime-rewards-menu.title", "Your Playtime Rewards"));

        if(!event.getView().title().equals(title)) return;
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if(clickedItem == null) return;

        ItemMeta clickedMeta = clickedItem.getItemMeta();
        if(clickedMeta == null) return;

        String clickedData = clickedMeta.getPersistentDataContainer().get(dataContainer, PersistentDataType.STRING);
        if(clickedData == null) return;

        Player player = (Player) event.getWhoClicked();

        switch(clickedData){
            //If the player clicks on the 'Return' Button
            case "return" -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                player.closeInventory();
            }

            //If the player clicks on a 'Reward' Item.
            case "first-place", "second-place", "third-place" -> {
                Material rewardItemMat = Material.valueOf(config.getString("reward-system.rewards-item.material", "ENDER_CHEST").toUpperCase());
                ItemStack rewardItem = new ItemStack(rewardItemMat);
                ItemMeta rewardItemMeta = rewardItem.getItemMeta();

                String displayName = config.getString("reward-system.rewards-item.display-name", "%player_tournamentPlace% &a&lReward");
                displayName = PlaceholderAPI.setPlaceholders(player, displayName);
                Component rewardDisplayNameComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(displayName)
                        .replaceText(TextReplacementConfig.builder().match("%player_tournamentPlace%").replacement(plugin.getGivingRewardsSystem().getFormattedPlacementString(Integer.parseInt(clickedData.replace("first-place", "1")))).build())
                        ;
                rewardItemMeta.displayName(rewardDisplayNameComponent);

                List<String> rawLore = config.getStringList("reward-system.rewards-item.lore");
                List<Component> lore = new ArrayList<>();
                for(String line : rawLore){
                    line = PlaceholderAPI.setPlaceholders(player, line);
                    Component coloredLine = LegacyComponentSerializer.legacyAmpersand().deserialize(line);
                    lore.add(coloredLine);
                }
                rewardItemMeta.lore(lore);

                rewardItemMeta.getPersistentDataContainer().set(dataContainer, PersistentDataType.STRING, clickedData);

                rewardItem.setItemMeta(rewardItemMeta);
                HashMap<Integer,ItemStack> addItem = player.getInventory().addItem(rewardItem);
                player.closeInventory();
                if(!addItem.isEmpty()){
                    String noInvSpaceMessage = plugin.getConfig().getString("reward-system.no-inventory-space-message", "&cYou do not have enough inventory space to get your reward!");
                    noInvSpaceMessage = PlaceholderAPI.setPlaceholders(player, noInvSpaceMessage);
                    Component noInvSpaceComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(noInvSpaceMessage);
                    player.sendMessage(noInvSpaceComponent);
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);

                    Bukkit.getScheduler().runTaskLater(plugin, () -> openGUI(player), 20);
                }
                else{
                    String rewardReceivedMessage = plugin.getConfig().getString("reward-system.reward-item-received-message", "&aYou have received your reward! &lClick &aor &lRight Click &ait to open.");
                    rewardReceivedMessage = PlaceholderAPI.setPlaceholders(player, rewardReceivedMessage);
                    Component rewardReceivedComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(rewardReceivedMessage);
                    player.sendMessage(rewardReceivedComponent);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.4f);
                }
            }
        }
    }
}
