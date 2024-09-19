package net.proxworld.regions.flags;

import com.sk89q.worldguard.protection.flags.StateFlag;
import lombok.NonNull;

import java.util.Optional;

public interface RegionFlags {

    void registerFlag(final @NonNull StateFlag flag);

    void removeFlags();

    @NonNull Optional<StateFlag> findFlag(final @NonNull String name);

}
