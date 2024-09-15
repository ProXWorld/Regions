package net.proxworld.regions.config.hologram;

import lombok.NonNull;
import net.proxworld.regions.config.locale.Message;

public interface HologramSettings {

    double getHeight();

    @NonNull Message getMessage();

}
