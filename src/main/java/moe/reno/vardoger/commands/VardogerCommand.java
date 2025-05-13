/**
 * MIT License
 * Copyright (c) 2025 cutelilreno
 * https://opensource.org/licenses/MIT
 */
package moe.reno.vardoger.commands;

import moe.reno.vardoger.Vardoger;
import moe.reno.vardoger.conf.GroupManager;
import moe.reno.vardoger.conf.ConfigManager;

import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.AStringArgument;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.io.IOException;

@Command("vg")
public class VardogerCommand {
    private final static Vardoger plugin = Vardoger.getInstance();
    private static ConfigManager config = plugin.getConfigManager();
    private static GroupManager groupManager = plugin.getGroupManager();

    @Default
    public static void vg(CommandSender sender) {
        sender.sendMessage("--- Vardoger help ---");
        sender.sendMessage("/vg - Show this help");
        sender.sendMessage("/vg reload - Reloads the config");
        sender.sendMessage("/vg addgroup <group> - Creates a group");
        sender.sendMessage("/vg addsign <id> <group> - Adds a sign to a group");
        sender.sendMessage("---------------------");
    }

    @Subcommand("reload")
    @Permission("vardoger.reload")
    public static void onReload(CommandSender sender) {
        plugin.reloadConfig();
        try {
            groupManager.reload();
            sender.sendMessage("§aReloaded vardoger config.");
        } catch (IOException e) {
            sender.sendMessage("§cFailed to reload config!");
            e.printStackTrace();
        }
    }

    @Subcommand("addgroup")
    @Permission("vardoger.addgroup")
    public static void onAddGroup(CommandSender sender, @AStringArgument String name) {
        try {
            groupManager.addGroup(name);
            sender.sendMessage("§aGroup '" + name + "' added.");
        } catch (IOException e) {
            sender.sendMessage("§cFailed to create group: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Subcommand("addsign")
    @Permission("vardoger.addsign")
    public static void onAddSign(Player player,
                        @AStringArgument String id,
                        @AStringArgument String groupName) {
        var group = groupManager.getGroup(groupName);
        if (group == null) {
            player.sendMessage("§cNo such group.");
            return;
        }

        var block = player.getTargetBlockExact(config.getViewDistance());
        if (block == null) {
            player.sendMessage("§cNo block in range.");
            return;
        }

        if (!config.getValidBlocks().contains(block.getType())) {
            player.sendMessage("§cBlock type " + block.getType() + " not allowed.");
            return;
        }

        try {
            groupManager.addSign(groupName, id, block.getLocation());
            player.sendMessage("§aSign '" + id + "' added to '" + groupName + "'.");
            player.sendMessage("§e§oThis will be tracked after the next restart.");
        } catch (IOException e) {
            player.sendMessage("§cFailed to save sign to disk.");
            e.printStackTrace();
        }
    }
}