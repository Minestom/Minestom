package net.minestom.server.message;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.registry.StaticRegistryData;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record ChatTypeImpl(
        @NotNull ChatTypeDecoration chat,
        @NotNull ChatTypeDecoration narration,
        @Nullable StaticRegistryData.ChatTypeEntry registry
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
        Check.notNull(chat, "missing chat");
        Check.notNull(narration, "missing narration");
    }

    ChatTypeImpl(@NotNull StaticRegistryData.ChatTypeEntry registry) {
        this(registry.chat(), registry.narration(), registry);
    }

}
