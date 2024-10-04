package net.proxworld.regions.handler.impl;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.proxworld.regions.RegionPlugin;
import net.proxworld.regions.handler.AbstractActionBarHandler;

import java.util.Set;

/**
 * @author saydov
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class FarewellHandler extends AbstractActionBarHandler {

    private FarewellHandler(@NonNull RegionPlugin plugin, @NonNull Session session) {
        super(plugin, session);
    }

    public static @NonNull FarewellHandler create(
            final @NonNull RegionPlugin plugin, final @NonNull Session session
    ) {
        return new FarewellHandler(plugin, session);
    }

    @Override
    public void _handleRegion(
            final @NonNull LocalPlayer player,
            final @NonNull ApplicableRegionSet toSet,
            final @NonNull Set<ProtectedRegion> enteredRegions,
            final @NonNull Set<ProtectedRegion> exitedRegions
    ) {
        if (exitedRegions.isEmpty()) return;

        plugin.getRegionFlags().findFlag("proxregions-farewell-message")
                .ifPresent(flag -> {
                    for (final ProtectedRegion region : exitedRegions) {
                        val state = region.getFlag(flag);
                        if (state == null) continue;
                        if (state == StateFlag.State.DENY) continue;

                        sendActionBar(player, "FAREWELL", "region", region.getId());
                        break;
                    }
                });
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@NonNull))
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class Factory extends Handler.Factory<FarewellHandler> {

        RegionPlugin plugin;

        public static @NonNull Factory create(final @NonNull RegionPlugin plugin) {
            return new Factory(plugin);
        }

        @Override
        public FarewellHandler create(Session session) {
            return FarewellHandler.create(plugin, session);
        }
    }

}
