package net.proxworld.regions.database;

import net.proxworld.regions.database.dto.RegionDto;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public interface RegionsDao {

    @NonNull List<RegionDto> findAllRegions();

    @Nullable RegionDto findRegionById(String name);

    int getRegionCount(final @NonNull String owner);

    int saveRegion(
            final @NonNull String name, final @NonNull String type,
            final @NonNull String owner, final @NonNull String world,
            final int x, final int y, final int z
    );

    void deleteRegion(final @NonNull String name);

}
