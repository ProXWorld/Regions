package net.proxworld.regions.command.impl;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import me.darkakyloff.api.model.APICommand;
import me.darkakyloff.api.utils.PlayerUtils;
import me.darkakyloff.api.utils.SoundUtils;
import net.proxworld.regions.RegionPlugin;
import net.proxworld.regions.menu.RegionMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class RegionsCommand extends APICommand implements TabCompleter {

    RegionPlugin plugin;

    private RegionsCommand(
            final @NonNull RegionPlugin plugin
    ) {
        super("ps");

        this.plugin = plugin;
    }

    public static @NotNull RegionsCommand create(
            final @NotNull RegionPlugin plugin
    ) {
        return new RegionsCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, String s, String[] strings) {
        if (!PlayerUtils.instanceOfPlayer(commandSender)) return false;

        val player = (Player) commandSender;
        val playerLoc = player.getLocation();

        val regionManager = plugin.getRegionManagerByPlayer(player.getWorld());

        val vector = BlockVector3.at(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ());

        val regions = regionManager.getApplicableRegions(vector)
                .getRegions().stream()
                .filter(region -> region.getId().startsWith("rg_"))
                .toList();

        if (regions.isEmpty()) {
            plugin.getGeneralConfig().findMessage("REGION_COMMAND_NOT_IN_REGION")
                    .ifPresent(message -> message.send(player));
            SoundUtils.playErrorSound(player);

            return false;
        }

        val region = regions.get(0);

        if (!region.getOwners().contains(player.getUniqueId())) {
            plugin.getGeneralConfig().findMessage("REGION_COMMAND_NOT_OWNER")
                    .ifPresent(message -> message.send(player));
            SoundUtils.playErrorSound(player);

            return false;
        }

        RegionMenu.create(player, region.getId(), region, plugin).open();
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!PlayerUtils.instanceOfPlayer(commandSender)) return null;

        if (strings.length == 1) {
            return List.of("add", "remove");
        }

        if (strings.length > 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList();
        }

        return List.of();
    }
}
