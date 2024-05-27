package net.minestom.server.message;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record ChatTypeImpl(
        @NotNull NamespaceID namespace,
        @NotNull ChatTypeDecoration chat,
        @NotNull ChatTypeDecoration narration,
        @Nullable Registry.ChatTypeEntry registry
) implements ChatType {

    static final BinaryTagSerializer<ChatType> REGISTRY_NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                throw new UnsupportedOperationException("ChatType is read-only");
            },
            chatType -> CompoundBinaryTag.builder()
                    .put("chat", ChatTypeDecoration.NBT_TYPE.write(chatType.chat()))
                    .put("narration", ChatTypeDecoration.NBT_TYPE.write(chatType.narration()))
                    .build()
    );

    ChatTypeImpl {
        Check.notNull(namespace, "Namespace cannot be null");
        Check.notNull(chat, "missing chat: {0}", namespace);
        Check.notNull(narration, "missing narration: {0}", namespace);
    }

    ChatTypeImpl(@NotNull Registry.ChatTypeEntry registry) {
        this(registry.namespace(), registry.chat(), registry.narration(), registry);
    }

}
