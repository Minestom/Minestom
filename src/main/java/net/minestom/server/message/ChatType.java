package net.minestom.server.message;

import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.DynamicRegistryImpl;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface ChatType extends ProtocolObject, ChatTypes permits ChatTypeImpl {
    @NotNull BinaryTagSerializer<ChatType> NBT_TYPE = ChatTypeImpl.NBT_TYPE;

    static @NotNull ChatType create(
            @NotNull NamespaceID namespace,
            @NotNull ChatTypeDecoration chat,
            @NotNull ChatTypeDecoration narration
    ) {
        return new ChatTypeImpl(namespace, chat, narration, null);
    }

    static @NotNull Builder builder(@NotNull String namespace) {
        return builder(NamespaceID.from(namespace));
    }

    static @NotNull Builder builder(@NotNull NamespaceID namespace) {
        return new Builder(namespace);
    }

    /**
     * <p>Creates a new registry for chat types, loading the vanilla chat types.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static @NotNull DynamicRegistry<ChatType> createDefaultRegistry() {
        return new DynamicRegistryImpl<>(
                "minecraft:chat_type", NBT_TYPE, Registry.Resource.CHAT_TYPES,
                (namespace, props) -> new ChatTypeImpl(Registry.chatType(namespace, props))
        );
    }

    @NotNull ChatTypeDecoration chat();

    @NotNull ChatTypeDecoration narration();

    @Override
    @Nullable Registry.ChatTypeEntry registry();

    final class Builder {
        private final NamespaceID namespace;
        private ChatTypeDecoration chat;
        private ChatTypeDecoration narration;

        public Builder(@NotNull NamespaceID namespace) {
            this.namespace = namespace;
        }

        public Builder chat(@NotNull ChatTypeDecoration chat) {
            this.chat = chat;
            return this;
        }

        public Builder narration(@NotNull ChatTypeDecoration narration) {
            this.narration = narration;
            return this;
        }

        public @NotNull ChatType build() {
            return new ChatTypeImpl(namespace, chat, narration, null);
        }
    }
}
