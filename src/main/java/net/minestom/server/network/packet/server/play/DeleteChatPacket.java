package net.minestom.server.network.packet.server.play;

import net.minestom.server.crypto.MessageSignature;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

public record DeleteChatPacket(@NotNull MessageSignature signature) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<DeleteChatPacket> SERIALIZER = NetworkBufferTemplate.template(
            MessageSignature.SERIALIZER, DeleteChatPacket::signature,
            DeleteChatPacket::new
    );
}
