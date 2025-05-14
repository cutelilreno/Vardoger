package moe.reno.vardoger.commands;

import moe.reno.vardoger.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.regex.Pattern;

public class CommandProcessor {
    // Pattern for internal commands, e.g. @print, @broadcast, etc.
    private static final Pattern INTERNAL_CMD = Pattern.compile("^@(\\w+)\\s*(.*)");
    private static MessageUtil msg = new MessageUtil();

    public static void process(List<String> commands, Player player) {
        for (String cmd : commands) {
            var matcher = INTERNAL_CMD.matcher(cmd);
            if (matcher.find()) {
                String keyword = matcher.group(1).toLowerCase();
                String args = matcher.group(2);
                handleInternal(keyword, args, player);
            } else {
                // Standard Bukkit command
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", player.getName()));
            }
        }
    }

    private static void handleInternal(String keyword, String args, Player player) {
        switch (keyword) {
            case "print" -> msg.sendMessage(player, args);
            default -> {
                msg.sendToPermission("vardoger.admin", "<gray>[<gold>vg</gold>] <em>Unknown internal command: @" + keyword);
                msg.sendToPermission("vardoger.admin", "<gray>[<gold>vg</gold>] <em>was called by " + player.getName());
                msg.sendToPermission("vardoger.admin", "<gray>[<gold>vg</gold>] <em>Please check your groups.yml file.");
                Bukkit.getLogger().warning("[Vardoger] Unknown internal command: @" + keyword + " found in groups.yml");
            }
        }
    }
}