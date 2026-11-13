package net.minestom.server.network;

import net.minestom.server.network.packet.PacketReading;
import net.minestom.server.network.packet.PacketWriting;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.play.ClientPunchPacket;
import net.minestom.server.registry.Registries;
import net.minestom.testing.RegistriesTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.zip.DataFormatException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@RegistriesTest
public class SendablePacketRegistriesTest {

    @Test
    public void trimmed(Registries registries) throws DataFormatException {
        var packet = new ClientPunchPacket();

        var buffer = PacketWriting.allocateTrimmedPacket(ConnectionState.PLAY, packet, 0, registries);

        var result = PacketReading.readClient(buffer, ConnectionState.PLAY, false);
        if (!(result instanceof PacketReading.Result.Success<ClientPacket>(
                List<PacketReading.ParsedPacket<ClientPacket>> packets
        ))) {
            fail();
            return;
        }
        assertEquals(1, packets.size());
        ClientPacket readPacket = packets.getFirst().packet();
        assertEquals(packet, readPacket);
    }
}
