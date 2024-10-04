package net.proxworld.regions.block;

import com.sk89q.worldguard.protection.flags.StateFlag;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SimpleRegionBlock implements RegionBlock {

    String key;

    ItemStack item;

    boolean exploding, onlyGive;

    int size;

    List<Pair<StateFlag, Boolean>> flags;

    public static @NonNull SimpleRegionBlock create(
            final @NonNull String key, final @NonNull ItemStack itemStack, final int size, final boolean exploding,
            final boolean onlyGive, final @NonNull List<Pair<StateFlag, Boolean>> flags
    ) {
        return new SimpleRegionBlock(key, itemStack, exploding, onlyGive, size, flags);
    }

}
