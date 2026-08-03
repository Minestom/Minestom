package net.minestom.server.message;

import java.util.Objects;

value record ChatTypeImpl(
        ChatTypeDecoration chat,
        ChatTypeDecoration narration
) implements ChatType {

    ChatTypeImpl {
        Objects.requireNonNull(chat, "missing chat");
        Objects.requireNonNull(narration, "missing narration");
    }

}
