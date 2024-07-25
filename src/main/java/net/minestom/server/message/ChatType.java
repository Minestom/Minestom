package net.minestom.server.message;

import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface ChatType extends ProtocolObject, ChatTypes permits ChatTypeImpl {

    static @NotNull ChatType create(
            @NotNull ChatTypeDecoration chat,
            @NotNull ChatTypeDecoration narration
    ) {
        return new ChatTypeImpl(chat, narration, null);
    }

    static @NotNull Builder builder() {
        return new Builder();
    }


    /**
     * <p>Creates a new registry for chat types, loading the vanilla chat types.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static @NotNull DynamicRegistry<ChatType> createDefaultRegistry() {
        return DynamicRegistry.create(
                "minecraft:chat_type", ChatTypeImpl.REGISTRY_NBT_TYPE, Registry.Resource.CHAT_TYPES,
                (key, props) -> new ChatTypeImpl(Registry.chatType(key, props))
        );
    }

    @NotNull ChatTypeDecoration chat();

    @NotNull ChatTypeDecoration narration();

    @Override
    @Nullable Registry.ChatTypeEntry registry();

    final class Builder {
        private ChatTypeDecoration chat;
        private ChatTypeDecoration narration;

        private Builder() {
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
            return new ChatTypeImpl(chat, narration, null);
        }
    }
}
