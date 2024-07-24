package net.minestom.server.network.packet.client.login;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.BYTE_ARRAY;

public record ClientEncryptionResponsePacket(byte[] sharedSecret,
                                             byte[] encryptedVerifyToken) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientEncryptionResponsePacket> SERIALIZER = NetworkBufferTemplate.template(
            BYTE_ARRAY, ClientEncryptionResponsePacket::sharedSecret,
            BYTE_ARRAY, ClientEncryptionResponsePacket::encryptedVerifyToken,
            ClientEncryptionResponsePacket::new);

    @Override
    public boolean processImmediately() {
        return true;
    }
}
