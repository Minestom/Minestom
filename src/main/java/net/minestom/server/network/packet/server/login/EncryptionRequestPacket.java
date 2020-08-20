package net.minestom.server.network.packet.server.login;

import net.minestom.server.MinecraftServer;
import net.minestom.server.data.type.array.ByteArrayData;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.concurrent.ThreadLocalRandom;

public class EncryptionRequestPacket implements ServerPacket {

    private byte[] nonce = new byte[4];

    public EncryptionRequestPacket(PlayerConnection connection) {
        ThreadLocalRandom.current().nextBytes(nonce);
        connection.setNonce(nonce);
    }

    @Override
    public void write(BinaryWriter writer) {
        writer.writeSizedString("");
        byte[] publicKey = MinecraftServer.getKeyPair().getPublic().getEncoded();
        ByteArrayData.encodeByteArray(writer, publicKey);
        ByteArrayData.encodeByteArray(writer, nonce);
    }

    @Override
    public int getId() {
        return 0x01;
    }
}
