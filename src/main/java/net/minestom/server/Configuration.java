package net.minestom.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.registry.dynamic.chat.ChatDecoration;
import net.minestom.server.registry.dynamic.chat.ChatType;
import net.minestom.server.registry.dynamic.chat.ChatTypeBuilder;

public record Configuration(boolean requireValidPlayerPublicKey, Component missingPlayerPublicKeyMessage,
                            Component invalidPlayerPublicKeyMessage, ChatTypeBuilder playerChatType,
                            ChatTypeBuilder systemChatType) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean requireValidPlayerPublicKey = false;
        private Component missingPlayerPublicKeyMessage = Component.text("Missing public key!", NamedTextColor.RED);
        private Component invalidPlayerPublicKeyMessage = Component.text("Invalid public key!", NamedTextColor.RED);
        private ChatTypeBuilder playerChatType = ChatTypeBuilder.builder(ChatType.CHAT.key()).chat(ChatDecoration.contentWithSender("chat.type.text"));
        private ChatTypeBuilder systemChatType = ChatTypeBuilder.builder(ChatType.SYSTEM.key()).chat();

        public Builder setRequireValidPlayerPublicKey(boolean requireValidPlayerPublicKey) {
            this.requireValidPlayerPublicKey = requireValidPlayerPublicKey;
            return this;
        }

        public Builder setMissingPlayerPublicKeyMessage(Component missingPlayerPublicKeyMessage) {
            this.missingPlayerPublicKeyMessage = missingPlayerPublicKeyMessage;
            return this;
        }

        public Builder setInvalidPlayerPublicKeyMessage(Component invalidPlayerPublicKeyMessage) {
            this.invalidPlayerPublicKeyMessage = invalidPlayerPublicKeyMessage;
            return this;
        }

        public Builder setPlayerChatType(ChatTypeBuilder playerChatType) {
            this.playerChatType = playerChatType;
            return this;
        }

        public Builder setSystemChatType(ChatTypeBuilder systemChatType) {
            this.systemChatType = systemChatType;
            return this;
        }

        public Configuration build() {
            return new Configuration(requireValidPlayerPublicKey, missingPlayerPublicKeyMessage, invalidPlayerPublicKeyMessage, playerChatType, systemChatType);
        }
    }
}
