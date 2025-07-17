package net.minestom.server.message;

import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

record ChatTypeImpl(
        @NotNull ChatTypeDecoration chat,
        @NotNull ChatTypeDecoration narration
) implements ChatType {

    ChatTypeImpl {
        Check.notNull(chat, "missing chat");
        Check.notNull(narration, "missing narration");
    }

}
