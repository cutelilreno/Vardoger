/**
 * MIT License
 * Copyright (c) 2025 cutelilreno
 * https://opensource.org/licenses/MIT
 */
package moe.reno.vardoger.util;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles sending messages to players and console
 */
public class MessageUtil {
    private final MiniMessage mm;;

    public MessageUtil() {
        this.mm = MiniMessage.miniMessage();
    }

    /**
     * Sends a message to the target CommandSender
     *
     * @param target  The target CommandSender
     * @param message The message to send
     */
    public void sendMessage(CommandSender target, String message) {
        if (message == null || message.isEmpty()) return;
        target.sendMessage(mm.deserialize(message));
    }

    /**
     * Sends a message to the target CommandSender with the prefix
     * "Warning: " in yellow
     * 
     * MiniMessage formatted messages are supported
     *
     * @param target  The target CommandSender
     * @param message The message to send
     */
    public void warn(CommandSender target, String message) {
        if (message == null || message.isEmpty()) return;
        Component warning = Component.text("Warning: ", NamedTextColor.YELLOW);
        target.sendMessage(warning.append(mm.deserialize("<white>" + message)));
    }

    /**
     * Sends a message to the target CommandSender with the prefix
     * "Error: " in red
     * 
     * MiniMessage formatted messages are supported
     *
     * @param target  The target CommandSender
     * @param message The message to send
     */
    public void error(CommandSender target, String message) {
        if (message == null || message.isEmpty()) return;
        Component error = Component.text("Error: ", NamedTextColor.RED);
        target.sendMessage(error.append(mm.deserialize(message)));
    }

    /**
     * Sends a message to the target CommandSender with the prefix
     * "Success: " in green
     * 
     * MiniMessage formatted messages are supported
     *
     * @param target  The target CommandSender
     * @param message The message to send
     */
    public void success(CommandSender target, String message) {
        if (message == null || message.isEmpty()) return;
        Component success = Component.text("Success: ", NamedTextColor.GREEN);
        target.sendMessage(success.append(mm.deserialize("<white>" + message)));
    }

    /**
     * Sends a message to the target CommandSender in gray
     *
     * @param target  The target CommandSender
     * @param message The message to send
     */
    public void info(CommandSender target, String message) {
        if (message == null || message.isEmpty()) return;
        Component info = Component.text(message, NamedTextColor.GRAY);
        target.sendMessage(info);
    }

    /**
     * Sends a message to players with a specific permission
     * 
     * MiniMessage formatted messages are supported
     *
     * @param permission The permission node to check
     * @param miniMsg    The MiniMessage-formatted message to send
     */
    public void sendToPermission(String permission, String miniMsg) {
        if (miniMsg == null || miniMsg.isEmpty() || permission == null || permission.isEmpty()) return;
        Component component = mm.deserialize(miniMsg);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                player.sendMessage(component);
            }
        }
    }

}



