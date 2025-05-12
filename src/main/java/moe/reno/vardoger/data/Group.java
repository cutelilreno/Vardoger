/**
 * MIT License
 * Copyright (c) 2025 cutelilreno
 * https://opensource.org/licenses/MIT
 */
package moe.reno.vardoger.data;

import org.bukkit.Location;
import java.util.List;
import java.util.Map;

public record Group(
    String name,
    long requiredDuration,
    List<String> onComplete,
    Map<String, Location> signs,
    Map<String, List<String>> signCommands,
    double spyThreshold
) {}
