package net.proxworld.regions.hook;

import com.google.common.collect.Maps;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.DecentHologramsAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.proxworld.regions.RegionPlugin;
import net.proxworld.regions.config.hologram.HologramSettings;
import org.bukkit.Location;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class DecentHologramHook implements HologramHook {

    RegionPlugin plugin;

    HologramSettings hologramSettings;

    public static DecentHologramHook create(final @NonNull RegionPlugin plugin) {
        return new DecentHologramHook(plugin,
                plugin.getGeneralConfig().getHologramSettings(), Maps.newHashMap());
    }

    Map<String, Hologram> holograms;

    @Override
    public void addHologram(String key, Location location, List<String> text) {
        if (holograms.containsKey(key)) {
            plugin.getLogger().warning("Hologram with key " + key + " already exists!");
            return;
        }

        val hologram = new Hologram(key, location);
        val page = hologram.getPage(0);

        for (String s : text) {
            val line = new HologramLine(page, page.getNextLineLocation(), s);
            line.setHeight(hologramSettings.getHeight());

            page.addLine(line);
        }

        hologram.showAll();
        holograms.put(key, hologram);
    }

    @Override
    public void removeHologram(String key) {
        val hologram = holograms.remove(key);

        if (hologram == null) {
            plugin.getLogger().warning("Hologram with key " + key + " does not exist!");
            return;
        }

        hologram.delete();
    }

    @Override
    public void updateHologram(String key, int line, String text) {
        val hologram = holograms.get(key);

        if (hologram == null) {
            plugin.getLogger().warning("Hologram with key " + key + " does not exist!");
            return;
        }

        DHAPI.setHologramLine(hologram, line, text);
    }

    @Override
    public void removeHolograms() {
        holograms.values().forEach(Hologram::delete);
        holograms.clear();
    }
}
