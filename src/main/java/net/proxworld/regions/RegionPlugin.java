package net.proxworld.regions;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.*;
import lombok.experimental.FieldDefaults;
import me.darkakyloff.api.utils.WorldGuardUtils;
import net.proxworld.regions.block.RegionBlock;
import net.proxworld.regions.command.CommandManager;
import net.proxworld.regions.command.SimpleCommandManager;
import net.proxworld.regions.command.impl.AdminRegionCommand;
import net.proxworld.regions.command.impl.RegionsCommand;
import net.proxworld.regions.config.GeneralConfig;
import net.proxworld.regions.config.SimpleGeneralConfig;
import net.proxworld.regions.database.RegionsDao;
import net.proxworld.regions.hook.DecentHologramHook;
import net.proxworld.regions.hook.EmptyHologramHook;
import net.proxworld.regions.hook.HologramHook;
import net.proxworld.regions.logic.EventListener;
import net.proxworld.regions.model.result.CreateResult;
import net.proxworld.regions.player.PlayerManager;
import net.proxworld.regions.player.impl.PlayerManagerImpl;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jdbi.v3.cache.caffeine.CaffeineCachePlugin;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.async.JdbiExecutor;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class RegionPlugin extends JavaPlugin implements RegionsApi {

    GeneralConfig generalConfig;

    PlayerManager playerManager;

    CommandManager commandManager;

    HologramHook hologramHook;

    boolean testMode = false;

    @Override
    public void onLoad() {
        generalConfig = SimpleGeneralConfig.create(this);
        generalConfig.init();

        playerManager = PlayerManagerImpl.create();
        commandManager = SimpleCommandManager.create();

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

      //  rmSqliteFile();
      //  _loadSqlite();
       // dao = RegionsDaoSQLite.class;
    }

    private void _loadHolograms() {
        for (val world : generalConfig.getAllowedWorlds()) {
            val w = getServer().getWorld(world);

            if (w == null) continue;

            val regionManager = getRegionManagerByPlayer(w);

            for (val region : regionManager.getRegions().values()) {
                // getting x y z by rg_X_Y_Z format
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

                val displayName = Optional.ofNullable(b.getItem().getItemMeta())
                        .map(ItemMeta::getDisplayName)
                        .orElse(b.getItem().getType().name());

                val owner = region.getOwners().getUniqueIds().stream()
                        .map(getServer()::getOfflinePlayer)
                        .map(o -> o.getName())
                        .findFirst()
                        .orElse("Unknown");

                hologramHook.addHologram(
                        name, loc.clone().add(0.5, 2, 0.5),
                        generalConfig.getHologramSettings().getMessage()
                                .format("type", displayName, "size", b.getSize(), "owner", owner)
                                .getLines()
                );

               /* hologramHook.addHologram(
                        name, loc.clone().add(0.5, 2, 0.5),
                        generalConfig.getHologramSettings().getMessage()
                                .format("type", displayName, "size", rgBlock.getSize(), "owner", player.getName())
                                .getLines()
                )); */
            }
        }
    }

   /* Class<? extends RegionsDao> dao;

    Jdbi jdbi;

    JdbiExecutor executor;

    private void _loadSqlite() {
        val source = getHikariDataSource();

        jdbi = Jdbi.create(source);
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.installPlugin(new CaffeineCachePlugin());

        try (val input = getClass().getResourceAsStream("/schema.sql")) {
            if (input != null) {
                jdbi.useHandle((handle) -> handle.createScript(new String(input.readAllBytes()))
                        .execute());
            }
        } catch (final Exception e) {
            getLogger().severe("Failed to load schema.sql");
            return;
        }

        executor = JdbiExecutor.create(jdbi, Executors.newFixedThreadPool(2));
    }

    @NotNull
    @SneakyThrows
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private HikariDataSource getHikariDataSource() {
        val file = new File(getDataFolder(), "regions.db");

        if (!file.exists()) {
            file.createNewFile();
        }

        val config = new HikariConfig();
        config.setDriverClassName("org.sqlite.JDBC");

        config.setConnectionTestQuery("SELECT 1");
        config.setJdbcUrl("jdbc:sqlite:" + file);

        return new HikariDataSource(config);
    } */

    @Override
    public void onEnable() {
        getServer().getPluginManager()
                .registerEvents(EventListener.create(this, generalConfig), this);

        commandManager.registerCommand(RegionsCommand.create(this));
        commandManager.registerCommand(AdminRegionCommand.create(this));
    }

    @Override
    public void onDisable() {
        commandManager.unregisterCommands();
        hologramHook.removeHolograms();
    }

    @Override
    public @NonNull HologramHook getHologramHook() {
        return hologramHook;
    }

    @Override
    public @NonNull GeneralConfig getGeneralConfig() {
        return generalConfig;
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
    public @NonNull PlayerManager getPlayerManager() {
        return playerManager;
    }

    @Override
    public @NotNull CommandManager getCommandManager() {
        return commandManager;
    }

    @Override
    public @NonNull CreateResult createRegion(
            final @NonNull String name, final @NonNull Player player,
            final @NonNull RegionBlock block, final @NonNull Location location
    ) {
        val blockX = location.getBlockX();
        val blockY = location.getBlockY();
        val blockZ = location.getBlockZ();

        val regionManager = getRegionManagerByPlayer(player.getWorld());
        if (regionManager.hasRegion(name)) return CreateResult.ALREADY_EXISTS;

        val size = block.getSize();

        val min = BlockVector3.at(blockX - size, blockY - size, blockZ - size);
        val max = BlockVector3.at(blockX + size, blockY + size, blockZ + size);

        if (regionManager.getApplicableRegions(min).size() > 0) return CreateResult.DEFINED;
        if (regionManager.getApplicableRegions(max).size() > 0) return CreateResult.DEFINED;

        val region = new ProtectedCuboidRegion(name, min, max);

        region.getOwners().addPlayer(player.getUniqueId());
        regionManager.addRegion(region);

        return CreateResult.SUCCESS;
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
    public void removePlayerToRegion(final @NonNull ProtectedRegion region, @NonNull OfflinePlayer player) {
        region.getMembers().removePlayer(player.getUniqueId());
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
