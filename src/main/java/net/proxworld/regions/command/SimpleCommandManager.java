package net.proxworld.regions.command;

import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import me.darkakyloff.api.model.APICommand;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleCommandManager implements CommandManager {

    public static @NonNull SimpleCommandManager create() {
        return new SimpleCommandManager();
    }

    Map<String, APICommand> commands = Maps.newHashMap();

    @Override
    public @Unmodifiable @NonNull Map<String, APICommand> getCommands() {
        return Collections.unmodifiableMap(commands);
    }

    @Override
    public void registerCommand(@NonNull APICommand command) {
        commands.put(command.getName(), command);
        me.darkakyloff.api.model.CommandManager.registerCommand(command.getName(), command);

        log.info("Registered command: {}", command.getName());
    }

    @Override
    public void unregisterCommand(@NonNull String commandName) {
        commands.remove(commandName);
        log.info("Unregistered command: {}", commandName);
    }

    @Override
    public void unregisterCommands() {
        for (val command : new ArrayList<>(commands.values())) {
            unregisterCommand(command.getName());

            me.darkakyloff.api.model.CommandManager.unregisterCommand(command.getName(), command);
        }
    }
}
