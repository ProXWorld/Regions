package net.proxworld.regions.flags;

import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.proxworld.regions.RegionPlugin;

import java.util.Optional;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleRegionFlags implements RegionFlags {

    RegionPlugin plugin;

    Object2ObjectMap<String, StateFlag> flags;

    public static @NonNull SimpleRegionFlags create(final @NonNull RegionPlugin plugin) {
        return new SimpleRegionFlags(plugin, new Object2ObjectOpenHashMap<>());
    }

    @Override
    public void registerFlag(@NonNull StateFlag flag) {
        try {
            plugin.getWorldGuard().getFlagRegistry()
                    .register(flag);
        } catch (final FlagConflictException e) {
            // fix ebaniy plugman
            plugin.getSLF4JLogger().error("Failed to register flag: " + flag.getName());
            return;
        }

        flags.put(flag.getName(), flag);
        plugin.getSLF4JLogger().info("Registered flag: " + flag.getName());
    }

    @Override
    public void removeFlags() {
        flags.clear();
        plugin.getSLF4JLogger().info("Removed all flags");
    }

    @Override
    public @NonNull Optional<StateFlag> findFlag(@NonNull String name) {
        return Optional.ofNullable(flags.get(name));
    }

}
