package net.proxworld.regions.menu;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import me.darkakyloff.api.menu.AbstractMenuContents;
import me.darkakyloff.api.menu.Menu;
import me.darkakyloff.api.menu.SimpleMenu;
import me.darkakyloff.api.menu.slot.Slot;
import me.darkakyloff.api.utils.ItemNewBuilder;
import net.proxworld.regions.RegionPlugin;
import net.proxworld.regions.config.GeneralConfig;
import net.proxworld.regions.config.locale.SimpleSingleMessage;
import net.proxworld.regions.event.PlayerAddMemberRequestEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class RegionMenu extends AbstractMenuContents {

    RegionPlugin plugin;
    GeneralConfig generalConfig;

    String regionName;
    ProtectedRegion region;

    public static Menu create(
            final @NonNull Player player, final @NonNull String regionName,
            final @NonNull ProtectedRegion region, final @NonNull RegionPlugin plugin
    ) {
        val title = plugin.getGeneralConfig().findMessage("REGION_MENU_TITLE")
                .orElse(SimpleSingleMessage.create("Not found")).asSingleLine();

        val contents = new RegionMenu(plugin, plugin.getGeneralConfig(), regionName, region);
        return SimpleMenu.create(contents, player, 6, title);
    }

    @Override
    public void render() {
        inventory.set(slot(2, 2), Slot.builder()
                .item(ItemNewBuilder.builder(Material.OAK_SIGN)
                        .setName(generalConfig.getMessage("REGION_MENU_ADD_MEMBER_ITEM")
                                .asSingleLine())
                        .setLore(generalConfig.getMessage("REGION_MENU_ADD_MEMBER_LORE")
                                .getLines())
                        .build())
                        .onClick(type -> {
                            inventory.close();

                            val cache = plugin.isPlayerInCache(player);
                            if (cache != null) return;

                            plugin.getServer()
                                    .getPluginManager().callEvent(PlayerAddMemberRequestEvent
                                            .create(player, region));
                            //plugin.addPlayerToCache(player, region);
                        })
                .build());

        val center = getCenterPoint(region.getMinimumPoint(), region.getMaximumPoint());

        inventory.set(slot(4, 2), Slot.builder()
                .item(ItemNewBuilder.builder(Material.PAPER)
                        .setName(generalConfig.getMessage("REGION_MENU_INFO_ITEM")
                                .asSingleLine())
                        .setLore(generalConfig.getMessage("REGION_MENU_INFO_LORE")
                                .format("name", regionName, "members", region.getMembers().size(),
                                        "owners", region.getOwners().size(),
                                        "x", center.getX(), "y", center.getY(), "z", center.getZ())
                                .getLines())
                        .build())
                .onClick(type -> inventory.close())
                .build());

        inventory.set(slot(6, 2), Slot.builder()
                .item(ItemNewBuilder.builder(Material.PLAYER_HEAD)
                        .setName(generalConfig.getMessage("REGION_MENU_MEMBERS_ITEM")
                                .asSingleLine())
                        .setLore(generalConfig.getMessage("REGION_MENU_MEMBERS_LORE")
                                .getLines())
                        .build())
                .onClick(type -> RegionMemberMenu.open(plugin, player, region))
                .build());
    }

    private @NonNull BlockVector3 getCenterPoint(
            final @NonNull BlockVector3 min, final @NonNull BlockVector3 max
    ) {
        val x = (min.getX() + max.getX()) / 2;
        val y = (min.getY() + max.getY()) / 2;
        val z = (min.getZ() + max.getZ()) / 2;

        return BlockVector3.at(x, y, z);
    }
}
