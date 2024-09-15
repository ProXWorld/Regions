package net.proxworld.regions.config.locale;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SimpleSingleMessage implements SingleMessage {

    @NonNull
    @NonFinal
    String message;

    public static @NonNull SimpleSingleMessage create(final @NonNull String message) {
        return new SimpleSingleMessage(message);
    }

    @Override
    public List<@NonNull String> getLines() {
        return Collections.singletonList(message);
    }

    @Override
    public @NonNull String getJoinedLines() {
        return message;
    }

    @Override
    public @NonNull String asSingleLine() {
        return message;
    }

    @Override
    public @NonNull Message format(final @NonNull Object... args) {
        // format: PLACEHOLDER {x} -> args[0]
        for (int i = 0; i < args.length; i += 2) {
            if (args.length <= i + 1) break;

            message = message.replace("{" + args[i] + "}",
                    args[i + 1].toString());
        }

        return SimpleSingleMessage.create(message);
    }

    @Override
    public void send(final @NonNull CommandSender sender) {
        sender.sendMessage(message);
    }

}
