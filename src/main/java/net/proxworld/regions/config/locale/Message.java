package net.proxworld.regions.config.locale;

import lombok.NonNull;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface Message {

    List<@NonNull String> getLines();

    @NonNull String getJoinedLines();

    @NonNull String asSingleLine();

    @NonNull Message format(final @NonNull Object... args);

    void send(final @NonNull CommandSender sender);

}
