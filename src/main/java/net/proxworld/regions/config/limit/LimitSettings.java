package net.proxworld.regions.config.limit;

import lombok.NonNull;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

public interface LimitSettings {

    int getDefaultLimit();

    int getLimit(final @NonNull Player player);

}
