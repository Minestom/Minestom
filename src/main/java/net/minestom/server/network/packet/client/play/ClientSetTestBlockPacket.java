package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

public record ClientSetTestBlockPacket(
        Point blockPosition,
        TestBlockMode mode,
        String message
) implements ClientPacket {

    public static final NetworkBuffer.Type<ClientSetTestBlockPacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.BLOCK_POSITION, ClientSetTestBlockPacket::blockPosition,
            TestBlockMode.NETWORK_TYPE, ClientSetTestBlockPacket::mode,
            NetworkBuffer.STRING, ClientSetTestBlockPacket::message,
            ClientSetTestBlockPacket::new);

    public enum TestBlockMode {
        START,
        LOG,
        FAIL,
        ACCEPT;

        public static final NetworkBuffer.Type<TestBlockMode> NETWORK_TYPE = NetworkBuffer.Enum(TestBlockMode.class);
    }
}
