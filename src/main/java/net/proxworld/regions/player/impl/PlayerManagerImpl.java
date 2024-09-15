package net.proxworld.regions.player.impl;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.proxworld.regions.player.PlayerManager;
import net.proxworld.regions.player.RegionPlayer;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class PlayerManagerImpl implements PlayerManager {

    Object2ObjectOpenHashMap<UUID, RegionPlayer> players;

    LoadingCache<@NonNull UUID, @NonNull RegionPlayer> offline;

    public PlayerManagerImpl() {
        this.players = new Object2ObjectOpenHashMap<>();

        this.offline = Caffeine.newBuilder()
                .build(uuid -> RegionPlayerImpl.create(uuid, true));
    }

    public static @NonNull PlayerManagerImpl create() {
        return new PlayerManagerImpl();
    }

    @Override
    public boolean hasPlayer(final @NotNull UUID uuid) {
        return players.containsKey(uuid);
    }

    @Override
    public void addPlayer(final @NotNull UUID uuid) {
     /*   if (players.containsKey(uuid)) {
            Bukkit.getLogger().info(MessageFormat.format(
                    "§c§oИгрок {0} уже существует в кэше", uuid.toString()));
            return;
        }

        if (offline.get(uuid) != null) {
            players.put(uuid, offline.get(uuid));
            return;
        }

        players.put(uuid, BuyerPlayerImpl.create(uuid)); */
    }

    @Override
    public void removePlayer(@NotNull UUID uuid) {
        val player = players.remove(uuid);

        if (player == null) {
            Bukkit.getLogger().info(MessageFormat.format(
                    "§c§oИгрок {0} не найден в кэше", uuid.toString()));
            return;
        }

        offline.put(uuid, player);
    }

    @Override
    public Optional<RegionPlayer> getOrCreate(final @NotNull UUID uuid) {
        return Optional.empty();
        // return Optional.of(players.computeIfAbsent(uuid, RegionPlayerImpl::create));
    }

    @Override
    public Collection<RegionPlayer> getPlayers() {
        return players.values();
    }
}

