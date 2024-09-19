package net.proxworld.regions.crafts;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.proxworld.regions.block.RegionBlock;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ = @NonNull)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class SimpleCustomCrafts implements CustomCrafts {

    JavaPlugin javaPlugin;

    Object2ObjectMap<NamespacedKey, CraftingRegionBlock> crafts;

    public static @NonNull SimpleCustomCrafts create(
            final @NonNull JavaPlugin javaPlugin
    ) {
        return new SimpleCustomCrafts(javaPlugin, new Object2ObjectOpenHashMap<>());
    }

    @Override
    public void registerCrafts() {
        crafts.forEach((key, b) -> _registerCraft(b));
    }

    private void _registerCraft(final @NonNull CraftingRegionBlock b) {
        // javaPlugin.getServer().addRecipe();
    }

    @Override
    public void registerCraft(
            final @NonNull RegionBlock regionBlock,
            final @NonNull String key,
            final @NonNull String[] array
    ) {

    }

    @Override
    public @NonNull Optional<ShapedRecipe> getRecipeByRegion(@NonNull RegionBlock regionBlock) {
          return Optional.empty();
    }

    @Override
    public void removeCrafts() {
        crafts.forEach((key, b) -> _removeCraft(b));
        crafts.clear();
    }

    private void _removeCraft(final @NonNull CraftingRegionBlock block) {
        javaPlugin.getServer().removeRecipe(block.getKey());
    }

    protected interface CraftingRegionBlock {

        @NonNull RegionBlock getBlock();

        @NonNull String[] getIngredients();

        @NonNull NamespacedKey getKey();

    }

    @Getter(value = AccessLevel.PUBLIC)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ = @NonNull)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    protected static class SimpleCraftingRegionBlock implements CraftingRegionBlock {

        RegionBlock block;

        String[] ingredients;

        NamespacedKey key;

        public static @NonNull SimpleCraftingRegionBlock create(
                final @NonNull RegionBlock block,
                final @NonNull String[] ingredients,
                final @NonNull NamespacedKey key
        ) {
            return new SimpleCraftingRegionBlock(block, ingredients, key);
        }

    }

}
