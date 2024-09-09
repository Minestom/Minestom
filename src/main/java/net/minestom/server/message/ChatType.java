package net.minestom.server.message;

import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public sealed interface ChatType extends ProtocolObject, ChatTypes permits ChatTypeImpl {

    static @NotNull ChatType create(
            NamespaceID namespace,
            @NotNull ChatTypeDecoration chat,
            @NotNull ChatTypeDecoration narration
    ) {
        return new ChatTypeImpl(namespace, chat, narration, null);
    }

    static @NotNull Builder builder(NamespaceID namespace) {
        return new Builder(namespace);
    }


    /**
     * <p>Creates a new registry for chat types, loading the vanilla chat types.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static @NotNull DynamicRegistry<ChatType> createDefaultRegistry() {
        final List<ChatType> chatTypes = Registry.loadRegistry(Registry.Resource.CHAT_TYPES, Registry.ChatTypeEntry::new).stream()
                .<ChatType>map(ChatTypeImpl::new).toList();
        return DynamicRegistry.create("minecraft:chat_type", ChatTypeImpl.REGISTRY_NBT_TYPE, chatTypes);
    }

    @NotNull ChatTypeDecoration chat();

    @NotNull ChatTypeDecoration narration();

    @Override
    @Nullable Registry.ChatTypeEntry registry();

    final class Builder {
        private final NamespaceID namespace;
        private ChatTypeDecoration chat;
        private ChatTypeDecoration narration;

        private Builder(NamespaceID namespace) {
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
