/**
 * MIT License
 * Copyright (c) 2025 cutelilreno
 * https://opensource.org/licenses/MIT
 */
package moe.reno.vardoger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.File;

import moe.reno.vardoger.commands.VardogerCommand;
import moe.reno.vardoger.conf.ConfigManager;
import moe.reno.vardoger.conf.GroupManager;
import moe.reno.vardoger.conf.PlayerDataManager;
import moe.reno.vardoger.listener.GazeListener;

public class Vardoger extends JavaPlugin {
    private ConfigManager config;
    private GroupManager groupManager;
    private PlayerDataManager playerDataManager;
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private GazeListener gazeListener;

    @Override
    public void onEnable() {

        // Load config and groups
        saveDefaultConfig();
        config = new ConfigManager(getConfig());
        File groupsFile = new File(getDataFolder(), "groups.yml");
        if (!groupsFile.exists()) saveResource("groups.yml", false);
        YamlConfiguration groupsCfg = YamlConfiguration.loadConfiguration(groupsFile);
        groupManager = new GroupManager(groupsCfg, this);

        // Prepare player data
        File dataDir = new File(getDataFolder(), "playerdata");
        if (!dataDir.exists()) dataDir.mkdirs();
        playerDataManager = new PlayerDataManager(dataDir, gson);

        // Commands & listener
        getCommand("vg").setExecutor(new VardogerCommand(this));
        gazeListener = new GazeListener(this);
        Bukkit.getPluginManager().registerEvents(gazeListener, this);

        // Auto save
        new BukkitRunnable() {
            @Override
            public void run() {
                playerDataManager.saveAll();
            }
        }.runTaskTimer(this, 12000L, 12000L);
    }

    @Override
    public void onDisable() {
        // Cancel tasks and unregister listeners
        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);
        
        // Save final player data
        playerDataManager.saveAll();
    }

    public ConfigManager getConfigManager() { return config; }
    public GroupManager getGroupManager() { return groupManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public void setGroupManager(GroupManager gm) { this.groupManager = gm; }

    public GazeListener getGazeListener() { return gazeListener; }
    
}