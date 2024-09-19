package net.proxworld.regions.command.impl;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import me.darkakyloff.api.CastManager;
import me.darkakyloff.api.model.APICommand;
import me.darkakyloff.api.utils.PlayerUtils;
import me.darkakyloff.api.utils.SoundUtils;
import net.proxworld.regions.RegionPlugin;
import net.proxworld.regions.block.RegionBlock;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class GiveRegionCommand extends APICommand {

    RegionPlugin plugin;

    @SuppressWarnings("ConstantConditions")
    private GiveRegionCommand(final @NonNull RegionPlugin regionPlugin) {
        super("giveregion");

        this.plugin = regionPlugin;
    }

    public static @NonNull GiveRegionCommand create(
            final @NonNull RegionPlugin plugin
            ) {
        return new GiveRegionCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, String label, String[] strings) {
        if (!PlayerUtils.hasPermission(commandSender, "proxregions.admin"))
            return false;

        if (strings.length < 2) {
            SoundUtils.playErrorSound(commandSender);
            plugin.getGeneralConfig().findMessage("REGION_ADMIN_GIVE_USE")
                    .ifPresent(message -> message
                            .format("cmd", label).send(commandSender));
            return false;
        }

        val target = plugin.getServer().getPlayer(strings[0]);

        if (target == null) {
            SoundUtils.playErrorSound(commandSender);
            plugin.getGeneralConfig().findMessage("REGION_PLAYER_NOT_FOUND")
                    .ifPresent(message -> message.send(commandSender));
            return false;
        }

        val arg = strings[1];

        val block = plugin.getGeneralConfig().findRegionBlock(arg);

        if (block.isEmpty()) {
            SoundUtils.playErrorSound(commandSender);
            plugin.getGeneralConfig().findMessage("REGION_ADMIN_GIVE_BLOCK_NOT_FOUND")
                    .ifPresent(message -> message
                            .format("block", arg).send(commandSender));
            return false;
        }

        val regionBlock = block.get();
        int amount = 1;

        if (strings.length > 2) {
            if (!CastManager.catToInt(commandSender, strings[2])) return false;
                     /*   try {
                amount = Integer.parseInt(strings[2]);
            } catch (final NumberFormatException e) {
                SoundUtils.playErrorSound(commandSender);
                plugin.getGeneralConfig().findMessage("REGIONS_ADMIN_GIVE_AMOUNT_NOT_NUMBER")
                        .ifPresent(message -> message
                                .format("arg", strings[2]).send(commandSender));
                return false;
            } */

            val maxStack = regionBlock.getItem()
                    .getMaxStackSize();

            if (Integer.parseInt(strings[2]) > maxStack) {
                SoundUtils.playErrorSound(commandSender);
                plugin.getGeneralConfig().findMessage("REGION_ADMIN_GIVE_MAX_STACK")
                        .ifPresent(message -> message
                                .format("block", block.get().getKey())
                                .format("max", maxStack)
                                .send(commandSender));
                return false;
            }

            amount = Integer.parseInt(strings[2]);
        }

        int temp = amount;

        plugin.getGeneralConfig().findMessage("REGION_ADMIN_BLOCK_GIVEN")
                .ifPresent(message -> message
                        .format("player", target.getName())
                        .format("block", regionBlock.getKey())
                        .format("amount", temp)
                        .send(commandSender));

        SoundUtils.playSuccessfulSound(commandSender);

        val item = regionBlock.getItem().clone();
        if (amount > 1) item.setAmount(amount);

        target.getInventory().addItem(item);
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        val length = args.length;

        return switch (length) {
            case 1 -> plugin.getServer().getOnlinePlayers().stream()
                    .map(HumanEntity::getName)
                    .toList();
            case 2 -> plugin.getGeneralConfig().getRegionBlocks().stream()
                    .map(RegionBlock::getKey)
                    .toList();
            case 3 -> List.of("1", "16", "32", "64");
            default -> new ArrayList<>();
        };
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args, @Nullable Location location) throws IllegalArgumentException {
        return tabComplete(sender, alias, args);
    }
}
