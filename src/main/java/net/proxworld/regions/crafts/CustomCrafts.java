package net.proxworld.regions.crafts;

import lombok.NonNull;
import net.proxworld.regions.block.RegionBlock;
import org.bukkit.inventory.ShapedRecipe;

import java.util.Optional;

public interface CustomCrafts {

    void registerCrafts();

    void registerCraft(
            final @NonNull RegionBlock regionBlock, final @NonNull String key, final @NonNull String[] array
    );

    @NonNull Optional<ShapedRecipe> getRecipeByRegion(@NonNull RegionBlock regionBlock);

    void removeCrafts();

}
