package net.minestom.server.message;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Holder;
import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public sealed interface ChatType extends Holder.Direct<ChatType>, ChatTypes permits ChatTypeImpl {

    @NotNull Codec<ChatType> REGISTRY_CODEC = StructCodec.struct(
            "chat", ChatTypeDecoration.CODEC, ChatType::chat,
            "narration", ChatTypeDecoration.CODEC, ChatType::narration,
            ChatType::create);

    static @NotNull ChatType create(
            @NotNull ChatTypeDecoration chat,
            @NotNull ChatTypeDecoration narration
    ) {
        return new ChatTypeImpl(chat, narration);
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
        return DynamicRegistry.create(Key.key("chat_type"), REGISTRY_CODEC, RegistryData.Resource.CHAT_TYPES);
    }

    @NotNull ChatTypeDecoration chat();

    @NotNull ChatTypeDecoration narration();

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
            return new ChatTypeImpl(chat, narration);
        }
    }
}
