package net.proxworld.regions.player;

import lombok.NonNull;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface PlayerManager {

    void addPlayer(final @NonNull UUID uuid);

    void removePlayer(final @NonNull UUID uuid);

    boolean hasPlayer(final @NonNull UUID uuid);

    Optional<RegionPlayer> getOrCreate(final @NonNull UUID uuid);

    Collection<RegionPlayer> getPlayers();

}
