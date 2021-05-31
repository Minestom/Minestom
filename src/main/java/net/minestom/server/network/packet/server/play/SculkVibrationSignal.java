package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class SculkVibrationSignal implements ServerPacket {
    @Override
    public int getId() {
        return 0;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {

    }
}
