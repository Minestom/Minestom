package net.minestom.server.network.packet.server.login;

import net.minestom.server.MinecraftServer;
import net.minestom.server.data.type.array.ByteArrayData;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class EncryptionRequestPacket implements ServerPacket {

    public byte[] publicKey;
    public byte[] nonce = new byte[4];

    public EncryptionRequestPacket(PlayerConnection connection) {
        ThreadLocalRandom.current().nextBytes(nonce);
        connection.setNonce(nonce);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString("");
        final byte[] publicKey = MinecraftServer.getKeyPair().getPublic().getEncoded();
        ByteArrayData.encodeByteArray(writer, publicKey);
        ByteArrayData.encodeByteArray(writer, nonce);
    }

    @Override
    public int getId() {
        return 0x01;
    }
}
