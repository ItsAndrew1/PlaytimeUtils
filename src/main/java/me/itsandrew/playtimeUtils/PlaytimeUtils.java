//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import me.itsandrew.playtimeUtils.RewardSystem.ConfigGUIs.AddRewardsGUI;
import me.itsandrew.playtimeUtils.RewardSystem.ConfigGUIs.ChoosePlaceGUI;
import me.itsandrew.playtimeUtils.RewardSystem.ConfigGUIs.ItemsOrExpGUI;
import me.itsandrew.playtimeUtils.RewardSystem.ConfigGUIs.RemoveRewardsGUIs;
import me.itsandrew.playtimeUtils.RewardSystem.GivingRewards;
import me.itsandrew.playtimeUtils.RewardSystem.PlaytimeRewardsGUI;
import me.itsandrew.playtimeUtils.RewardSystem.States.StaffState;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.node.types.PrefixNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
import org.bukkit.scheduler.BukkitTask;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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
    private final RemoveRewardsGUIs removeRewardsGUIs = new RemoveRewardsGUIs(this);
    private final GivingRewards givingRewardsSystem = new GivingRewards(this);

    private final PlaceholdersManager placeholdersManager = new PlaceholdersManager();
    private final PlaytimeRewardsGUI playtimeRewardsGUI = new PlaytimeRewardsGUI(this);

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

        getCommand("myplaytime").setTabCompleter(new CommandTABs());
        getCommand("ptutils").setTabCompleter(new CommandTABs());
        getCommand("topplaytime").setTabCompleter(new CommandTABs());
        getCommand("playtime").setTabCompleter(new CommandTABs());

        //Registering events.
        getServer().getPluginManager().registerEvents(new PlayerJoin(this), this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(itemsOrExpGUI, this);
        getServer().getPluginManager().registerEvents(choosePlaceGUI, this);
        getServer().getPluginManager().registerEvents(addRewardsGUI, this);
        getServer().getPluginManager().registerEvents(removeRewardsGUIs, this);
        getServer().getPluginManager().registerEvents(playtimeRewardsGUI, this);
        getServer().getPluginManager().registerEvents(givingRewardsSystem, this);

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
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) new PluginPapiPlaceholders(this).register();
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

        //Task for updating the placeholders. Runs every 10 seconds Async.
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            List<Map.Entry<UUID, Integer>> top3Players = getDatabaseManager().getTournamentTop3Players();
            List<Map.Entry<UUID, Integer>> mainTop3Players = getDatabaseManager().getMainTop3Players();

            //Resetting defaults to prevent old data sticking around if list changes.
            getPlaceholdersManager().setCachedTop1TournamentPlaytime("0m");
            getPlaceholdersManager().setCachedTop2TournamentPlaytime("0m");
            getPlaceholdersManager().setCachedTop3TournamentPlaytime("0m");
            getPlaceholdersManager().setCachedTop1TournamentIGN("None");
            getPlaceholdersManager().setCachedTop2TournamentIGN("None");
            getPlaceholdersManager().setCachedTop3TournamentIGN("None");

            //Saving the Tournament placeholders
            boolean toggleRewardSystem = getConfig().getBoolean("reward-system.toggle", false);
            long tournamentDuration = getDatabaseManager().getTournamentTimestamp("duration");
            if (toggleRewardSystem && top3Players != null && !top3Players.isEmpty() && tournamentDuration != 0) {
                UUID uuid1 = top3Players.getFirst().getKey();
                OfflinePlayer top1 = Bukkit.getOfflinePlayer(uuid1);
                String name1 = top1.getName() != null ? top1.getName() : "Unknown";
                getPlaceholdersManager().setCachedTop1TournamentPlaytime(getDatabaseManager().getTournamentPlaytimeString(uuid1));
                getPlaceholdersManager().setCachedTop1TournamentIGN(name1);

                if (top3Players.size() > 1) {
                    UUID uuid2 = top3Players.get(1).getKey();
                    OfflinePlayer top2 = Bukkit.getOfflinePlayer(uuid2);
                    String name2 = top2.getName() != null ? top2.getName() : "Unknown";
                    getPlaceholdersManager().setCachedTop2TournamentPlaytime(getDatabaseManager().getTournamentPlaytimeString(uuid2));
                    getPlaceholdersManager().setCachedTop2TournamentIGN(name2);
                }

                if (top3Players.size() > 2) {
                    UUID uuid3 = top3Players.get(2).getKey();
                    OfflinePlayer top3 = Bukkit.getOfflinePlayer(uuid3);
                    String name3 = top3.getName() != null ? top3.getName() : "Unknown";
                    getPlaceholdersManager().setCachedTop3TournamentPlaytime(getDatabaseManager().getTournamentPlaytimeString(uuid3));
                    getPlaceholdersManager().setCachedTop3TournamentIGN(name3);
                }
            }

            //Resetting Main Playtime defaults
            getPlaceholdersManager().setCachedTop1IGN("None");
            getPlaceholdersManager().setCachedTop2IGN("None");
            getPlaceholdersManager().setCachedTop3IGN("None");
            getPlaceholdersManager().setCachedTop1Playtime("0m");
            getPlaceholdersManager().setCachedTop2Playtime("0m");
            getPlaceholdersManager().setCachedTop3Playtime("0m");

            //Saving the Main Playtime Placeholders
            if (mainTop3Players != null && !mainTop3Players.isEmpty()) {
                UUID uuid1 = mainTop3Players.getFirst().getKey();
                OfflinePlayer top1 = Bukkit.getOfflinePlayer(uuid1);
                String name1 = top1.getName() != null ? top1.getName() : "Unknown";
                getPlaceholdersManager().setCachedTop1Playtime(getDatabaseManager().getMainPlaytimeString(uuid1));
                getPlaceholdersManager().setCachedTop1IGN(name1);

                if (mainTop3Players.size() > 1) {
                    UUID uuid2 = mainTop3Players.get(1).getKey();
                    OfflinePlayer top2 = Bukkit.getOfflinePlayer(uuid2);
                    String name2 = top2.getName() != null ? top2.getName() : "Unknown";
                    getPlaceholdersManager().setCachedTop2Playtime(getDatabaseManager().getMainPlaytimeString(uuid2));
                    getPlaceholdersManager().setCachedTop2IGN(name2);
                }

                if (mainTop3Players.size() > 2) {
                    UUID uuid3 = mainTop3Players.get(2).getKey();
                    OfflinePlayer top3 = Bukkit.getOfflinePlayer(uuid3);
                    String name3 = top3.getName() != null ? top3.getName() : "Unknown";
                    getPlaceholdersManager().setCachedTop3Playtime(getDatabaseManager().getMainPlaytimeString(uuid3));
                    getPlaceholdersManager().setCachedTop3IGN(name3);
                }
            }

            //Saving the main Playtime
            List<UUID> onlinePlayerUUIDs = Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).toList();
            for(UUID uuid : onlinePlayerUUIDs) getPlaceholdersManager().addToMainPlaytimeCache(uuid, getDatabaseManager().getMainPlaytimeString(uuid));

            //Saving the Tournament Playtime
            if(toggleRewardSystem && tournamentDuration != 0){
                for(UUID uuid : onlinePlayerUUIDs) getPlaceholdersManager().addTournamentPlaytimeCache(uuid, getDatabaseManager().getTournamentPlaytimeString(uuid));
            }
        }, 0, 200);
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
            if(toggleRewardSystem) databaseManager.updatePlayerTournamentPlaytime(playerUUID, playtimeMap.get(playerUUID));
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
            if(toggleRewardSystem) databaseManager.updatePlayerTournamentPlaytime(playerUUID, currentPlaytime);
        });

        //Removing the player from the Maps
        playtimeMap.remove(player.getUniqueId());
        afkMap.remove(player.getUniqueId());
        lastActivity.remove(player.getUniqueId());

        //Removing the player from StaffState Map if he has an ongoing staff state
        staffStates.remove(player);

        //Removing the player from the PlaceholdersManager maps
        getPlaceholdersManager().removeFromMainPlaytimeCache(playerUUID);
        getPlaceholdersManager().removeFromTournamentPlaytimeCache(playerUUID);

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

    //Helper functions for formatting the dates/times
    public String formatDate(long millis){
        Instant instant = Instant.ofEpochMilli(millis);
        LocalDateTime time = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        return time.format(formatter);
    }
    public String formatTime(long millis){
        long days =  TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);

        long hours =  TimeUnit.MILLISECONDS.toHours(millis);
        millis -=  TimeUnit.HOURS.toMillis(hours);

        long minutes =  TimeUnit.MILLISECONDS.toMinutes(millis);

        StringBuilder sb = new StringBuilder();

        if(days > 0) sb.append(days).append("d ");
        if(hours > 0) sb.append(hours).append("h ");
        if(minutes > 0) sb.append(minutes).append("m ");
        if(sb.isEmpty()) sb.append(0).append("m ");

        return sb.toString().trim();
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
    public RemoveRewardsGUIs getRemoveRewardsGUIs() {
        return removeRewardsGUIs;
    }
    public GivingRewards getGivingRewardsSystem() {
        return givingRewardsSystem;
    }
    public PlaceholdersManager getPlaceholdersManager() {
        return placeholdersManager;
    }
    public PlaytimeRewardsGUI getPlaytimeRewardsGUI() {
        return playtimeRewardsGUI;
    }
}
