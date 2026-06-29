//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import me.itsandrew.playtimeUtils.RewardSystem.GUIs.AddRewardsGUI;
import me.itsandrew.playtimeUtils.RewardSystem.GUIs.ChoosePlaceGUI;
import me.itsandrew.playtimeUtils.RewardSystem.GUIs.ItemsOrExpGUI;
import me.itsandrew.playtimeUtils.RewardSystem.GUIs.RemoveRewardsGUI;
import me.itsandrew.playtimeUtils.RewardSystem.States.StaffState;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.node.types.PrefixNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

//Main plugin class.
public final class PlaytimeUtils extends JavaPlugin implements Listener {
    private DbManager databaseManager;

    private final Map<UUID, Integer> playtimeMap = new HashMap<>();
    private final Map<UUID, Long> lastActivity = new HashMap<>();
    private final Map<UUID, Boolean> afkMap = new HashMap<>();
    private LuckPerms luckpermsAPI;
    private final Map<UUID, Consumer<Component>> chatInputMap = new HashMap<>();

    private final Map<Player, StaffState> staffStates = new HashMap<>();
    private final ItemsOrExpGUI itemsOrExpGUI = new ItemsOrExpGUI(this);
    private final ChoosePlaceGUI choosePlaceGUI = new ChoosePlaceGUI(this);
    private final AddRewardsGUI addRewardsGUI = new AddRewardsGUI(this);
    private final RemoveRewardsGUI removeRewardsGUI = new RemoveRewardsGUI(this);

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        //Creating the necessary objects.
        databaseManager = new DbManager(this);

        //Registering commands and the TABs.
        getCommand("myplaytime").setExecutor(new CommandManager(this));
        getCommand("playtime").setExecutor(new CommandManager(this));
        getCommand("topplaytime").setExecutor(new CommandManager(this));
        getCommand("ptutils").setExecutor(new CommandManager(this));
        getCommand("ptutils").setTabCompleter(new CommandTABs());

