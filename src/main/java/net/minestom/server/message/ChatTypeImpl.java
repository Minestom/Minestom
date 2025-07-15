package net.minestom.server.message;

import net.minestom.server.utils.validate.Check;

record ChatTypeImpl(
        ChatTypeDecoration chat,
        ChatTypeDecoration narration
) implements ChatType {

    ChatTypeImpl {
        Check.notNull(chat, "missing chat");
        Check.notNull(narration, "missing narration");
    }

}
