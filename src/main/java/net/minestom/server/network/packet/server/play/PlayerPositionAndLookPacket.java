package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record PlayerPositionAndLookPacket(Pos position, byte flags, int teleportId,
                                          boolean dismountVehicle) implements ServerPacket {

    // Bitfield from https://wiki.vg/Protocol#Synchronize_Player_Position
    private static final byte FLAG_RELATIVE_X = 0x01;
    private static final byte FLAG_RELATIVE_Y = 0x02;
    private static final byte FLAG_RELATIVE_Z = 0x04;
    private static final byte FLAG_RELATIVE_Y_ROT = 0x08;
    private static final byte FLAG_RELATIVE_X_ROT = 0x10;
    /**
     * Used to declare that the given coordinates are relative to the player's position.
     */
    public static final byte FLAG_RELATIVE =
            FLAG_RELATIVE_X | FLAG_RELATIVE_Y | FLAG_RELATIVE_Z | FLAG_RELATIVE_Y_ROT | FLAG_RELATIVE_X_ROT;
    /**
     * Used to declare that the given coordinates are absolute.
     */
    public static final byte FLAG_ABSOLUTE = 0x0;


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