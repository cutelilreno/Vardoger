/**
 * MIT License
 * Copyright (c) 2025 cutelilreno
 * https://opensource.org/licenses/MIT
 */
package moe.reno.vardoger.conf;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {
    private final File dataDir;
    private final Gson gson;
    private final Map<UUID, PlayerProgress> cache = new ConcurrentHashMap<>();
    private final Type progressType = new TypeToken<PlayerProgress>(){}.getType();
    private final Object saveLock = new Object(); // Global lock for all save operations

    public PlayerDataManager(File dataDir, Gson gson) {
        this.dataDir = dataDir;
        this.gson = gson;
    }

    public PlayerProgress getProgress(UUID uuid) {
        return cache.computeIfAbsent(uuid, this::load);
    }

    private PlayerProgress load(UUID uuid) {
        synchronized(saveLock) {  // Prevent reads during writes
            File f = new File(dataDir, uuid+".json");
            if (!f.exists()) return new PlayerProgress();
            try (FileReader r = new FileReader(f)) {
                return gson.fromJson(r, progressType);
            } catch(IOException ex) { 
                ex.printStackTrace(); 
                return new PlayerProgress(); 
            }
        }
    }

    public void save(UUID uuid) {
        PlayerProgress p = cache.get(uuid);
        if (p == null) { return; } // No progress to save

        synchronized(saveLock) {  // Global lock for all file operations
            File tempFile = new File(dataDir, uuid+".json.tmp");
            File targetFile = new File(dataDir, uuid+".json");
            
            try {
                if(tempFile.exists()) {
                    tempFile.delete();
                }

                try (FileWriter w = new FileWriter(tempFile)) {
                    gson.toJson(p, w);
                    w.flush();
                }
                
                Files.move(tempFile.toPath(), targetFile.toPath(), 
                    StandardCopyOption.ATOMIC_MOVE, 
                    StandardCopyOption.REPLACE_EXISTING);
                
            } catch(IOException ex) {
                if(tempFile.exists()) {
                    tempFile.delete();
                }
                ex.printStackTrace();
            } finally {
                if (!Bukkit.getOfflinePlayer(uuid).isOnline()) {
                    cache.remove(uuid);
                }
            }
        }
    }

    public void saveAll() {
        synchronized(saveLock) {  // Global lock prevents concurrent saves
            for(UUID uuid : cache.keySet()) {
                PlayerProgress p = cache.get(uuid);
                
                File tempFile = new File(dataDir, uuid+".json.tmp");
                File targetFile = new File(dataDir, uuid+".json");
                
                try {
                    if(tempFile.exists()) {
                        tempFile.delete();
                    }

                    try (FileWriter w = new FileWriter(tempFile)) {
                        gson.toJson(p, w);
                        w.flush();
                    }
                    
                    Files.move(tempFile.toPath(), targetFile.toPath(), 
                        StandardCopyOption.ATOMIC_MOVE, 
                        StandardCopyOption.REPLACE_EXISTING);
                    
                } catch(IOException ex) {
                    if(tempFile.exists()) {
                        tempFile.delete();
                    }
                    ex.printStackTrace();
                }
            }
        }
    }

    public static class PlayerProgress {
        public Map<String, Map<String, Long>> totalTicks = new HashMap<>();
        public Map<String, Boolean> completedGroups = new HashMap<>();
        public transient String currentGroup;
        public transient String currentSign;
        public transient long lookStart;
        public Map<String, Map<String, Boolean>> completedSigns = new HashMap<>();
        public Map<String, Boolean> completedSpyThreshold = new HashMap<>();
    }
}