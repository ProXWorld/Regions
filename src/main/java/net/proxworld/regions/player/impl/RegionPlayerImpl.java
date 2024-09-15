package net.proxworld.regions.player.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.proxworld.regions.player.RegionPlayer;

import java.util.UUID;

@AllArgsConstructor
//@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RegionPlayerImpl implements RegionPlayer {

    UUID playerUuid;

    public static @NonNull RegionPlayer create(
            final @NonNull UUID playerUuid,
            final boolean toggledCreateRegions
    ) {
        return new RegionPlayerImpl(playerUuid, toggledCreateRegions);
    }

    @Override
    public @NonNull UUID getPlayerUuid() {
        return playerUuid;
    }

    @NonFinal
    boolean toggledCreateRegions;

    @Override
    public boolean isToggledCreateRegions() {
        return toggledCreateRegions;
    }

    public void toggleCreateRegions() {
        toggledCreateRegions = !toggledCreateRegions;
    }

}
