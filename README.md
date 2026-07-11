<img src = "https://img.shields.io/github/v/release/ItsAndrew1/PlaytimeUtils?display_name=tag&style=flat&label=Plugin%20Version" alt="plugin version"> 
<img src = "https://img.shields.io/badge/Minecraft%20Version-1.21%2B-green?style=flat" alt="MC Version">
<img src = "https://img.shields.io/modrinth/dt/MJmnDxVw?label=Modrinth%20Downloads&color=%2314e025" alt = "Modrinth Downloads">
<img src = "https://img.shields.io/spiget/downloads/136465?style=flat&label=Spigot%20Downloads" alt="Spigot Downloads">

# PlaytimeUtils 

**PlaytimeUtils** is a powerful, lightweight **playtime tracking plugin** for Minecraft servers, designed to effortlessly monitor player activity.
Gather engagement with a **Custom Playtime Tournament**, apply **Custom Prefixes** and more today!

## 💎 Features
* **⚡ Optimized AFK Detection:** Super-lightweight movement tracking to keep server performance clean.
* **💾 Dual Database Support:** Works seamlessly with local **SQLite** (`.db`) or remote **MySQL** networks.
* **🏷️ Dynamic AFK Prefix:** Automatically assign custom prefixes to inactive players.
* **🎉 First-Join Celebrations:** Fully configurable titles, subtitles, and sounds for new players.
* **🏆 Playtime Tournament:** Easily reward the players who played the most!
* **🔧 Simple Configuration:** Via *Interactive GUIs* and *config.yml* file.

And much more! See **config.yml** for all the features.

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

## 📜 Commands

| Command                                | Description                                              | Permission                         |
|----------------------------------------|----------------------------------------------------------|------------------------------------|
| `/myplaytime <main/tournament>`        | Shows your **main/tournament playtime** in chat          | `playtimeutils.myplaytime...`      |
 | `/myplaytime rewards`                  | Opens up the **Playtime Rewards** Menu                   | `playtimeutils.myplaytime.rewards` |
| `/playtime <player> <main/tournament>` | Shows the **main/tournament playtime** of another player | `playtimeutils.playtime...`        |
| `/topplaytime <main/tournament>`       | Shows the **main/tournament playtime** top 3 leaderboard | `playtimeutils.topplaytime...`     |
| `/putils ...`                          | Handles the main configuration of the plugin             | `playtimeutils.ptutils...`         |

## 📜 Permissions
| Permission                                  | Description                                                       | 
|---------------------------------------------|-------------------------------------------------------------------|
| `playtimeutils.myplaytime`                  | Gives access to **/myplaytime** command                           |
| `playtimeutils.myplaytime.main/tournament`  | Lets the player see his own **main/tournament** playtime          |
 | `playtimeutils.myplaytime.rewards`          | Lets the player access the **Playtime Rewards** Menu              |
| `playtimeutils.playtime`                    | Gives access to the **/playtime \<player>** command               |
 | `playtimeutils.playtime.main/tournament`    | Lets the player see another player's **main/tournament** playtime |
| `playtimeutils.topplaytime`                 | Gives access to the **/topplaytime** command                      | 
 | `playtimeutils.topplaytime.main/tournament` | Lets the player see the **main/tournament** playtime top          |
| `playtimeutils.ptutils`                     | Gives access to the **main configuration command**                |
 | `playtimeutils.ptutils.reload`              | Lets the player reload the **config.yml** file                    |
 | `playtimeutils.ptutils.rewards...`          | Allows the player to manage the **Rewards System**                |

## ❤️ Credits
**PlaytimeUtils** was built and tested by *\_ItsAndrew_*.  
Special thanks to people who *give feedback*.  
My discord: **\_itsandrew_**
