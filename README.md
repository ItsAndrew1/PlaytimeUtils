# PlaytimeUtils 

**PlaytimeUtils** is a powerful, lightweight **playtime tracking plugin** for Minecraft servers, designed to effortlessly monitor player activity. Track engagement with a top 3 leaderboard and manage active players with ease!

## 💎 Features
* **⚡ Optimized AFK Detection:** Super-lightweight movement tracking (`distanceSquared`) to keep server performance pristine.
* **💾 Dual Database Support:** Works seamlessly with local **SQLite** (`.db`) or remote **MySQL** networks.
* **🏆 Top 3 Leaderboard:** Reward and showcase your most dedicated players.
* **🏷️ Dynamic AFK Prefix:** Automatically assign custom prefixes to active/inactive players.
* **🎉 First-Join Celebrations:** Fully configurable titles, subtitles, and sounds for new players.

And much more! See **config.yml** for all the features.

## 📜 Commands

| Command                             | Description | Permission |
|-------------------------------------|--------------|-------------|
| `/myplaytime`              | Shows your playtime in chat. | `playtimeutils.myplaytime` |
| `/playtime <player>`             | Shows the playtime of a player | `playtimeutils.playtime` |
| `/topplaytime`              | Shows the top 3 leaderboard | `playtimeutils.topplaytime` |
| `/putilsreload` | Reloads the **config.yml** file | `playtimeutils.reload` |

## 📜 Permissions
| Permission                            | Description | 
|-------------------------------------|--------------|
| `playtimeutils.myplaytime`              | Gives access to **/myplaytime** command |
| `playtimeutils.playtime`             | Gives access to the **/playtime \<player>** command  |
| `playtimeutils.topplaytime`              | Gives access to the **/topplaytime** command| 
| `playtimeutils.reload` | Gives access to the reload command | 


## 📌 Integrations
**PlaytimeUtils** uses the following dependencies:
### 🧩 _PlaceholderAPI_ (PAPI)
 - Allows the use of **placeholders** in every message of the plugin.
 - Gives access to the **custom playtime placeholder**, which allows you to display the playtime in *scoreboards*, *tab lists*, etc.

### 🍀 _LuckPerms_
 - Needed for the **custom AFK prefix** 

‼️These are all **SOFT-DEPENDENCIES**, meaning you don't need these plugins installed on your server, but it may improve the *functionality* of **PlaytimeUtils**.


## 🐞 Found a Bug?
 - Open an issue [here](https://github.com/ItsAndrew1/PlaytimeUtils/issues)
 - Send me a DM on my discord: **\_itsandrew_**

## ❤️ Credits
**PlaytimeUtils** was built and tested by *\_ItsAndrew_*.  
Special thanks to people who *give feedback*.  
My discord: **\_itsandrew_**
