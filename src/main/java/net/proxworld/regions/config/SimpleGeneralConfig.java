package net.proxworld.regions.config;

import com.sk89q.worldguard.protection.flags.StateFlag;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;
import me.darkakyloff.api.JavaMain;
import me.darkakyloff.api.utils.ConfigUtils;
import me.darkakyloff.api.utils.ItemNewBuilder;
import me.darkakyloff.api.utils.MessageUtils;
import net.proxworld.regions.RegionPlugin;
import net.proxworld.regions.block.RegionBlock;
import net.proxworld.regions.block.SimpleRegionBlock;
import net.proxworld.regions.config.hologram.HologramSettings;
import net.proxworld.regions.config.hologram.SimpleHologramSettings;
import net.proxworld.regions.config.limit.LimitSettings;
import net.proxworld.regions.config.limit.SimpleLimitSettings;
import net.proxworld.regions.config.locale.Message;
import net.proxworld.regions.config.locale.SimpleMultiMessage;
import net.proxworld.regions.config.locale.SimpleSingleMessage;
import net.proxworld.regions.config.locale.SingleMessage;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class SimpleGeneralConfig implements GeneralConfig {

    RegionPlugin plugin;

    public static @NonNull SimpleGeneralConfig create(final @NonNull RegionPlugin plugin) {
        return new SimpleGeneralConfig(plugin);
    }

    @NonFinal
    FileConfiguration configuration;

    @Override
    public @NonNull FileConfiguration getConfiguration() {
        return configuration;
    }

    @NonFinal
    List<String> allowedWorlds;

    @Override
    public @NonNull List<String> getAllowedWorlds() {
        return allowedWorlds;
    }

    @Override
    public void init() {
        ConfigUtils.createConfigFile(plugin, "settings.yml");
        configuration = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "settings.yml"));

        _loadLimits();
        _loadSettings();
        _loadRegionBlocks();
        _loadMessages();
    }

    private void _loadSettings() {
        val section = configuration.getConfigurationSection("settings");
        if (section == null) return;

        allowedWorlds = section.getStringList("allowed-worlds");

        regionNameFormat = SimpleSingleMessage.create(section.getString("region-name-format",
                "&7Region &e%region%"));

        isLightningOnCreate = section.getBoolean("lightning-on-create", true);

        isRegionExplosion = section.getBoolean("region-explosion", true);

        isPlaceRegionBlockByShift = section.getBoolean("place-region-block-by-shift", true);

        _loadHolograms(section);
    }

    @SuppressWarnings("ConstantConditions")
    private void _loadLimits() {
        val defaultLimit = configuration.getInt("limits.default-limit", 1);

        limitSettings = SimpleLimitSettings.create(defaultLimit);
    }

    private void _loadHolograms(final @NonNull ConfigurationSection settings) {
        val section = settings.getConfigurationSection("holograms");

        if (section == null) return;

        val enabled = section.getBoolean("enabled", true);
        if (!enabled) return;

        @SuppressWarnings("ConstantConditions")
        val height = section.getInt("height", 1);

        @SuppressWarnings("ConstantConditions")
        val lines = _loadMessage(section.get("lines"));

        hologramSettings = SimpleHologramSettings.create(height, lines);
    }

    @NonFinal
    @Unmodifiable
    List<RegionBlock> regionBlocks;

    @Override
    public @Unmodifiable @NonNull List<RegionBlock> getRegionBlocks() {
        return regionBlocks;
    }

    @Override
    public boolean isRegionBlock(@NonNull Material material) {
        return regionBlocks.stream()
                .anyMatch(regionBlock -> regionBlock.getItem().getType() == material);
    }

    @Override
    public boolean isRegionBlock(@NonNull ItemStack itemStack) {
        return regionBlocks.stream()
                .anyMatch(regionBlock -> regionBlock.getItem().isSimilar(itemStack));
    }

    @Override
    public @NonNull Optional<RegionBlock> findRegionBlock(@NonNull String key) {
        return regionBlocks.stream()
                .filter(regionBlock -> regionBlock.getKey().equalsIgnoreCase(key))
                .findFirst();
    }

    @Override
    public @NonNull Optional<RegionBlock> findRegionBlock(@NonNull ItemStack itemStack) {
        return regionBlocks.stream() // todo isSimular
                //     .filter(regionBlock -> itemStack.getType().name().endsWith(regionBlock.getItem().getType().name()))
                .findFirst();
    }

    @NonFinal
    boolean isLightningOnCreate;

    @Override
    public boolean isLightningOnCreate() {
        return isLightningOnCreate;
    }

    @NonFinal
    boolean isRegionExplosion;

    @Override
    public boolean isRegionExplosion() {
        return isRegionExplosion;
    }

    @NonFinal
    boolean isPlaceRegionBlockByShift;

    @Override
    public boolean isPlaceRegionBlockByShift() {
        return isPlaceRegionBlockByShift;
    }

    @NonFinal
    HologramSettings hologramSettings;

    @Override
    public @NonNull HologramSettings getHologramSettings() {
        return hologramSettings;
    }

    @NonFinal
    LimitSettings limitSettings;

    @Override
    public @NonNull LimitSettings getLimitSettings() {
        return limitSettings;
    }

    @NonFinal
    SingleMessage regionNameFormat;

    @Override
    public @NonNull SingleMessage getRegionNameFormat() {
        return regionNameFormat;
    }

    @SuppressWarnings("ConstantConditions")
    private void _loadRegionBlocks() {
        val section = configuration.getConfigurationSection("blocks");
        if (section == null) return;

        regionBlocks = section.getKeys(false).stream()
                .map(key -> _loadRegionBlock(key, section.getConfigurationSection(key)))
                .toList();
    }

    @SuppressWarnings("ConstantConditions")
    private @NonNull RegionBlock _loadRegionBlock(
            final @NonNull String key, final @NonNull ConfigurationSection section
    ) {
        val itemSection = section.getConfigurationSection("item");
        val size = section.getInt("size", 1);

        val flagRegistry = plugin.getWorldGuard()
                .getFlagRegistry();

       /* List<Pair<StateFlag, Boolean>> flags = section.getConfigurationSection("flags")
                .getKeys(false).stream()
                .map(flag -> ImmutablePair.of(flagRegistry.get(flag), section.getBoolean(flag, false)))
                .toList(); */

        return SimpleRegionBlock.create(
                key, _loadItem(itemSection), size, Collections.emptyList()
        );
    }

    @SuppressWarnings("ConstantConditions")
    private @NonNull ItemStack _loadItem(final @NonNull ConfigurationSection section) {
        val item = ItemNewBuilder.builder(Material.valueOf(section.getString("type", "STONE").toUpperCase()));

        if (section.contains("name")) {
            item.setName(section.getString("name"));
        }

        if (section.contains("lore")) {
            item.setLore(section.getStringList("lore")
                    .stream().map(MessageUtils::replaceColors)
                    .collect(Collectors.toList()));
        }

        return item.build();
    }

   /* private @NonNull List<Pair<StateFlag, StateFlag.State>> _loadFlags(final @NonNull ConfigurationSection section) {
        return section.getKeys(false).stream()
                .map(key -> new Pair<>(StateFlag.(key), section.getBoolean(key)))
                .toList();
    } */

    @NonFinal
    Map<String, Message> messages;

    @Override
    @Unmodifiable
    public @NonNull Map<String, Message> getMessages() {
        return messages;
    }

    @SuppressWarnings("ConstantConditions")
    private void _loadMessages() {
        val folder = JavaMain.getInstance().getDataFolder();
        val locale = YamlConfiguration.loadConfiguration(new File(folder, "messages.yml"));

        messages = locale.getKeys(false).stream()
                .filter(key -> key.startsWith("REGION_"))
                .collect(Collectors.toUnmodifiableMap(
                        key -> key,
                        value -> _loadMessage(locale.get(value))
                ));
    }

    private static final Message DEFAULT_VALUE = SimpleSingleMessage.create("Unknown message");

    @Override
    public @NonNull Optional<Message> findMessage(@NonNull String key) {
        return Optional.of(getMessage(key));
    }

    @Override
    public @NonNull Message getMessage(@NonNull String key) {
        return messages.getOrDefault(key, SimpleSingleMessage.create(key));
    }

    private @NonNull Message _loadMessage(final @NonNull Object obj) {
        if (obj instanceof String) {
            return SimpleSingleMessage.create(MessageUtils.replaceColors((String) obj));
        }

        if (obj instanceof List) {
            return SimpleMultiMessage.create(((List<?>) obj)
                    .stream().map(b -> MessageUtils.replaceColors((String) b))
                    .collect(Collectors.toList())
            );
        }

        return DEFAULT_VALUE;
    }

}