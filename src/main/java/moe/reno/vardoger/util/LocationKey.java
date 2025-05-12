package moe.reno.vardoger.util;

import org.bukkit.Location;

public class LocationKey {
        public static String from(Location loc) {
        return loc.getWorld().getName() + ":" +
               loc.getBlockX() + ":" +
               loc.getBlockY() + ":" +
               loc.getBlockZ();
    }
}
