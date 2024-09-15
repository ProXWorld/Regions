package net.proxworld.regions.logic;

import com.sk89q.worldedit.math.BlockVector3;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import me.darkakyloff.api.utils.SoundUtils;
import net.proxworld.regions.RegionPlugin;
import net.proxworld.regions.config.GeneralConfig;
import net.proxworld.regions.event.PlayerAddMemberRequestEvent;
import net.proxworld.regions.menu.RegionMemberMenu;
import net.proxworld.regions.menu.RegionMenu;
import net.proxworld.regions.model.result.CreateResult;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class EventListener implements Listener {

    RegionPlugin plugin;

    GeneralConfig generalConfig;

    public static @NonNull EventListener create(
            final @NonNull RegionPlugin plugin, final @NonNull GeneralConfig generalConfig
    ) {
        return new EventListener(plugin, generalConfig);
    }

    @EventHandler
    public void onRequestAddPlayer(final PlayerAddMemberRequestEvent event) {
        val player = event.getPlayer();

        val region = plugin.isPlayerInCache(player);

        if (region != null) return;
        plugin.addPlayerToCache(player, event.getRegion());

        plugin.getGeneralConfig().findMessage("REGION_ADD_MEMBER_REQUEST")
                .ifPresent(msg -> msg.send(player));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onAsyncChat(final AsyncPlayerChatEvent event) {
        val player = event.getPlayer();

        val region = plugin.isPlayerInCache(player);

        if (region == null) return;

        val message = event.getMessage();

        if (message.startsWith("/")) {
            return;
        }

        event.setCancelled(true);

        val regionManager = plugin.getRegionManagerByPlayer(player.getWorld());

        val vector = BlockVector3.at(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());

        if (!regionManager.getApplicableRegions(vector).getRegions().contains(region)) {
            generalConfig.findMessage("REGION_NOT_IN_REGION")
                    .ifPresent(msg -> msg.send(player));
            return;
        }

        val target = plugin.getServer().getPlayer(message);

        if (target == null) {
            generalConfig.findMessage("REGION_PLAYER_NOT_FOUND")
                    .ifPresent(msg -> msg.send(player));
            return;
        }

        if (region.getMembers().contains(target.getUniqueId())) {
            generalConfig.findMessage("REGION_ALREADY_MEMBER")
                    .ifPresent(msg -> msg.send(player));
            return;
        }

        if (region.getOwners().contains(target.getUniqueId())) {
            generalConfig.findMessage("REGION_ALREADY_OWNER")
                    .ifPresent(msg -> msg.send(player));
            return;
        }

        plugin.addPlayerToRegion(player.getLocation(), region.getId(), target);
        plugin.removePlayerFromCache(player);

        SoundUtils.playSuccessfulSound(player);
        SoundUtils.playSuccessfulSound(target.getPlayer());

        generalConfig.findMessage("REGION_ADD_MEMBER_SUCCESS")
                .ifPresent(msg -> msg.format("player", target.getName()).send(player));
        generalConfig.findMessage("REGION_ADD_MEMBER_SUCCESS_TARGET")
                .ifPresent(msg -> msg.format("id", region.getId(), "player", player.getName())
                        .send(target));

        RegionMemberMenu.open(plugin, player, region);
    }

    @EventHandler
    public void onPlaceRegion(final BlockPlaceEvent event) {
        val type = event.getBlock().getType();
        if (!generalConfig.isRegionBlock(type)) return;

        val player = event.getPlayer();
        if (!generalConfig.isPlaceRegionBlockByShift() && player.isSneaking()) return;

        val item = event.getItemInHand();
        if (!generalConfig.isRegionBlock(item)) return;
        if (!generalConfig.getAllowedWorlds()
                .contains(player.getWorld().getName())) return;

        val regionManager = plugin.getRegionManagerByPlayer(player.getWorld());

        val location = event.getBlock().getLocation();
        val vector = BlockVector3.at(location.getX(), location.getY(), location.getZ());

        if (regionManager.getApplicableRegions(vector).size() > 0) {
            event.setCancelled(true);
            generalConfig.findMessage("REGION_ALREADY_ANOTHER_REGION")
                    .ifPresent(message -> message.send(player));
            return;
        }

        generalConfig.findRegionBlock(item).ifPresent(rgBlock -> {
            val regionName = generateRegionName(location);

            val block = event.getBlockPlaced();
            val result = plugin.createRegion(regionName, player, rgBlock, location);

            if (result == CreateResult.ERROR) {
                event.setCancelled(true);
                generalConfig.findMessage("REGION_CREATE_ERROR")
                        .ifPresent(message -> message.send(player));
                return;
            }

            if (result == CreateResult.ALREADY_EXISTS) {
                event.setCancelled(true);
                generalConfig.findMessage("REGION_ALREADY_EXISTS")
                        .ifPresent(message -> message.send(player));
                return;
            }

            if (result == CreateResult.DEFINED) {
                event.setCancelled(true);
                generalConfig.findMessage("REGION_DEFINED_WITH_ANOTHER")
                        .ifPresent(message -> message.send(player));
                return;
            }

            val playerLimit = plugin.getGeneralConfig().getLimitSettings()
                    .getLimit(player);

            val regionsCount = plugin.getRegionCount(player);

            if (regionsCount >= playerLimit) {
                event.setCancelled(true);
                generalConfig.findMessage("REGION_LIMIT_REACHED")
                        .ifPresent(message -> message.send(player));
                return;
            }

            val centered = toCenter(location);

            if (generalConfig.isLightningOnCreate()) {
                block.getWorld().strikeLightningEffect(centered);
            }

            block.setMetadata("antiBreak", new FixedMetadataValue(plugin, regionName));

            val displayName = Optional.ofNullable(rgBlock.getItem().getItemMeta())
                    .map(ItemMeta::getDisplayName)
                    .orElse(rgBlock.getItem().getType().name());

            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
                    plugin.getHologramHook().addHologram(
                            regionName, centered.clone().add(0, 2, 0),
                            generalConfig.getHologramSettings().getMessage()
                                    .format("type", displayName, "size", rgBlock.getSize(), "owner", player.getName())
                                    .getLines()
                    ));

            generalConfig.findMessage("REGION_CREATED")
                    .ifPresent(message -> message.send(player));

            /*val future = plugin.getExecutor().withExtension(plugin.getDao(), (dao) -> {
                val regionCount = dao.getRegionCount(player.getUniqueId().toString());

                val limit = generalConfig.getLimitSettings().getLimit(player);
                if (regionCount >= limit) return DaoResult.LIMIT_REACHED;

                val result = dao.findRegionById(regionName);
                if (result != null) return DaoResult.ALREADY_EXISTS;

                val saveResult = dao.saveRegion(
                        regionName, "",
                        player.getUniqueId().toString(), player.getWorld().getName(),
                        location.getBlockX(), location.getBlockY(), location.getBlockZ()
                );



                return DaoResult.OK;
            }).exceptionally((t) -> DaoResult.FAILURE);

            future.thenAccept(region -> {
                if (region == null) return;

                if (region == DaoResult.LIMIT_REACHED) {
                    event.setCancelled(true);
                    generalConfig.findMessage("REGION_LIMIT_REACHED")
                            .ifPresent(message -> message.send(player));
                    return;
                }

                if (region == DaoResult.ALREADY_EXISTS) {
                    event.setCancelled(true);
                    generalConfig.findMessage("REGION_ALREADY_EXISTS")
                            .ifPresent(message -> message.send(player));
                    return;
                }

                if (region == DaoResult.FAILURE) {
                    event.setCancelled(true);
                    generalConfig.findMessage("REGION_CREATE_ERROR")
                            .ifPresent(message -> message.send(player));
                    return;
                }
            }); */
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractRegion(final PlayerInteractEvent event) {
        val player = event.getPlayer();
        val block = event.getClickedBlock();

        if (block == null) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        val type = block.getType();
        if (!generalConfig.isRegionBlock(type)) return;
        if (!block.hasMetadata("antiBreak")) return;
        if (!generalConfig.getAllowedWorlds().contains(player.getWorld().getName())) return;

        val regionManager = plugin.getRegionManagerByPlayer(player.getWorld());

        val vector = BlockVector3.at(block.getX(), block.getY(), block.getZ());

        val regions = regionManager.getApplicableRegions(vector)
                .getRegions().stream()
                .filter(region -> region.getId().startsWith("rg_"))
                .toList();

        if (regions.isEmpty()) {
            generalConfig.findMessage("REGION_NOT_IN_REGION")
                    .ifPresent(message -> message.send(player));
            SoundUtils.playErrorSound(player);

            return;
        }

        val region = regions.get(0);

        if (!region.getOwners().contains(player.getUniqueId())) {
            generalConfig.findMessage("REGION_NOT_OWNER")
                    .ifPresent(message -> message.send(player));
            SoundUtils.playErrorSound(player);

            return;
        }

        RegionMenu.create(player, region.getId(), region, plugin).open();
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(final BlockBurnEvent event) {
        val block = event.getBlock();

        if (checkRgBlock(block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockFade(final BlockFadeEvent event) {
        val block = event.getBlock();

        if (checkRgBlock(block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonPush(final BlockPistonExtendEvent event) {
        event.setCancelled(checkRgBlocks(event.getBlocks()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonPull(final BlockPistonRetractEvent event) {
        event.setCancelled(checkRgBlocks(event.getBlocks()));
    }

    private boolean checkRgBlock(final @NonNull Block block) {
        val type = block.getType();
        if (!generalConfig.isRegionBlock(type)) return false;

        return block.hasMetadata("antiBreak");
    }

    private boolean checkRgBlocks(final @NonNull List<Block> blocks) {
        return blocks.stream().anyMatch(this::checkRgBlock);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(final BlockExplodeEvent event) {
        val blocks = event.blockList();

        // plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
        blocks.forEach(block -> {
            val type = block.getType();

            if (!generalConfig.isRegionBlock(type)) return;
            if (!block.hasMetadata("antiBreak")) return;

            generalConfig.findRegionBlock(type).ifPresent(rgBlock -> {
                plugin.destroyRegion(block);
                block.removeMetadata("antiBreak", plugin);

                plugin.getHologramHook()
                        .removeHologram(generateRegionName(block.getLocation()));
                block.getWorld().dropItem(block.getLocation(), rgBlock.getItem());

                event.blockList().remove(block);
            });
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBreakRegion(final BlockBreakEvent event) {
        val block = event.getBlock();

        val type = block.getType();
        if (!generalConfig.isRegionBlock(type)) return;
        if (!block.hasMetadata("antiBreak")) return;

        val player = event.getPlayer();
        if (!generalConfig.getAllowedWorlds().contains(player.getWorld().getName())) return;

        generalConfig.findRegionBlock(type).ifPresent(rgBlock -> {
            //plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean success = plugin.destroyRegion(block);

            if (!success) {
                generalConfig.findMessage("REGION_DESTROY_ERROR")
                        .ifPresent(message -> message.send(player));
                return;
            }

            block.removeMetadata("antiBreak", plugin);

            plugin.getHologramHook()
                    .removeHologram(generateRegionName(event.getBlock().getLocation()));
            generalConfig.findMessage("REGION_DESTROYED")
                    .ifPresent(message -> message.send(player));

            event.setDropItems(false);
            player.getInventory().addItem(rgBlock.getItem());
            //    });
        });
    }

    private @NonNull Location toCenter(final @NonNull Location location) {
        return location.clone().add(0.5, 0, 0.5);
    }

    private @NonNull String generateRegionName(final @NonNull Location location) {
        val blockX = location.getBlockX();
        val blockY = location.getBlockY();
        val blockZ = location.getBlockZ();

        return "rg_" + blockX + "_" + blockY + "_" + blockZ;
    }

}
