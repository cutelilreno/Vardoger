/**
 * MIT License
 * Copyright (c) 2025 cutelilreno
 * https://opensource.org/licenses/MIT
 */
package moe.reno.vardoger.listener;

import moe.reno.vardoger.Vardoger;
import moe.reno.vardoger.commands.CommandProcessor;
import moe.reno.vardoger.conf.ConfigManager;
import moe.reno.vardoger.conf.GroupManager;
import moe.reno.vardoger.conf.PlayerDataManager;
import moe.reno.vardoger.conf.PlayerDataManager.PlayerProgress;
import moe.reno.vardoger.data.Group;
import moe.reno.vardoger.util.MessageUtil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.RayTraceResult;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GazeListener implements Listener {
    private final ConfigManager config;
    private final GroupManager gm;
    private final PlayerDataManager pdm;
    private final Vardoger plugin;
    private final MessageUtil msg = new MessageUtil();

    private final Object2ObjectOpenHashMap<String, String[]> signs = new Object2ObjectOpenHashMap<>();
    private final Set<String> trackedChunks = new ObjectOpenHashSet<>(); // world:x,z
    private final Object2BooleanOpenHashMap<UUID> inRange = new Object2BooleanOpenHashMap<>();
    private final Object2LongOpenHashMap<UUID> lastAccumulation = new Object2LongOpenHashMap<>();

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
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        boolean nowInRange = false;
        int x = player.getLocation().getBlockX() >> 4;
        int z = player.getLocation().getBlockZ() >> 4;
        String world = player.getWorld().getName();
        
        for (int dx = -1; dx <= 1 && !nowInRange; dx++) {
        for (int dz = -1; dz <= 1; dz++) {
            String chunkKey = world + ":" + (x + dx) + "," + (z + dz);
            if (trackedChunks.contains(chunkKey)) {
                nowInRange = true;
                break;
            }
        }
    }
        inRange.put(uuid, nowInRange);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        UUID uuid = player.getUniqueId();
        boolean nowInRange = false;
        int x = to.getBlockX() >> 4;
        int z = to.getBlockZ() >> 4;
        String world = to.getWorld().getName();

        for (int dx = -1; dx <= 1 && !nowInRange; dx++) {
        for (int dz = -1; dz <= 1; dz++) {
            String chunkKey = world + ":" + (x + dx) + "," + (z + dz);
            if (trackedChunks.contains(chunkKey)) {
                nowInRange = true;
                break;
            }
        }
    }
        inRange.put(uuid, nowInRange);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        PlayerProgress prog = pdm.getProgress(uuid);
        // save + clean up
        if (prog != null) {
            prog.lastCompleted.clear();
        }
        pdm.save(uuid);
        lastAccumulation.removeLong(uuid);
        inRange.removeBoolean(uuid);
    }
    
    private void startRayTraceTask() {
        
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            long now = System.currentTimeMillis();
            
            // Check all online players
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!inRange.getBoolean(player.getUniqueId())) continue;
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
            
            Map<String, Boolean> signStates = prog.completedSigns
                        .computeIfAbsent(groupName, k -> new HashMap<>());

            handleSpyThreshold(player, prog, group, groupName);           
            handleSignProgress(player, prog, group, groupName, now);
            
            boolean allDone = group.signs().keySet().stream()
                .allMatch(id -> signStates.getOrDefault(id, false));
                    
            if (allDone && !prog.completedGroups.getOrDefault(groupName, false)) {
                //player.sendMessage("§7[Debug] Completed group " + groupName);
                runCommands(group.onComplete(), player);
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
            for (var signEntry : group.signs().entrySet()) {
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
                .computeIfAbsent(prog.currentGroup, k -> new Object2LongOpenHashMap<>())
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
        CommandProcessor.process(commands, player);
    }
    
    private void notifySpy(Player player, Group group) {
        String message = "<gray>[<gold>vg</gold>] <em>" + player.getName() + " has looked at " + group.name() + ".";
        msg.sendToPermission("vardoger.spy", message);
    }

    private void handleSignProgress(Player player, PlayerProgress prog, Group group, String groupName, long now) {
        Map<String, Boolean> signStates = prog.completedSigns.computeIfAbsent(groupName, k -> new HashMap<>());
        Map<String, Long> totalTicks = prog.totalTicks.getOrDefault(groupName, new Object2LongOpenHashMap<>());
        Map<String, Long> lastCompleted = prog.lastCompleted;

        String id = prog.currentSign;
if (id != null && group.signs().containsKey(id)) {
            long current = totalTicks.getOrDefault(id, 0L);
            boolean completed = signStates.getOrDefault(id, false);
            int cooldown = group.cooldown();

            long last = lastCompleted.getOrDefault(id, 0L);

            // If sign is completed and cooldown is enabled, check for rerun
            if (completed && cooldown > -1) {
                if (last == 0L || (now - last) >= cooldown * 1000L) {
                    runCommands(group.signCommands().getOrDefault(id, List.of()), player);
                    lastCompleted.put(id, now);
                }
            }

            // If not completed, check if requirements are met to complete
            if (!completed && current >= group.requiredDuration()) {
                signStates.put(id, true);
                runCommands(group.signCommands().getOrDefault(id, List.of()), player);
                lastCompleted.put(id, now);
            }
        }
    }

    private void handleSpyThreshold(Player player, PlayerProgress prog, Group group, String groupName) {
        if (group.spyThreshold() > 0.0) {
            Map<String, Boolean> signStates = prog.completedSigns.computeIfAbsent(groupName, k -> new HashMap<>());
            double percentCompleted = signStates.values().stream()
                .filter(Boolean::booleanValue)
                .count() / (double) group.signs().size();
            if (!prog.completedSpyThreshold.getOrDefault(groupName, false)
                && percentCompleted >= group.spyThreshold()) {
                prog.completedSpyThreshold.put(groupName, true);
                notifySpy(player, group);
            }
        }
    }

}
