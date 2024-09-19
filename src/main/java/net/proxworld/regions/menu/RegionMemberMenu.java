package net.proxworld.regions.menu;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.util.profile.Profile;
import com.sk89q.worldguard.util.profile.cache.ProfileCache;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import me.darkakyloff.api.menu.PaginatedMenuContents;
import me.darkakyloff.api.menu.SimpleMenu;
import me.darkakyloff.api.menu.slot.Slot;
import me.darkakyloff.api.utils.ItemNewBuilder;
import net.proxworld.regions.RegionPlugin;
import net.proxworld.regions.config.GeneralConfig;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class RegionMemberMenu extends PaginatedMenuContents<Pair<UUID, RegionMemberMenu.PlayerRole>> {

    RegionPlugin plugin;

    GeneralConfig generalConfig;

    ProfileCache profileCache;

    ProtectedRegion region;

    public static void open(
            final @NonNull RegionPlugin plugin,
            final @NonNull Player player,
            final @NonNull ProtectedRegion region
    ) {
        val cache = plugin.getWorldGuard().getProfileCache();

        val title = plugin.getGeneralConfig().getMessage("REGION_MENU_MEMBERS_TITLE").asSingleLine();

        val menu = SimpleMenu.create(new RegionMemberMenu(plugin, plugin.getGeneralConfig(), cache, region), player, 6, title);

        menu.runUpdater(20);
        menu.open();
    }

    @Override
    protected void init() {
        super.init();

        addRect(1, 1, 7, 3);
    }

    @Override
    public void render() {
        super.render();

        renderButtons(slot(2, 4), slot(6, 4));
        renderBackItem(slot(4, 5), () ->
                RegionMenu.create(player, region.getId(), region, plugin).open());
    }

    @Override
    protected List<Pair<UUID, PlayerRole>> getItems() {
        val players = new ArrayList<Pair<UUID, RegionMemberMenu.PlayerRole>>();

        players.addAll(region.getOwners().getPlayerDomain().getUniqueIds().stream()
                .map(p -> ImmutablePair.of(p, RegionMemberMenu.PlayerRole.OWNER))
                .toList());

        players.addAll(region.getMembers().getPlayerDomain().getUniqueIds().stream()
                .map(p -> ImmutablePair.of(p, RegionMemberMenu.PlayerRole.MEMBER))
                .toList());

        return players;
    }

    @Override
    protected void renderItem(int slot, Pair<UUID, RegionMemberMenu.PlayerRole> pair) {
        val target = pair.getKey();

        getProfileNameByCache(target).ifPresent(profile -> {
            val role = pair.getValue();

            inventory.set(slot, Slot.builder()
                    .item(ItemNewBuilder.builder(Material.PLAYER_HEAD)
                            .setName(generalConfig.getMessage("REGION_MENU_MEMBER_NAME")
                                    .format("player", profile.getName())
                                    .asSingleLine())
                            .setLore(generalConfig.getMessage("REGION_MENU_ROLE_" + role.name())
                                    .getLines())
                            .build())
                    .onClick(type -> {
                        if (role == PlayerRole.OWNER) return;

                        if (type.isRightClick()) {
                            plugin.removePlayerToRegion(region, target);
                            render();
                        }
                    })
                    .build());
        });
    }

    private @NonNull Optional<Profile> getProfileNameByCache(final @NonNull UUID uuid) {
        return Optional.ofNullable(profileCache.getIfPresent(uuid));
    }

    protected enum PlayerRole {

        OWNER, MEMBER

    }
}
