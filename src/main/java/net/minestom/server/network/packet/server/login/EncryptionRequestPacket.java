package net.minestom.server.network.packet.server.login;

import net.minestom.server.data.type.array.ByteArrayData;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.network.player.PlayerSocketConnection;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class EncryptionRequestPacket implements ServerPacket {

    public byte[] publicKey;
    public byte[] nonce = new byte[4];

    public EncryptionRequestPacket(PlayerSocketConnection connection) {
        ThreadLocalRandom.current().nextBytes(nonce);
        connection.setNonce(nonce);
    }

    /**
     * Only for testing purposes. DO NOT USE
     */
    private EncryptionRequestPacket() {
        MojangAuth.init();
        publicKey = new byte[0];
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString("");
        final byte[] publicKey = MojangAuth.getKeyPair().getPublic().getEncoded();
        ByteArrayData.encodeByteArray(writer, publicKey);
        ByteArrayData.encodeByteArray(writer, nonce);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        reader.readSizedString(); // server id, apparently empty

        publicKey = ByteArrayData.decodeByteArray(reader);
        nonce = ByteArrayData.decodeByteArray(reader);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.LOGIN_ENCRYPTION_REQUEST;
    }
}
