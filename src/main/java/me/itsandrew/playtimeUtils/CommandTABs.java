//Developed by _ItsAndrew_
package me.itsandrew.playtimeUtils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandTABs implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(command.getName().equalsIgnoreCase("myplaytime")) return List.of("main", "tournament", "rewards");
        if(command.getName().equalsIgnoreCase("topplaytime") || command.getName().equalsIgnoreCase("playtime")) return List.of("main", "tournament");
        if(command.getName().equalsIgnoreCase("ptutils")){
            if(args.length == 1) return List.of("reload", "rewards");
            if(args.length == 2 && args[0].equals("rewards")) return List.of("tournament", "add", "remove");
            if(args.length == 3 && args[1].equals("tournament")) return List.of("settimer", "setbook", "enable", "disable");
        }

        return List.of();
    }
}
