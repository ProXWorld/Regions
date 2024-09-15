package net.proxworld.regions.config.locale;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.bukkit.command.CommandSender;

import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SimpleMultiMessage implements MultiMessage {

    @NonNull List<String> lines;

    public static @NonNull SimpleMultiMessage create(final @NonNull List<String> lines) {
        return new SimpleMultiMessage(lines);
    }

    @Override
    public @NonNull List<@NonNull String> getLines() {
        return lines;
    }

    @Override
    public @NonNull String getJoinedLines() {
        return String.join("\n", lines);
    }

    @Override
    public @NonNull String asSingleLine() {
        return String.join(" ", lines);
    }

    @Override
    public @NonNull Message format(final @NonNull Object... args) {
        for (int i = 0; i < lines.size(); i++) {
            for (int j = 0; j < args.length; j += 2) {
                if (args.length <= j + 1) break;

                val value = args[j + 1].toString();
                lines.set(i, lines.get(i).replaceAll("\\{" + args[j] + "}", value));
            }
        }

        return SimpleMultiMessage.create(lines);
    }

    @Override
    public void send(final @NonNull CommandSender sender) {
        lines.forEach(sender::sendMessage);
    }

}
