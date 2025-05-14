package net.minestom.server.dialog;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record DialogMetadata(
        @NotNull Component title,
        @Nullable Component externalTitle,
        boolean canCloseWithEscape
//        @NotNull List<DialogBody> body
) {
}
