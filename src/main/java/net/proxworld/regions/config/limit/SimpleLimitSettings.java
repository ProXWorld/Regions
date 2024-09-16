package net.proxworld.regions.config.limit;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.proxworld.regions.util.CountablePermission;
import org.bukkit.entity.Player;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class SimpleLimitSettings implements LimitSettings {

    public static @NonNull SimpleLimitSettings create(final int defaultLimit) {
        return new SimpleLimitSettings(defaultLimit);
    }

    int defaultLimit;

    @Override
    public int getDefaultLimit() {
        return defaultLimit;
    }

    @Override
    public int getLimit(@NonNull Player player) {
        return CountablePermission.maxAvailable(player, "proxregions.limit")
                .orElse(Integer.MAX_VALUE);
    }
}
