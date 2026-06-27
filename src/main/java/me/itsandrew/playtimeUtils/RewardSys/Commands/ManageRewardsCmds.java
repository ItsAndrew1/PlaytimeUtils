package me.itsandrew.playtimeUtils.RewardSys.Commands;

import me.itsandrew.playtimeUtils.PlaytimeUtils;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class ManageRewardsCmds implements CommandExecutor {
    private final PlaytimeUtils plugin;

    public ManageRewardsCmds(PlaytimeUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        final FileConfiguration config = plugin.getConfig();
        final Component noPermMsg = MiniMessage.miniMessage().deserialize("<red>You don't have permission to do that!");
        final Sound errorSound = Sound.sound().type(NamespacedKey.minecraft("entity_enderman_teleport")).build();

        //Checking if the sender has the necessary permission
        if(!sender.hasPermission("playtimeutils.ptutils")){
            sender.sendMessage(noPermMsg);
            sender.playSound(errorSound);
            return true;
        }

        if(command.getName().equalsIgnoreCase("ptutils")){

        }

        return false;
    }
}
