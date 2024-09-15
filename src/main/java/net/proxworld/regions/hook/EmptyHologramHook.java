package net.proxworld.regions.hook;

import org.bukkit.Location;

import java.util.List;

public final class EmptyHologramHook implements HologramHook {

   @Override
    public void addHologram(String key, Location location, List<String> text) {
    }

    @Override
    public void removeHologram(String key) {
    }

    @Override
    public void updateHologram(String key, int line, String text) {
    }

    @Override
    public void removeHolograms() {
    }

}
