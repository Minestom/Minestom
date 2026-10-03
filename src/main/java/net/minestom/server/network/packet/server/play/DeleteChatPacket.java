package net.minestom.server.network.packet.server.play;

import net.minestom.server.crypto.MessageSignature;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

/**
 * Removes a signed message from the chat of the client.
 *
 * @param signature the signature of the message to remove, either in full or as an index into the signature cache of the client
 */
public record DeleteChatPacket(MessageSignature.Packed signature) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<DeleteChatPacket> SERIALIZER = NetworkBufferTemplate.template(
            MessageSignature.Packed.SERIALIZER, DeleteChatPacket::signature,
            DeleteChatPacket::new
    );

    /**
     * Creates a packet that removes the message with the given full signature.
     *
     * @param signature the full signature of the message to remove
     */
    public DeleteChatPacket(MessageSignature signature) {
        this(new MessageSignature.Packed(signature));
    }
}
