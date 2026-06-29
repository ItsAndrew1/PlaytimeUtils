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
        if(command.getName().equalsIgnoreCase("ptutils")){
            if(args.length == 0) return List.of("reload", "rewards");
            if(args[0].equals("rewards") && args.length == 1) return List.of("tournament", "add", "remove");
            if(args[1].equals("tournament") && args.length == 2) return List.of("settimer", "enable", "disable");
        }

        return List.of();
    }
}