        //Registering events.
        getServer().getPluginManager().registerEvents(new PlayerJoin(this), this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(itemsOrExpGUI, this);
        getServer().getPluginManager().registerEvents(choosePlaceGUI, this);
        getServer().getPluginManager().registerEvents(addRewardsGUI, this);
        getServer().getPluginManager().registerEvents(removeRewardsGUI, this);

        //Connecting the database
        try{
            if(!databaseManager.connectDb()){
                getLogger().severe("[PlaytimeUtils] Failed to connect to the database. Shutting down the plugin.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        } catch (Exception e){
            getLogger().severe("[PlaytimeUtils] Failed to connect to the database. Shutting down the plugin. See message below for more details: ");
            getLogger().severe("[PlaytimeUtils] " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        //Connecting the LuckPerms API (if LuckPerms plugin exists)
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if(provider != null && getServer().getPluginManager().isPluginEnabled("LuckPerms")) luckpermsAPI = provider.getProvider();

        //Enabling the PlaytimeUtils Placeholders Extension
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) new PlaytimePlaceholder(this).register();
        else getLogger().warning("[PlaytimeUtils] PlaceholderAPI is not installed. Placeholders won't work.");

        getLogger().info("[PlaytimeUtils] Plugin enabled successfully.");

        //Starting the task to track the playtime of players
        getServer().getScheduler().runTaskTimer(this, () -> {
            for(Player player : Bukkit.getOnlinePlayers()){
                //Skipping if the player is already AFK
                if(afkMap.containsKey(player.getUniqueId())) continue;

                //Checking if the player is now AFK (4 secs)
                if(isPlayerAFK(player.getUniqueId())){
                    String chatMessage = getConfig().getString("messages.player-afk", "&7You are now AFK!");
                    chatMessage = PlaceholderAPI.setPlaceholders(player, chatMessage);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatMessage));

                    //Putting the player in an "AFK" group (if luckperms is enabled)
                    if (luckpermsAPI != null){
                        luckpermsAPI.getUserManager().modifyUser(player.getUniqueId(), user -> {
                            //Setting the AFK prefix to the player
                            try{
                                String prefixString = getConfig().getString("lp-afk-prefix", "&7&l[AFK] &7");
                                PrefixNode prefix = PrefixNode.builder(prefixString, 100000).build();
                                user.data().add(prefix);
                                luckpermsAPI.getUserManager().saveUser(user);
                            } catch (Exception e){
                                getLogger().severe("[PlaytimeUtils] There was an error assigning the AFK Prefix. See message below:");
                                getLogger().severe("[PlaytimeUtils] " + e.getMessage());
                            }
                        });
                    }

                    afkMap.put(player.getUniqueId(), true);
                    continue;
                }

                playtimeMap.compute(player.getUniqueId(), (k, playtime) -> playtime + 1);
            }
        }, 0, 20);
    }

    private void removeAfkPrefixNodeFromPlayer(Player player){
        if(luckpermsAPI == null) return;

        luckpermsAPI.getUserManager().modifyUser(player.getUniqueId(), user -> {
            String prefix = getConfig().getString("lp-afk-prefix", "&7&l[AFK] &7");
            PrefixNode prefixNode = PrefixNode.builder(prefix, 100000).build();
            user.data().remove(prefixNode);
            luckpermsAPI.getUserManager().saveUser(user);
        });
    }

    @Override
    public void onDisable() {
        saveConfig();

        //Saving the playtime of all players to the database
        for(UUID playerUUID : playtimeMap.keySet()){
            databaseManager.updatePlayerMainPlaytime(playerUUID, playtimeMap.get(playerUUID));
            boolean toggleRewardSystem = getConfig().getBoolean("reward-system.toggle", true);
            if(toggleRewardSystem) databaseManager.updatePlayerRewardPlaytime(playerUUID, playtimeMap.get(playerUUID));
        }

        getLogger().info("[PlaytimeUtils] Plugin disabled successfully.");
    }

    private boolean isPlayerAFK(UUID playerUUID){
        int afkSeconds = getConfig().getInt("afk-seconds", 4);
        return System.currentTimeMillis() - lastActivity.get(playerUUID) > afkSeconds * 1000L;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        //Checking if the player moves (with WASD SPACE etc.).
        if(event.getFrom().toVector().distanceSquared(event.getTo().toVector()) > 0.03){
            //Checking if the player is already AFK
            if(isPlayerAFK(event.getPlayer().getUniqueId())){
                String message = getConfig().getString("messages.player-no-more-afk", "&7You are not AFK anymore.");
                message = PlaceholderAPI.setPlaceholders(event.getPlayer(), message);
                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', message));

                afkMap.remove(event.getPlayer().getUniqueId());

                //Removing the AFK group from the player
                removeAfkPrefixNodeFromPlayer(event.getPlayer());
            }

            lastActivity.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
    }

    //Building the functions to handle chat input from the player when needed.
    @EventHandler
    public void onPlayerChat(AsyncChatEvent event){
        if(!chatInputMap.containsKey(event.getPlayer().getUniqueId())) return;
        event.setCancelled(true);

        Component message = event.message();
        Consumer<Component> callback = chatInputMap.remove(event.getPlayer().getUniqueId());
        Bukkit.getScheduler().runTask(this, () -> callback.accept(message));
    }
    public void playerInput(Player player, Consumer<Component> callback){
        chatInputMap.put(player.getUniqueId(), callback);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();

        //Saving the playtime in the database
        UUID playerUUID = player.getUniqueId();
        boolean toggleRewardSystem = getConfig().getBoolean("reward-system.toggle", true);
        int currentPlaytime = playtimeMap.get(playerUUID);
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            databaseManager.updatePlayerMainPlaytime(playerUUID, currentPlaytime);
            if(toggleRewardSystem) databaseManager.updatePlayerRewardPlaytime(playerUUID, currentPlaytime);
        });

        //Removing the player from the Maps
        playtimeMap.remove(player.getUniqueId());
        afkMap.remove(player.getUniqueId());
        lastActivity.remove(player.getUniqueId());

        //Removing the AFK group from the player (if he is afk)
        removeAfkPrefixNodeFromPlayer(player);
    }

    //Sets up the decoration for the GUIs
    public void decorationSetup(Inventory GUI){
        for(int i = 0; i < 9; i++) {
            ItemStack decoGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta decoGlassMeta = decoGlass.getItemMeta();
            if(decoGlassMeta != null) decoGlassMeta.displayName(Component.text(" "));
            decoGlass.setItemMeta(decoGlassMeta);
            GUI.setItem(i, decoGlass);
        }
        for(int i = 45; i < 54; i++) {
            ItemStack decoGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta decoGlassMeta = decoGlass.getItemMeta();
            if(decoGlassMeta != null) decoGlassMeta.displayName(Component.text(" "));
            decoGlass.setItemMeta(decoGlassMeta);
        }
    }

    //Getters
    public DbManager getDatabaseManager() {
        return databaseManager;
    }
    public Map<UUID, Integer> getPlaytimeMap() {
        return playtimeMap;
    }
    public Map<UUID, Long> getLastActivity() {
        return lastActivity;
    }
    public Map<Player, StaffState> getStaffStates() {
        return staffStates;
    }
    public ItemsOrExpGUI getItemsOrExpGUI() {
        return itemsOrExpGUI;
    }
    public ChoosePlaceGUI getChoosePlaceGUI() {
        return choosePlaceGUI;
    }
    public AddRewardsGUI getAddRewardsGUI() {
        return addRewardsGUI;
    }
    public RemoveRewardsGUI getRemoveRewardsGUI() {
        return removeRewardsGUI;
    }
}
