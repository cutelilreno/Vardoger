/**
 * MIT License
 * Copyright (c) 2025 cutelilreno
 * https://opensource.org/licenses/MIT
 */
package moe.reno.vardoger.data;

import org.bukkit.Location;
import java.util.List;
import java.util.Map;

public class Group {
    private final String name;
    private final long requiredDuration;
    private final List<String> onComplete;
    private final Map<String, Location> signs;
    private final Map<String, List<String>> signCommands;
    private final double spyThreshold;

    public Group(String name, long requiredDuration, List<String> onComplete, Map<String, Location> signs, Map<String, List<String>> signCommands, double spyThreshold) {
        this.name = name;
        this.requiredDuration = requiredDuration;
        this.onComplete = onComplete;
        this.signs = signs;
        this.signCommands = signCommands;
        this.spyThreshold = spyThreshold;
    }
    public String getName() { return name; }
    public long getRequiredDuration() { return requiredDuration; }
    public List<String> getOnComplete() { return onComplete; }
    public Map<String, Location> getSigns() { return signs; }
    public Map<String, List<String>> getSignCommands() { return signCommands; }
    public double getSpyThreshold() { return spyThreshold; }
}