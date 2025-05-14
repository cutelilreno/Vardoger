/**
 * MIT License
 * Copyright (c) 2025 cutelilreno
 * https://opensource.org/licenses/MIT
 */
package moe.reno.vardoger.commands;

import moe.reno.vardoger.Vardoger;
import moe.reno.vardoger.conf.GroupManager;
import moe.reno.vardoger.util.MessageUtil;
import moe.reno.vardoger.conf.ConfigManager;
import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.AStringArgument;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.io.IOException;

@Command("vardoger")
@Alias("vg")
public class VardogerCommand {
    private final static Vardoger plugin = Vardoger.getInstance();
    private static ConfigManager config = plugin.getConfigManager();
    private static GroupManager groupManager = plugin.getGroupManager();
    private static MessageUtil msg = new MessageUtil();

    @Default
    public static void vg(CommandSender sender) {
        msg.sendMessage(sender, "--- <gold>Vardoger help<reset> ---");
        msg.info(sender, "/vg - Show this help");
        msg.info(sender, "/vg reload - Reloads the config");
        msg.info(sender, "/vg addgroup <group> - Creates a group");
        msg.info(sender, "/vg addsign <id> <group> - Adds a sign to a group");
        msg.sendMessage(sender, "---------------------");
    }

    // TODO: Implement reload command
    /*
    @Subcommand("reload")
    @Permission("vardoger.reload")
    public static void onReload(CommandSender sender) {
        plugin.reloadConfig();
        try {
            groupManager.reload();
            msg.success(sender, "Reloaded group config.");
        } catch (IOException e) {
            msg.error(sender, "Failed to reload group config.");
            e.printStackTrace();
        }
    }*/

    @Subcommand("addgroup")
    @Permission("vardoger.addgroup")
    public static void onAddGroup(CommandSender sender, @AStringArgument String name) {
        try {
            groupManager.addGroup(name);
            msg.success(sender, "Group '" + name + "' added.");
        } catch (IOException e) {
            msg.error(sender, "Failed to create group");
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
            msg.error(player, "No such group.");
            return;
        }

        var block = player.getTargetBlockExact(config.getViewDistance());
        if (block == null) {
            msg.error(player, "No block in range.");
            return;
        }

        if (!config.getValidBlocks().contains(block.getType())) {
            msg.error(player, "Block type " + block.getType() + " not allowed.");
            return;
        }

        try {
            groupManager.addSign(groupName, id, block.getLocation());
            msg.success(player, "Sign '" + id + "' added to '" + groupName + "'.");
            msg.info(player, "This will be tracked after the next restart.");
        } catch (IOException e) {
            msg.error(player, "Failed to save sign to disk.");
            e.printStackTrace();
        }
    }
}