package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class SculkVibrationSignal implements ServerPacket {

    public Point position;
    public String destinationIdentifier;
    // TODO 'varies' destination
    public int arrivalTicks;

    @Override
    public void write(@NotNull BinaryWriter writer) {

    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SCULK_VIBRATION_SIGNAL;
    }
}
