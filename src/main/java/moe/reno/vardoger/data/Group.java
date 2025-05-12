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

    public Group(String name, long requiredDuration, List<String> onComplete, Map<String, Location> signs) {
        this.name = name;
        this.requiredDuration = requiredDuration;
        this.onComplete = onComplete;
        this.signs = signs;
    }
    public String getName() { return name; }
    public long getRequiredDuration() { return requiredDuration; }
    public List<String> getOnComplete() { return onComplete; }
    public Map<String, Location> getSigns() { return signs; }
}