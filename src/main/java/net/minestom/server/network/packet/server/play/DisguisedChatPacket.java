package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.COMPONENT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record DisguisedChatPacket(
        @NotNull Component message,
        int type,
        @NotNull Component senderName,
        @Nullable Component targetName
) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<DisguisedChatPacket> SERIALIZER = NetworkBufferTemplate.template(
            COMPONENT, DisguisedChatPacket::message,
            VAR_INT, DisguisedChatPacket::type,
            COMPONENT, DisguisedChatPacket::senderName,
            COMPONENT.optional(), DisguisedChatPacket::targetName,
            DisguisedChatPacket::new);
}
