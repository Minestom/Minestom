package network;

import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.minestom.server.message.ChatPosition;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.handshake.HandshakePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.handshake.ResponsePacket;
import net.minestom.server.network.packet.server.login.LoginDisconnectPacket;
import net.minestom.server.network.packet.server.login.LoginSuccessPacket;
import net.minestom.server.network.packet.server.login.SetCompressionPacket;
import net.minestom.server.network.packet.server.play.ChatMessagePacket;
import net.minestom.server.network.packet.server.status.PongPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Ensures that packet can be written and read correctly.
 */
public class PacketWriteReadTest {
    private static final List<ServerPacket> SERVER_PACKETS = new ArrayList<>();
    private static final List<ClientPacket> CLIENT_PACKETS = new ArrayList<>();

    @BeforeAll
    public static void setupServer() {
        SERVER_PACKETS.add(new ResponsePacket(new JsonObject().toString()));
        SERVER_PACKETS.add(new PongPacket(5));
        //SERVER_PACKETS.add(new EncryptionRequestPacket("server", generateByteArray(16), generateByteArray(16)));
        SERVER_PACKETS.add(new LoginDisconnectPacket(Component.text("Hey")));
        //SERVER_PACKETS.add(new LoginPluginRequestPacket(5, "id", generateByteArray(16)));
        SERVER_PACKETS.add(new LoginSuccessPacket(UUID.randomUUID(), "TheMode911"));
        SERVER_PACKETS.add(new SetCompressionPacket(256));
        SERVER_PACKETS.add(new ChatMessagePacket(Component.text("Hey"), ChatPosition.CHAT, UUID.randomUUID()));
    }

    @BeforeAll
    public static void setupClient() {
        CLIENT_PACKETS.add(new HandshakePacket(755, "localhost", 25565, 2));
    }

    @Test
    public void serverTest() {
        SERVER_PACKETS.forEach(PacketWriteReadTest::testPacket);
    }

    @Test
    public void clientTest() {
        CLIENT_PACKETS.forEach(PacketWriteReadTest::testPacket);
    }

    private static void testPacket(Writeable writeable) {
        try {
            BinaryWriter writer = new BinaryWriter();
            writeable.write(writer);
            var readerConstructor = writeable.getClass().getConstructor(BinaryReader.class);

            BinaryReader reader = new BinaryReader(writer.toByteArray());
            var createdPacket = readerConstructor.newInstance(reader);
            assertEquals(writeable, createdPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] generateByteArray(int size) {
        byte[] array = new byte[size];
        ThreadLocalRandom.current().nextBytes(array);
        return array;
    }
}
