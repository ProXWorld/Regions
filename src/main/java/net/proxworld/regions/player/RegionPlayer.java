package net.proxworld.regions.player;

import lombok.NonNull;

import java.util.UUID;

public interface RegionPlayer {

    @NonNull UUID getPlayerUuid();

    boolean isToggledCreateRegions();



}
