package net.proxworld.regions.database.sqlite;

import net.proxworld.regions.database.RegionsDao;
import net.proxworld.regions.database.dto.RegionDto;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;

public interface RegionsDaoSQLite extends RegionsDao {

    @SqlQuery("SELECT * FROM regions")
    @NonNull List<RegionDto> findAllRegions();

    @SqlQuery("SELECT * FROM regions WHERE name = :NAME")
    @Nullable RegionDto findRegionById(final @NonNull @Bind("NAME") String name);

    @SqlQuery("SELECT COUNT(*) FROM regions WHERE owner = :OWNER")
    int getRegionCount(final @NonNull String owner);

    @SqlQuery("INSERT INTO regions (name, type, owner, world, x, y, z) VALUES (:NAME, :TYPE, :OWNER, :WORLD, :X, :Y, :Z)")
    int saveRegion(
            final @NonNull @Bind("NAME") String name, final @NonNull @Bind("TYPE") String type,
            final @NonNull @Bind("OWNER") String owner, final @NonNull @Bind("WORLD") String world,
            final @Bind("X") int x, final @Bind("Y") int y, final @Bind("Z") int z
    );

    @SqlQuery("DELETE FROM regions WHERE name = :NAME")
    void deleteRegion(final @NonNull @Bind("NAME") String name);

}
