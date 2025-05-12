package moe.reno.vardoger.conf;

import org.bukkit.Material;
import java.util.List;

public class ConfigManager {
    private final int viewDistance;
    private final List<Material> validBlocks;

    public ConfigManager(org.bukkit.configuration.file.FileConfiguration cfg) {
        this.viewDistance = cfg.getInt("view-distance", 12);
        this.validBlocks = cfg.getStringList("valid-blocks").stream()
                .map(Material::valueOf)
                .toList();
    }
    public int getViewDistance() { return viewDistance; }
    public List<Material> getValidBlocks() { return validBlocks; }
}
