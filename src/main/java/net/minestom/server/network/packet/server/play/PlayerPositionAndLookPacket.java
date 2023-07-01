package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record PlayerPositionAndLookPacket(Pos position, byte flags, int teleportId,
                                          boolean dismountVehicle) implements ServerPacket {
    public PlayerPositionAndLookPacket(@NotNull NetworkBuffer reader) {
        this(new Pos(reader.read(DOUBLE), reader.read(DOUBLE), reader.read(DOUBLE), reader.read(FLOAT), reader.read(FLOAT)),
                reader.read(BYTE), reader.read(VAR_INT), reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(DOUBLE, position.x());
        writer.write(DOUBLE, position.y());
        writer.write(DOUBLE, position.z());

        writer.write(FLOAT, position.yaw());
        writer.write(FLOAT, position.pitch());

        writer.write(BYTE, flags);
        writer.write(VAR_INT, teleportId);
        writer.write(BOOLEAN, dismountVehicle);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLAYER_POSITION_AND_LOOK;
    }
}