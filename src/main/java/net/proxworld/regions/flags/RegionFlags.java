package net.proxworld.regions.flags;

import com.sk89q.worldguard.protection.flags.StateFlag;
import lombok.NonNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Optional;

public interface RegionFlags {

    void registerFlag(final @NonNull StateFlag flag);

    @Unmodifiable @NonNull Collection<StateFlag> getFlags();

    void removeFlags();

    @NonNull Optional<StateFlag> findFlag(final @NonNull String name);

}
