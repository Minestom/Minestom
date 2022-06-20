package net.minestom.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.registry.dynamic.chat.ChatDecoration;
import net.minestom.server.registry.dynamic.chat.ChatType;
import net.minestom.server.registry.dynamic.chat.ChatTypeBuilder;
import net.minestom.server.utils.FinalObject;
import org.jetbrains.annotations.ApiStatus;

/**
 * Can be used to change Minestom defaults before {@link MinecraftServer#start} is called.
 */
public final class ConfigurationManager {
    public final FinalObject<Boolean> REQUIRE_VALID_PLAYER_PUBLIC_KEY = new FinalObject<>();
    public final FinalObject<Component> MISSING_PLAYER_PUBLIC_KEY = new FinalObject<>();
    public final FinalObject<Component> INVALID_PLAYER_PUBLIC_KEY = new FinalObject<>();
    public final FinalObject<ChatTypeBuilder> PLAYER_CHAT_TYPE = new FinalObject<>();
    public final FinalObject<ChatTypeBuilder> SYSTEM_CHAT_TYPE = new FinalObject<>();

    @ApiStatus.Internal
    public void initDefaults() {
        REQUIRE_VALID_PLAYER_PUBLIC_KEY.optionalSet(false);
        MISSING_PLAYER_PUBLIC_KEY.optionalSet(Component.text("Missing public key!", NamedTextColor.RED));
        INVALID_PLAYER_PUBLIC_KEY.optionalSet(Component.text("Invalid public key!", NamedTextColor.RED));
        PLAYER_CHAT_TYPE.optionalSet(() -> ChatTypeBuilder.builder(ChatType.CHAT.key())
                .chat(ChatDecoration.contentWithSender("chat.type.text")));
        SYSTEM_CHAT_TYPE.optionalSet(() -> ChatTypeBuilder.builder(ChatType.SYSTEM.key()).chat());
    }
}
