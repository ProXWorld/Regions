package net.proxworld.regions.logic;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import me.darkakyloff.addon.commands.ec.EcManager;
import me.darkakyloff.api.utils.SoundUtils;
import net.proxworld.regions.RegionPlugin;
import net.proxworld.regions.config.GeneralConfig;
import net.proxworld.regions.event.PlayerAddMemberRequestEvent;
import net.proxworld.regions.menu.RegionMemberMenu;
import net.proxworld.regions.menu.RegionMenu;
import net.proxworld.regions.model.result.CreateResult;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
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

    @SuppressWarnings("deprecation")
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
        SoundUtils.playSuccessfulSound(target);

        generalConfig.findMessage("REGION_ADD_MEMBER_SUCCESS")
                .ifPresent(msg -> msg.format("player", target.getName())
                        .send(player));
        generalConfig.findMessage("REGION_ADD_MEMBER_SUCCESS_TARGET")
                .ifPresent(msg -> msg.format("id", region.getId())
                        .format("player", player.getName())
                        .send(target));

        RegionMemberMenu.open(plugin, player, region);
    }

    private static final Particle.DustOptions DUST_OPTIONS
            = new Particle.DustOptions(Color.fromRGB(241, 166, 33), 1);

    @EventHandler
    public void onPlaceRegion(final BlockPlaceEvent event) {
        val block = event.getBlock();
        if (!generalConfig.isRegionBlock(block.getType())) return;

        val player = event.getPlayer();
        if (!generalConfig.isPlaceRegionBlockByShift() && player.isSneaking()) return;

        val item = event.getItemInHand();
        if (!generalConfig.isRegionBlock(item)) return;
        if (!generalConfig.getAllowedWorlds()
                .contains(player.getWorld().getName())) return;

        val regionManager = plugin.getRegionManagerByPlayer(player.getWorld());

        val location = block.getLocation();
        val vector = BlockVector3.at(location.getX(), location.getY(), location.getZ());

        if (regionManager.getApplicableRegions(vector).size() > 0) {
            event.setCancelled(true);
            generalConfig.findMessage("REGION_ALREADY_ANOTHER_REGION")
                    .ifPresent(message -> message.send(player));
            return;
        }

        generalConfig.findRegionBlock(item).ifPresent(rgBlock -> {
            val playerLimit = plugin.getGeneralConfig()
                    .getLimitSettings().getLimit(player);
            val regionsCount = plugin.getRegionCount(player);

            if (regionsCount >= playerLimit) {
                event.setCancelled(true);
                generalConfig.findMessage("REGION_LIMIT_REACHED")
                        .ifPresent(message -> message.send(player));
                return;
            }

            val regionName = generateRegionName(location);
            val result = plugin.createRegion(regionName, player, rgBlock, location);

            if (result == CreateResult.ALREADY_EXISTS) {
                event.setCancelled(true);
                generalConfig.findMessage("REGION_ALREADY_EXISTS")
                        .ifPresent(message -> message.send(player));
                return;
            }

            if (result == CreateResult.OVERLAP) {
                event.setCancelled(true);
                generalConfig.findMessage("REGION_OVERLAP_WITH_ANOTHER")
                        .ifPresent(message -> message.send(player));
                return;
            }

            val centered = toCenter(location);

            block.setMetadata("antiBreak", new FixedMetadataValue(plugin, regionName));

            @SuppressWarnings("deprecation")
            val displayName = Optional.ofNullable(rgBlock.getItem().getItemMeta())
                    .map(ItemMeta::getDisplayName)
                    .orElse(rgBlock.getItem().getType().name());

            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
                    plugin.getHologramHook().addHologram(
                            regionName, centered.clone().add(0, 2, 0),
                            generalConfig.getHologramSettings().getMessage()
                                    .format("type", displayName)
                                    .format("size", rgBlock.getSize())
                                    .format("owner", player.getName())
                                    .getLines()
                    ));

            playParticles(block);
            SoundUtils.playSuccessfulSound(player);

            generalConfig.findMessage("REGION_CREATED")
                    .ifPresent(message -> message.send(player));
        });
    }

    private void playParticles(
            final @NonNull Block block
    ) {
        val world = block.getWorld();

        val location = block.getLocation();

        world.playEffect(location, Effect.STEP_SOUND, block.getType());
        world.spawnParticle(Particle.REDSTONE, toCenter(location)
                .clone().add(0, 1.5, 0), 1, DUST_OPTIONS);

        // playing
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

        event.setCancelled(true);

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

        ProtectedRegion protectedRegion = null;

        for (val region : regions) {
            if (!region.getOwners().contains(player.getUniqueId())) {
                generalConfig.findMessage("REGION_NOT_OWNER")
                        .ifPresent(message -> message.send(player));
                SoundUtils.playErrorSound(player);
                return;
            }

            protectedRegion = region;
        }

        RegionMenu.create(player, protectedRegion.getId(), protectedRegion, plugin).open();
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityExplode(final EntityExplodeEvent event) {
        val world = event.getLocation().getWorld();
        if (world == null) return;
        if (!generalConfig.getAllowedWorlds().contains(world.getName())) return;

        List<Block> blocks = new ArrayList<>(event.blockList());
        event.blockList().clear();
        event.blockList().addAll(checkExplode(blocks));
    }

    private @NonNull List<Block> checkExplode(final @NonNull List<Block> blocks) {
        val remove = new ArrayList<>(blocks);

        for (val block : blocks) {
            val type = block.getType();

            if (!generalConfig.isRegionBlock(type)) continue;
            if (!block.hasMetadata("antiBreak")) continue;

            val opt = generalConfig.findRegionBlock(type);
            if (opt.isEmpty()) continue;

            val rgBlock = opt.get();
            remove.remove(block);
            if (!rgBlock.isExploding()) continue;

            plugin.destroyRegion(block);
            block.removeMetadata("antiBreak", plugin);

            val loc = block.getLocation();

            plugin.getHologramHook()
                    .removeHologram(generateRegionName(loc));

            block.setType(Material.AIR);
            block.getWorld().dropItem(loc, rgBlock.getItem());
        }

        return remove;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(final BlockExplodeEvent event) {
        val block = event.getBlock();
        val world = block.getWorld();
        if (!generalConfig.getAllowedWorlds().contains(world.getName())) return;

        // plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
        List<Block> blocks = new ArrayList<>(event.blockList());
        event.blockList().clear();
        event.blockList().addAll(checkExplode(blocks));
        // );
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBreakRegion(final BlockBreakEvent event) {
        val block = event.getBlock();

        val type = block.getType();

        if (!generalConfig.isRegionBlock(type)) return;
        if (!block.hasMetadata("antiBreak")) return;

        val player = event.getPlayer();
        if (!generalConfig.getAllowedWorlds().contains(player.getWorld().getName())) return;

        val regionManager = plugin.getRegionManagerByPlayer(player.getWorld());

        val vector = BlockVector3.at(block.getX(), block.getY(), block.getZ());

        val regions = regionManager.getApplicableRegions(vector)
                .getRegions().stream()
                .filter(region -> region.getId().startsWith("rg_"))
                .toList();

        event.setCancelled(true);

        if (regions.isEmpty()) {
            generalConfig.findMessage("REGION_NOT_IN_REGION")
                    .ifPresent(message -> message.send(player));
            SoundUtils.playErrorSound(player);

            return;
        }

        for (val region : regions) {
            if (!region.getOwners().contains(player.getUniqueId())) {
                generalConfig.findMessage("REGION_NOT_OWNER")
                        .ifPresent(message -> message.send(player));
                SoundUtils.playErrorSound(player);

                return;
            }
        }

        generalConfig.findRegionBlock(type).ifPresent(rgBlock -> {
            //plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean success = plugin.destroyRegion(block);

            if (!success) {
                generalConfig.findMessage("REGION_DESTROY_ERROR")
                        .ifPresent(message -> message.send(player));
                return;
            }

            val world = block.getWorld();

            for (val region : regions) {
                actionEnderChest(world, region, region.getMembers());
                actionEnderChest(world, region, region.getOwners());
            }

            event.setDropItems(false);
            event.setCancelled(true);

            block.setType(Material.AIR);
            block.removeMetadata("antiBreak", plugin);

            plugin.getHologramHook()
                    .removeHologram(generateRegionName(event.getBlock().getLocation()));
            generalConfig.findMessage("REGION_DESTROYED")
                    .ifPresent(message -> message.send(player));

            plugin.removePlayerFromCache(player);

            player.getInventory().addItem(rgBlock.getItem());
            //    });
        });
    }

    private void actionEnderChest(
            final @NonNull World world, final @NonNull ProtectedRegion region, final @NonNull DefaultDomain domain
    ) {
        val uuids = domain.getUniqueIds();

        for (val uuid : uuids) {
            val enderChest = EcManager.chestsMap.get(uuid.toString());

            if (enderChest != null) {
                if (!enderChest.getWorld().getName()
                        .equalsIgnoreCase(world.getName())) continue;

                val blocks = getBlocks(
                        world, region.getMinimumPoint(), region.getMaximumPoint()
                );

                for (val block : blocks) {
                    val loc = block.getLocation();
                    if (!loc.equals(enderChest)) continue;

                    EcManager.breakChest(block);

                    block.breakNaturally(new ItemStack(Material.ENDER_CHEST));

                    val item = generalConfig.getEnderChestItem();

                    if (item != null) {
                        block.getWorld().dropItem(loc, generalConfig.getEnderChestItem());
                    }
                }
            }
        }
    }

    private @NonNull List<Block> getBlocks(
            final @NonNull World world, final @NonNull BlockVector3 min, final @NonNull BlockVector3 max
    ) {
        val blocks = new ArrayList<Block>();

        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    blocks.add(world.getBlockAt(x, y, z));
                }
            }
        }

        return blocks;
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
