package net.proxworld.regions.config;

import lombok.NonNull;
import net.proxworld.regions.block.RegionBlock;
import net.proxworld.regions.config.hologram.HologramSettings;
import net.proxworld.regions.config.limit.LimitSettings;
import net.proxworld.regions.config.locale.Message;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GeneralConfig {

    @Unmodifiable @NonNull Map<String, Message> getMessages();

    @NonNull Optional<Message> findMessage(final @NonNull String key);

    @NonNull Message getMessage(final @NonNull String key);

    @NonNull FileConfiguration getConfiguration();

    @NonNull List<String> getAllowedWorlds();

    @Nullable ItemStack getEnderChestItem();

    @Unmodifiable @NonNull List<RegionBlock> getRegionBlocks();

    boolean isRegionBlock(final @NonNull ItemStack itemStack);

    boolean isRegionBlock(final @NonNull Material material);

    @NonNull Optional<RegionBlock> findRegionBlock(final @NonNull String key);

    @NonNull Optional<RegionBlock> findRegionBlock(final @NonNull ItemStack itemStack);

    @NonNull Optional<RegionBlock> findRegionBlock(final @NonNull Material material);

    boolean isPlaceRegionBlockByShift();

    @NonNull HologramSettings getHologramSettings();

    @NonNull LimitSettings getLimitSettings();

    void init();

}
