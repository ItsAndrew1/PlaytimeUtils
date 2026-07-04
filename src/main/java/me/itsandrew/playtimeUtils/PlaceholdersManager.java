package me.itsandrew.playtimeUtils;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlaceholdersManager {
    //Caching all the placeholders which communicate with the Database.
    private String cachedTop1TournamentPlaytime = "Loading...";
    private String cachedTop2TournamentPlaytime = "Loading...";
    private String cachedTop3TournamentPlaytime = "Loading...";

    private String cachedTop1Playtime = "Loading...";
    private String cachedTop2Playtime = "Loading...";
    private String cachedTop3Playtime = "Loading...";

    private String cachedTop1TournamentIGN = "Loading...";
    private String cachedTop2TournamentIGN = "Loading...";
    private String cachedTop3TournamentIGN = "Loading...";

    private String cachedTop1IGN = "Loading...";
    private String cachedTop2IGN = "Loading...";
    private String cachedTop3IGN = "Loading...";

    private final ConcurrentHashMap<UUID, String> mainPlaytimeCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, String> tournamentPlaytimeCache = new ConcurrentHashMap<>();

    //Adders for Maps
    public void addTournamentPlaytimeCache(UUID playerUUID, String playtime){
        tournamentPlaytimeCache.put(playerUUID, playtime);
    }
    public void addToMainPlaytimeCache(UUID playerUUID, String playtime){
        mainPlaytimeCache.put(playerUUID, playtime);
    }

    //Removers for Maps
    public void removeFromTournamentPlaytimeCache(UUID playerUUID){
        tournamentPlaytimeCache.remove(playerUUID);
    }
    public void removeFromMainPlaytimeCache(UUID playerUUID){
        mainPlaytimeCache.remove(playerUUID);
    }

    //Getters for Maps
    public String getTournamentPlaytime(UUID playerUUID){
        return tournamentPlaytimeCache.getOrDefault(playerUUID, " ");
    }
    public String getMainPlaytime(UUID playerUUID){
        return mainPlaytimeCache.getOrDefault(playerUUID, " ");
    }

    //Getters for the cached placeholders
    public String getCachedTop1TournamentPlaytime() {
        return cachedTop1TournamentPlaytime;
    }
    public String getCachedTop2TournamentPlaytime() {
        return cachedTop2TournamentPlaytime;
    }
    public String getCachedTop3TournamentPlaytime() {
        return cachedTop3TournamentPlaytime;
    }
    public String getCachedTop1Playtime() {
        return cachedTop1Playtime;
    }
    public String getCachedTop2Playtime() {
        return cachedTop2Playtime;
    }
    public String getCachedTop3Playtime() {
        return cachedTop3Playtime;
    }
    public String getCachedTop1TournamentIGN() {
        return cachedTop1TournamentIGN;
    }
    public String getCachedTop2TournamentIGN() {
        return cachedTop2TournamentIGN;
    }
    public String getCachedTop3TournamentIGN() {
        return cachedTop3TournamentIGN;
    }
    public String getCachedTop1IGN() {
        return cachedTop1IGN;
    }
    public String getCachedTop2IGN() {
        return cachedTop2IGN;
    }
    public String getCachedTop3IGN() {
        return cachedTop3IGN;
    }

    //Setters for the cached placeholders
    public void setCachedTop1TournamentPlaytime(String cachedTop1TournamentPlaytime) {
        this.cachedTop1TournamentPlaytime = cachedTop1TournamentPlaytime;
    }
    public void setCachedTop2TournamentPlaytime(String cachedTop2TournamentPlaytime) {
        this.cachedTop2TournamentPlaytime = cachedTop2TournamentPlaytime;
    }
    public void setCachedTop3TournamentPlaytime(String cachedTop3TournamentPlaytime) {
        this.cachedTop3TournamentPlaytime = cachedTop3TournamentPlaytime;
    }
    public void setCachedTop1Playtime(String cachedTop1Playtime) {
        this.cachedTop1Playtime = cachedTop1Playtime;
    }
    public void setCachedTop2Playtime(String cachedTop2Playtime) {
        this.cachedTop2Playtime = cachedTop2Playtime;
    }
    public void setCachedTop3Playtime(String cachedTop3Playtime) {
        this.cachedTop3Playtime = cachedTop3Playtime;
    }
    public void setCachedTop1TournamentIGN(String cachedTop1TournamentIGN) {
        this.cachedTop1TournamentIGN = cachedTop1TournamentIGN;
    }
    public void setCachedTop2TournamentIGN(String cachedTop2TournamentIGN) {
        this.cachedTop2TournamentIGN = cachedTop2TournamentIGN;
    }
    public void setCachedTop3TournamentIGN(String cachedTop3TournamentIGN) {
        this.cachedTop3TournamentIGN = cachedTop3TournamentIGN;
    }
    public void setCachedTop1IGN(String cachedTop1IGN) {
        this.cachedTop1IGN = cachedTop1IGN;
    }
    public void setCachedTop2IGN(String cachedTop2IGN) {
        this.cachedTop2IGN = cachedTop2IGN;
    }
    public void setCachedTop3IGN(String cachedTop3IGN) {
        this.cachedTop3IGN = cachedTop3IGN;
    }
}
