package net.proxworld.regions.command;

import lombok.NonNull;
import me.darkakyloff.api.model.APICommand;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

public interface CommandManager {

    @Unmodifiable @NonNull Map<String, APICommand> getCommands();

    void registerCommand(final @NonNull APICommand command);

    void unregisterCommand(final @NonNull String commandName);

    void unregisterCommands();

}
