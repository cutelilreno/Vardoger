/**
 * MIT License
 * Copyright (c) 2025 cutelilreno
 * https://opensource.org/licenses/MIT
 */
package moe.reno.vardoger.conf;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import moe.reno.vardoger.Vardoger;
import moe.reno.vardoger.data.Group;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupManager {
    private final Map<String, Group> groups = new HashMap<>();
    Vardoger plugin;

    public GroupManager(YamlConfiguration cfg, Vardoger v) {
        this.plugin = v;
        for (String name : cfg.getKeys(false)) {
            ConfigurationSection sec = cfg.getConfigurationSection(name);
            long duration = sec.getLong("requiredDuration");
            List<String> commands = sec.getStringList("onComplete");
            ConfigurationSection signsSec = sec.getConfigurationSection("signs");
            Map<String, Location> signs = new HashMap<>();
            Map<String, List<String>> signCommands = new HashMap<>();
            double spyThreshold = sec.getDouble("spyThreshold", 0.0);
            for (String id : signsSec.getKeys(false)) {
                ConfigurationSection s = signsSec.getConfigurationSection(id);
                Location loc = new Location(
                        Bukkit.getWorld(s.getString("world")),
                        s.getInt("x"), s.getInt("y"), s.getInt("z")
                );
                signs.put(id, loc);
                if (s.contains("onComplete")) {
                    signCommands.put(id, s.getStringList("onComplete"));
                }
            }
            groups.put(name, new Group(name, duration, commands, signs, signCommands, spyThreshold));
        }
    }

    public Group getGroup(String name) { return groups.get(name); }
    public Map<String, Group> getGroups() { return groups; }

    public void addGroup(String name) throws IOException {
        File gf = new File(plugin.getDataFolder(), "groups.yml");
        YamlConfiguration yc = YamlConfiguration.loadConfiguration(gf);
        yc.createSection(name);
        yc.getConfigurationSection(name).set("requiredDuration", 5);
        yc.getConfigurationSection(name).set("onComplete", Collections.emptyList());
        yc.getConfigurationSection(name).createSection("signs");
        yc.save(gf);
        
        groups.put(name, new Group(name, 5, Collections.emptyList(), new HashMap<>(), new HashMap<>(), 0.0));
    }

    public void addSign(String groupName, String signId, Location location) throws IOException {
        Group group = groups.get(groupName);
        if (group == null) throw new IllegalArgumentException("No such group: " + groupName);
        
        group.signs().put(signId, location);

        // Update file
        File gf = new File(plugin.getDataFolder(), "groups.yml");
        YamlConfiguration yc = YamlConfiguration.loadConfiguration(gf);
        ConfigurationSection signs = yc.getConfigurationSection(groupName + ".signs");
        if (signs == null) {
            signs = yc.createSection(groupName + ".signs");
        }
        ConfigurationSection signSection = signs.createSection(signId);
        signSection.set("world", location.getWorld().getName());
        signSection.set("x", location.getBlockX());
        signSection.set("y", location.getBlockY());
        signSection.set("z", location.getBlockZ());
        yc.save(gf);
    }

    public void reload() throws IOException {
        File gf = new File(plugin.getDataFolder(), "groups.yml");
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(gf);
        groups.clear();
        
        for (String name : cfg.getKeys(false)) {
            ConfigurationSection sec = cfg.getConfigurationSection(name);
            long duration = sec.getLong("requiredDuration");
            List<String> commands = sec.getStringList("onComplete");
            ConfigurationSection signsSec = sec.getConfigurationSection("signs");
            Map<String, Location> signs = new HashMap<>();
            Map<String, List<String>> signCommands = new HashMap<>();
            double spyThreshold = sec.getDouble("spyThreshold", 0.0);
            for (String id : signsSec.getKeys(false)) {
                ConfigurationSection s = signsSec.getConfigurationSection(id);
                Location loc = new Location(
                        Bukkit.getWorld(s.getString("world")),
                        s.getInt("x"), s.getInt("y"), s.getInt("z")
                );
                signs.put(id, loc);
                if (s.contains("onComplete")) {
                    signCommands.put(id, s.getStringList("onComplete"));
                }
            }
            groups.put(name, new Group(name, duration, commands, signs, signCommands, spyThreshold));
        }
    }
}