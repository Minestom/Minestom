package net.minestom.server.message;

import net.minestom.server.registry.Registry;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record ChatTypeImpl(
        @NotNull ChatTypeDecoration chat,
        @NotNull ChatTypeDecoration narration,
        @Nullable Registry.ChatTypeEntry registry
) implements ChatType {

    ChatTypeImpl {
        Check.notNull(chat, "missing chat");
        Check.notNull(narration, "missing narration");
    }

    ChatTypeImpl(@NotNull Registry.ChatTypeEntry registry) {
        this(registry.chat(), registry.narration(), registry);
    }

}
