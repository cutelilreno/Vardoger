/**
 * MIT License
 * Copyright (c) 2025 cutelilreno
 * https://opensource.org/licenses/MIT
 */
package moe.reno.vardoger.listener;

import moe.reno.vardoger.Vardoger;
import moe.reno.vardoger.conf.ConfigManager;
import moe.reno.vardoger.conf.GroupManager;
import moe.reno.vardoger.conf.PlayerDataManager;
import moe.reno.vardoger.conf.PlayerDataManager.PlayerProgress;
import moe.reno.vardoger.data.Group;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.RayTraceResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GazeListener implements Listener {
    private final ConfigManager config;
    private final GroupManager gm;
    private final PlayerDataManager pdm;
    private final Vardoger plugin;

    private final Map<String, String[]> signs = new HashMap<>();
    private final Set<String> trackedChunks = new HashSet<>(); // world:x,z
    private final Map<UUID, Boolean> inRange = new HashMap<>();
    private final Map<UUID, Long> lastAccumulation = new HashMap<>();

    private static final int CHECK_INTERVAL_TICKS = 5;

    public GazeListener(Vardoger plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.gm     = plugin.getGroupManager();
        this.pdm    = plugin.getPlayerDataManager();

        initTrackedChunks();
        startRayTraceTask();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        UUID   uuid   = player.getUniqueId();
        Location from  = e.getFrom();
        Location to    = e.getTo();
        if (to == null) return;

        // update chunk tracking
        int fromX = from.getBlockX() >> 4, fromZ = from.getBlockZ() >> 4;
        int toX   =   to.getBlockX() >> 4, toZ   =   to.getBlockZ() >> 4;
        if (!from.getWorld().equals(to.getWorld()) || fromX != toX || fromZ != toZ) {
            //player.sendMessage("§7[Debug] Entering new chunk area");
            boolean nowInRange = false;
            String world = to.getWorld().getName();
            for (int dx = -1; dx <= 1 && !nowInRange; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    String chunkKey = world + ":" + (toX + dx) + "," + (toZ + dz);
                    if (trackedChunks.contains(chunkKey)) {
                        nowInRange = true;
                        break;
                    }
                }
            }
            inRange.put(uuid, nowInRange);
            
            // End current gaze if necessary
            if (!nowInRange) {
                PlayerProgress prog = pdm.getProgress(uuid);
                if (prog.currentSign != null) {
                    accumulateTime(uuid, prog, System.currentTimeMillis()); // update time first!!!
                    //player.sendMessage("§7[Debug] Left tracked chunk area, ending gaze");
                    prog.currentGroup = null;
                    prog.currentSign = null;
                    prog.lookStart = 0;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        // save + clean up
        pdm.save(uuid);
        lastAccumulation.remove(uuid);
        inRange.remove(uuid);
    }
    
    private void startRayTraceTask() {
        
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            long now = System.currentTimeMillis();
            
            // Check all online players
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!inRange.getOrDefault(player.getUniqueId(), false)) continue;
                processPlayerGaze(player, now);
            }
        }, CHECK_INTERVAL_TICKS, CHECK_INTERVAL_TICKS);
    }

    private void processPlayerGaze(Player player, long now) {
        UUID uuid = player.getUniqueId();
        
        RayTraceResult result = player.rayTraceBlocks(config.getViewDistance());
        String[] hitInfo = null;
        if (result != null && result.getHitBlock() != null) {
            hitInfo = signs.get(locationKey(result.getHitBlock().getLocation()));
            //if (hitInfo != null) {
            //    player.sendMessage(String.format("§7[Debug] Ray hit sign %s in group %s", hitInfo[1], hitInfo[0]));
            //}
        }

        // Update progress
        PlayerProgress prog = pdm.getProgress(uuid);
        if (prog.currentSign != null) {
            accumulateTime(uuid, prog, now);
            
            //player.sendMessage(String.format("§7[Debug] Gaze time for %s: %d ticks (%.1f seconds)", 
            //    prog.currentSign, 
            //    prog.totalTicks.getOrDefault(prog.currentGroup, Map.of()).getOrDefault(prog.currentSign, 0L),
            //    prog.totalTicks.getOrDefault(prog.currentGroup, Map.of()).getOrDefault(prog.currentSign, 0L) / 20.0));
            
            // check if completed the group
            String groupName = prog.currentGroup;
            Group group = gm.getGroup(groupName);
            long required = group.getRequiredDuration();
            
            Map<String, Boolean> signStates = prog.completedSigns
                .computeIfAbsent(groupName, k -> new HashMap<>());

            // sign's progress logic
            group.getSigns().keySet().forEach(id -> {
                long current = prog.totalTicks
                    .getOrDefault(groupName, Map.of())
                    .getOrDefault(id, 0L);

                if (current >= required && !signStates.getOrDefault(id, false)) {
                    signStates.put(id, true);
                    runCommands(group.getSignCommands().getOrDefault(id, List.of()), player);
                    //player.sendMessage(String.format("§7[Debug] Completed sign %s!", id));
                    //player.sendMessage("[Debug]: Sign " + id + " has " + current + " ticks (req: " + required + "), done: " + signStates.getOrDefault(id, false));
                }

                //player.sendMessage(String.format("§7[Debug] Sign %s progress: %d/%d ticks (completed: %s)",
                //    id, current, required, signStates.getOrDefault(id, false)));
            });
            
            boolean allDone = group.getSigns().keySet().stream()
                .allMatch(id -> signStates.getOrDefault(id, false));
                    
            if (allDone && !prog.completedGroups.getOrDefault(groupName, false)) {
                //player.sendMessage("§7[Debug] Completed group " + groupName);
                runCommands(group.getOnComplete(), player);
                prog.completedGroups.put(groupName, true);
            }

            // reset or continue?
            boolean sameSign = hitInfo != null
                && hitInfo[0].equals(prog.currentGroup)
                && hitInfo[1].equals(prog.currentSign);

            if (!sameSign) {
                //player.sendMessage("§7[Debug] Ending gaze at " + prog.currentSign);
                prog.currentGroup = null;
                prog.currentSign = null;
                prog.lookStart = 0;
            }
        }
        
        if (hitInfo != null) {
            prog.currentGroup = hitInfo[0];
            prog.currentSign  = hitInfo[1];
            prog.lookStart    = now;
            //player.sendMessage(String.format("§7[Debug] Starting new gaze at %s", prog.currentSign));
        }
    }

    private void initTrackedChunks() {
        for (var entry : gm.getGroups().entrySet()) {
            String groupName = entry.getKey();
            Group group = entry.getValue();
            for (var signEntry : group.getSigns().entrySet()) {
                String signId = signEntry.getKey();
                Location loc  = signEntry.getValue();
                signs.put(locationKey(loc), new String[]{ groupName, signId });

                int cx = loc.getBlockX() >> 4;
                int cz = loc.getBlockZ() >> 4;
                String key = loc.getWorld().getName() + ":" + cx + "," + cz;
                trackedChunks.add(key);
            }
        }
    }

    private void accumulateTime(UUID uuid, PlayerProgress prog, long now) {
        if (prog.currentSign == null) return;
        
        long lastAccum = lastAccumulation.getOrDefault(uuid, prog.lookStart);
        long deltaTicks = (now - lastAccum) / 50;
        if (deltaTicks > 0) {
            prog.totalTicks
                .computeIfAbsent(prog.currentGroup, k -> new HashMap<>())
                .merge(prog.currentSign, deltaTicks, Long::sum);
        }
        lastAccumulation.put(uuid, now);
    }

    private String locationKey(Location loc) {
        return loc.getWorld().getName() + ":" +
            loc.getBlockX() + ":" +
            loc.getBlockY() + ":" +
            loc.getBlockZ();
    }

    private void runCommands(List<String> commands, Player player) {
        commands.forEach(cmd ->
            Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                cmd.replace("{player}", player.getName())
            )
        );
    }

}
