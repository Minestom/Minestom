package net.minestom.server.message;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Holder;
import net.minestom.server.registry.RegistryData;
import org.jetbrains.annotations.ApiStatus;

public sealed interface ChatType extends Holder.Direct<ChatType>, ChatTypes permits ChatTypeImpl {

    Codec<ChatType> REGISTRY_CODEC = StructCodec.struct(
            "chat", ChatTypeDecoration.CODEC, ChatType::chat,
            "narration", ChatTypeDecoration.CODEC, ChatType::narration,
            ChatType::create);

    static ChatType create(
            ChatTypeDecoration chat,
            ChatTypeDecoration narration
    ) {
        return new ChatTypeImpl(chat, narration);
    }

    static Builder builder() {
        return new Builder();
    }


    /**
     * <p>Creates a new registry for chat types, loading the vanilla chat types.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<ChatType> createDefaultRegistry() {
        return DynamicRegistry.create(Key.key("minecraft:chat_type"), REGISTRY_CODEC, RegistryData.Resource.CHAT_TYPES);
    }

    ChatTypeDecoration chat();

    ChatTypeDecoration narration();

    final class Builder {
        private ChatTypeDecoration chat;
        private ChatTypeDecoration narration;

        private Builder() {
        }

        public Builder chat(ChatTypeDecoration chat) {
            this.chat = chat;
            return this;
        }

        public Builder narration(ChatTypeDecoration narration) {
            this.narration = narration;
            return this;
        }

        public ChatType build() {
            return new ChatTypeImpl(chat, narration);
        }
    }
}
