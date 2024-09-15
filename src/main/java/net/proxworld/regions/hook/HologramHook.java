package net.proxworld.regions.hook;

import org.bukkit.Location;

import java.util.List;

public interface HologramHook {

    void addHologram(final String key, final Location location, final List<String> text);

    void removeHologram(final String key);

    void updateHologram(final String key, final int line, final String text);

    void removeHolograms();

}
