package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.BYTE;

public record ClientSteerVehiclePacket(byte flags) implements ClientPacket {
    private static final byte FLAG_FORWARD = 1;
    private static final byte FLAG_BACKWARD = 1 << 1;
    private static final byte FLAG_LEFT = 1 << 2;
    private static final byte FLAG_RIGHT = 1 << 3;
    private static final byte FLAG_JUMP = 1 << 4;
    private static final byte FLAG_SHIFT = 1 << 5;
    private static final byte FLAG_SPRINT = 1 << 6;

    public static final NetworkBuffer.Type<ClientSteerVehiclePacket> SERIALIZER = NetworkBufferTemplate.template(
            BYTE, ClientSteerVehiclePacket::flags,
            ClientSteerVehiclePacket::new);

    public ClientSteerVehiclePacket(boolean forward, boolean backward, boolean left, boolean right, boolean jump, boolean shift, boolean sprint) {
        this((byte) ((forward ? FLAG_FORWARD : 0) |
                (backward ? FLAG_BACKWARD : 0) |
                (left ? FLAG_LEFT : 0) |
                (right ? FLAG_RIGHT : 0) |
                (jump ? FLAG_JUMP : 0) |
                (shift ? FLAG_SHIFT : 0) |
                (sprint ? FLAG_SPRINT : 0)));
    }

    public boolean forward() {
        return (flags & FLAG_FORWARD) != 0;
    }

    public boolean backward() {
        return (flags & FLAG_BACKWARD) != 0;
    }

    public boolean left() {
        return (flags & FLAG_LEFT) != 0;
    }

    public boolean right() {
        return (flags & FLAG_RIGHT) != 0;
    }

    public boolean jump() {
        return (flags & FLAG_JUMP) != 0;
    }

    public boolean shift() {
        return (flags & FLAG_SHIFT) != 0;
    }

    public boolean sprint() {
        return (flags & FLAG_SPRINT) != 0;
    }
}
