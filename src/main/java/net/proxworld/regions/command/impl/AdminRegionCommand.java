package net.proxworld.regions.command.impl;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import me.darkakyloff.api.model.APICommand;
import me.darkakyloff.api.utils.PlayerUtils;
import me.darkakyloff.api.utils.SoundUtils;
import net.proxworld.regions.RegionPlugin;
import org.bukkit.command.CommandSender;

import javax.swing.*;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class AdminRegionCommand extends APICommand {

    RegionPlugin plugin;

    private AdminRegionCommand(final @NonNull RegionPlugin regionPlugin) {
        super("giveregion");

        this.plugin = regionPlugin;
    }

    public static @NonNull AdminRegionCommand create(
            final @NonNull RegionPlugin plugin
            ) {
        return new AdminRegionCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, String s, String[] strings) {
        if (!PlayerUtils.hasPermission(commandSender, "proxregions.admin"))
            return false;

        if (strings.length > 2) {
            SoundUtils.playErrorSound(commandSender);
            plugin.getGeneralConfig().findMessage("REGIONS_ADMIN_USE")
                    .ifPresent(message -> message.send(commandSender));
            return false;
        }

        val arg = strings[0];

        val block = plugin.getGeneralConfig().findRegionBlock(arg);

        if (block.isEmpty()) {
            SoundUtils.playErrorSound(commandSender);
            plugin.getGeneralConfig().findMessage("REGIONS_ADMIN_BLOCK_NOT_FOUND")
                    .ifPresent(message -> message.send(commandSender));
            return false;
        }

        val target = plugin.getServer().getPlayer(strings[1]);

        if (target == null) {
            SoundUtils.playErrorSound(commandSender);
            plugin.getGeneralConfig().findMessage("REGIONS_PLAYER_NOT_FOUND")
                    .ifPresent(message -> message.send(commandSender));
            return false;
        }

        val regionBlock = block.get();

        plugin.getGeneralConfig().findMessage("REGIONS_ADMIN_BLOCK")
                .ifPresent(message -> message
                        .format("block", regionBlock.getKey())
                        .send(commandSender));

        SoundUtils.playSuccessfulSound(commandSender);
        target.getInventory().addItem(regionBlock.getItem());

        return true;
    }
}
