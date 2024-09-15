package net.proxworld.regions.config.hologram;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.proxworld.regions.config.locale.Message;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class SimpleHologramSettings implements HologramSettings {

    double height;

    @NonNull Message message;

    public static SimpleHologramSettings create(
            final double height, final @NonNull Message message
    ) {
        return new SimpleHologramSettings(height, message);
    }

}
