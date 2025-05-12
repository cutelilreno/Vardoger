package moe.reno.vardoger.commands;

import moe.reno.vardoger.Vardoger;
import moe.reno.vardoger.conf.GroupManager;
import moe.reno.vardoger.conf.ConfigManager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.io.IOException;

public class VardogerCommand implements CommandExecutor {
    private final Vardoger plugin;
    private ConfigManager config;
    public VardogerCommand(Vardoger plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length==0) {
            sender.sendMessage("§eUsage: /vg <reload|addgroup|addsign>");
            return true;
        }
        switch(args[0].toLowerCase()) {
            case "reload":
            if (!sender.hasPermission("vardoger.reload")) return noPerm(sender);
            plugin.reloadConfig();
            try {
                plugin.getGroupManager().reload();
                sender.sendMessage("§aReloaded vardoger config.");
            } catch(IOException e) {
                sender.sendMessage("§cFailed to reload config!");
                e.printStackTrace();
            }
            break;
            case "addgroup":
                if (!sender.hasPermission("vardoger.addgroup")) return noPerm(sender);
                if (args.length<2) { sender.sendMessage("§cUsage: /vg addgroup <name>"); return true; }
                String name = args[1];
                try {
                    plugin.getGroupManager().addGroup(name);
                    sender.sendMessage("§aGroup '"+name+"' added.");
                } catch(IOException e) {
                    sender.sendMessage("§cFailed to create group: " + e.getMessage());
                    e.printStackTrace();
                }
                break;
            case "addsign":
                if (!sender.hasPermission("vardoger.addsign")) return noPerm(sender);
                if (args.length < 3 || !(sender instanceof Player)) {
                    sender.sendMessage("§cUsage: /vg addsign <id> <group> (must be player)");
                    return true;
                }
                Player p = (Player) sender;
                String id = args[1], grp = args[2];
                GroupManager gm = plugin.getGroupManager();
                var group = gm.getGroup(grp);
                if (group == null) {
                    sender.sendMessage("§cNo such group.");
                    return true;
                }
                
                var block = p.getTargetBlockExact(config.getViewDistance());
                if (block == null) {
                    sender.sendMessage("§cNo block in range.");
                    return true;
                }
                var mat = block.getType();
                if (!config.getValidBlocks().contains(mat)) {
                    sender.sendMessage("§cBlock type " + mat + " not allowed.");
                    return true;
                }
                
                var loc = block.getLocation();
                try {
                    gm.addSign(grp, id, loc);
                    sender.sendMessage("§aSign '" + id + "' added to '" + grp + "'.");
                } catch (IOException e) {
                    e.printStackTrace();
                    sender.sendMessage("§cFailed to save sign to disk.");
                }
                break;
            default:
                sender.sendMessage("§cUnknown subcommand.");
        }
        return true;
    }
    private boolean noPerm(CommandSender s) { s.sendMessage("§cNo permission."); return true; }
}