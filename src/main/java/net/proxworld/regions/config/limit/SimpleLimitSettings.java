package net.proxworld.regions.config.limit;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.bukkit.entity.Player;

import java.util.Map;

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
        // checking by permission proxregions.limit.<limit>
        val permissions = player.getEffectivePermissions();

        for (val permission : permissions) {
            val name = permission.getPermission();

            if (name.startsWith("proxregions.limit.")) {
                val limit = name.substring("proxregions.limit.".length());

                try {
                    return Integer.parseInt(limit);
                } catch (final NumberFormatException ignored) {
                }
            }
        }

        return defaultLimit;
    }
}
