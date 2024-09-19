package net.proxworld.regions;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import me.darkakyloff.api.menu.Menu;
import me.darkakyloff.api.menu.SimpleMenu;
import me.darkakyloff.api.utils.WorldGuardUtils;
import net.proxworld.regions.block.RegionBlock;
import net.proxworld.regions.command.CommandManager;
import net.proxworld.regions.command.SimpleCommandManager;
import net.proxworld.regions.command.impl.GiveRegionCommand;
import net.proxworld.regions.command.impl.RegionsCommand;
import net.proxworld.regions.config.GeneralConfig;
import net.proxworld.regions.config.SimpleGeneralConfig;
import net.proxworld.regions.crafts.CustomCrafts;
import net.proxworld.regions.crafts.SimpleCustomCrafts;
import net.proxworld.regions.flags.RegionFlags;
import net.proxworld.regions.hook.DecentHologramHook;
import net.proxworld.regions.hook.EmptyHologramHook;
import net.proxworld.regions.hook.HologramHook;
import net.proxworld.regions.logic.EventListener;
import net.proxworld.regions.menu.RegionMemberMenu;
import net.proxworld.regions.menu.RegionMenu;
import net.proxworld.regions.model.result.CreateResult;
import net.proxworld.regions.util.CountablePermission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class RegionPlugin extends JavaPlugin implements RegionsApi {

    GeneralConfig generalConfig;

    CommandManager commandManager;

    HologramHook hologramHook;

    CustomCrafts customCrafts;

    @Override
    public void onLoad() {
        generalConfig = SimpleGeneralConfig.create(this);
        generalConfig.init();

        commandManager = SimpleCommandManager.create();

        customCrafts = SimpleCustomCrafts.create(this);
        customCrafts.registerCrafts();

        if (getServer().getPluginManager().isPluginEnabled("DecentHolograms")) {
            hologramHook = DecentHologramHook.create(this);
            getLogger().info("DecentHolograms found, using DecentHologramHook");
        } else {
            hologramHook = new EmptyHologramHook();
            getLogger().warning("DecentHolograms not found, using EmptyHologramHook");
        }

        playerCache = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .build();

        _loadHolograms();
    }

    private void _loadHolograms() {
        for (val world : generalConfig.getAllowedWorlds()) {
            val w = getServer().getWorld(world);

            if (w == null) continue;

            val regionManager = getRegionManagerByPlayer(w);

            for (val region : regionManager.getRegions().values()) {
                val name = region.getId();

                val split = name.split("_");

                if (split.length != 4) continue;

                val x = Integer.parseInt(split[1]);

                val y = Integer.parseInt(split[2]);

                val z = Integer.parseInt(split[3]);

                val block = w.getBlockAt(x, y, z);

                val rgBlock = generalConfig.findRegionBlock(block.getType());

                if (rgBlock.isEmpty()) continue;

                val loc = new Location(w, x, y, z);

                val b = rgBlock.get();

                // заебалл то, что этот код спизжен с EventListener
                // todo: сделать утилиту какую-то
                @SuppressWarnings("deprecation")
                val displayName = Optional.ofNullable(b.getItem().getItemMeta())
                        .map(ItemMeta::getDisplayName)
                        .orElse(b.getItem().getType().name());

                @SuppressWarnings("ConstantConditions")
                val owner = region.getOwners().getUniqueIds().stream()
                        .map(getServer()::getOfflinePlayer)
                        .map(OfflinePlayer::getName)
                        .findFirst()
                        .orElse("Unknown");

                hologramHook.addHologram(
                        name, loc.clone().add(0.5, 2, 0.5),
                        generalConfig.getHologramSettings().getMessage()
                                .format("type", displayName)
                                .format("size", b.getSize())
                                .format("owner", owner)
                                .getLines()
                );
            }
        }
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onEnable() {
        getServer().getPluginManager()
                .registerEvents(EventListener.create(this, generalConfig), this);

        commandManager.registerCommand(RegionsCommand.create(this));
        commandManager.registerCommand(GiveRegionCommand.create(this));

        CountablePermission.registerRange("proxregions.limit", 1, 100);
    }

    @Override
    public void onDisable() {
        commandManager.unregisterCommands();
        hologramHook.removeHolograms();

        for (val player : Bukkit.getOnlinePlayers()) {
            val holder = player.getOpenInventory()
                    .getTopInventory().getHolder();

            if (holder != null) {
                if (holder instanceof Menu menu) {
                    if (!(menu instanceof SimpleMenu simpleMenu)) continue;

                    val contents = simpleMenu.getContents();

                    if (contents instanceof RegionMemberMenu
                            || contents instanceof RegionMenu) {
                        menu.close();
                    }
                }
            }
        }

        CountablePermission.unRegisterRange("proxregions.limit", 1, 100);
    }

    @Override
    public @NonNull WorldGuard getWorldGuard() {
        return WorldGuard.getInstance();
    }

    @Override
    public @NonNull List<RegionBlock> getRegionBlocks() {
        return generalConfig.getRegionBlocks();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public @NonNull CreateResult createRegion(
            final @NonNull String name, final @NonNull Player player,
            final @NonNull RegionBlock block, final @NonNull Location location
    ) {
        val regionManager = getRegionManagerByPlayer(player.getWorld());
        if (regionManager.hasRegion(name))
            return CreateResult.ALREADY_EXISTS;

        val size = block.getSize();

        val blockX = location.getBlockX();
        val blockY = location.getBlockY();
        val blockZ = location.getBlockZ();

        if (isRegionExists(location.getWorld(), blockX, blockY, blockZ, size))
            return CreateResult.OVERLAP;

        val min = BlockVector3.at(blockX - size, blockY - size, blockZ - size);
        val max = BlockVector3.at(blockX + size, blockY + size, blockZ + size);

        val region = new ProtectedCuboidRegion(name, min, max);

        region.getOwners().addPlayer(player.getUniqueId());
        regionManager.addRegion(region);

        return CreateResult.SUCCESS;
    }

    private boolean isRegionExists(
            final @NonNull World world, final int blockX,
            final int blockY, final int blockZ, final int size
    ) {
        for (int x = blockX - size; x <= blockX + size; x++) {
            for (int y = blockY - size; y <= blockY + size; y++) {
                for (int z = blockZ - size; z <= blockZ + size; z++) {
                    val regions = getRegionManagerByPlayer(world)
                            .getApplicableRegions(BlockVector3.at(x, y, z));

                    if (!regions.getRegions().isEmpty()) return true;
                }
            }
        }

        return false;
    }

    @Override
    public int getRegionCount(@NonNull Player player) {
        return getRegionManagerByPlayer(player.getWorld()).getRegions().size();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public @NonNull RegionManager getRegionManagerByPlayer(final @NonNull World world) {
        val regionContainer = getWorldGuard().getPlatform().getRegionContainer();

        return regionContainer.get(BukkitAdapter.adapt(world));
    }

    @Override
    public boolean destroyRegion(final @NonNull Block block) {
        val regionManager = getRegionManagerByPlayer(block.getWorld());
        val location = block.getLocation();

        val blockX = location.getBlockX();
        val blockY = location.getBlockY();
        val blockZ = location.getBlockZ();

        val name = "rg_" + blockX + "_" + blockY + "_" + blockZ;
        if (!regionManager.hasRegion(name)) return false;

        regionManager.removeRegion(name);
        return true;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void addPlayerToRegion(
            final @NonNull Location location, final @NonNull String regionName, final @NonNull OfflinePlayer player
    ) {
        val regionManager = getWorldGuard().getPlatform().getRegionContainer()
                .get(BukkitAdapter.adapt(location.getWorld()));

        val region = regionManager.getRegion(regionName);

        if (region == null) return;

        region.getMembers().addPlayer(player.getUniqueId());
    }

    @Override
    public void removePlayerToRegion(final @NonNull ProtectedRegion region, @NonNull UUID playerUuid) {
        region.getMembers().removePlayer(playerUuid);
    }

    @Override
    public boolean isPlayerInRegion(@NonNull Player player) {
        return WorldGuardUtils.isPlayerInOwnedOrMemberRegion(player);
    }

    @Override
    public boolean isOwnerOfRegion(@NonNull String regionName, @NonNull Player player) {
        val regionManager = getRegionManagerByPlayer(player.getWorld());
        val region = regionManager.getRegion(regionName);

        if (region == null) return false;

        return region.getOwners().contains(player.getUniqueId());
    }

    Cache<Player, ProtectedRegion> playerCache;

    @Override
    public void addPlayerToCache(@NonNull Player player, @NonNull ProtectedRegion region) {
        playerCache.put(player, region);
    }

    @Override
    public void removePlayerFromCache(@NonNull Player player) {
        playerCache.invalidate(player);
    }

    @Override
    public ProtectedRegion isPlayerInCache(@NonNull Player player) {
        return playerCache.getIfPresent(player);
    }
}
