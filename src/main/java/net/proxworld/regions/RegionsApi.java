package net.proxworld.regions;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.NonNull;
import net.proxworld.regions.block.RegionBlock;
import net.proxworld.regions.command.CommandManager;
import net.proxworld.regions.config.GeneralConfig;
import net.proxworld.regions.hook.HologramHook;
import net.proxworld.regions.model.result.CreateResult;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface RegionsApi {

    @NonNull GeneralConfig getGeneralConfig();

    @NonNull WorldGuard getWorldGuard();

    @NonNull List<RegionBlock> getRegionBlocks();

    @NonNull CommandManager getCommandManager();

    @NonNull HologramHook getHologramHook();

    @NonNull CreateResult createRegion(
            final @NonNull String regionName, final @NonNull Player player,
            final @NonNull RegionBlock block, final @NonNull Location location
    );

    void addPlayerToCache(final @NonNull Player player, final @NonNull ProtectedRegion region);

    void removePlayerFromCache(final @NonNull Player player);

    ProtectedRegion isPlayerInCache(final @NonNull Player player);

    int getRegionCount(final @NonNull Player player);

    @NonNull RegionManager getRegionManagerByPlayer(final @NonNull World world);

    boolean destroyRegion(final @NonNull Block block);

    void addPlayerToRegion(
            final @NonNull Location location, final @NonNull String regionName, final @NonNull OfflinePlayer player
    );

    void removePlayerToRegion(final @NonNull ProtectedRegion region, final @NonNull UUID playerUUid);

    boolean isPlayerInRegion(final @NonNull Player player);

    boolean isOwnerOfRegion(final @NonNull String regionName, final @NonNull Player player);

}
