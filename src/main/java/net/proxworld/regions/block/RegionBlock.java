package net.proxworld.regions.block;

import com.sk89q.worldguard.protection.flags.StateFlag;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface RegionBlock {

    @NonNull String getKey();

    @NonNull ItemStack getItem();

    boolean onlyCrafting();

    int getSize();

    @NonNull List<Pair<StateFlag, Boolean>> getFlags();

}