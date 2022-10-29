package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.Rotation;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientUpdateStructureBlockPacket(Point location, Action action,
                                               Mode mode, String name,
                                               Point offset, Point size,
                                               Mirror mirror, Rotation rotation,
                                               String metadata, float integrity,
                                               long seed, byte flags) implements ClientPacket {
    public ClientUpdateStructureBlockPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(BLOCK_POSITION), reader.readEnum(Action.class),
                reader.readEnum(Mode.class), reader.read(STRING),
                new Vec(reader.read(BYTE), reader.read(BYTE), reader.read(BYTE)), new Vec(reader.read(BYTE), reader.read(BYTE), reader.read(BYTE)),
                Mirror.values()[reader.read(VAR_INT)], fromRestrictedRotation(reader.read(VAR_INT)),
                reader.read(STRING), reader.read(FLOAT),
                reader.read(VAR_LONG), reader.read(BYTE));
    }

    // Flag values
    public static final byte IGNORE_ENTITIES = 0x1;
    public static final byte SHOW_AIR = 0x2;
    /**
     * Requires the player to be in creative and have a permission level higher than 2.
     */
    public static final byte SHOW_BOUNDING_BOX = 0x4;

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BLOCK_POSITION, location);
        writer.writeEnum(Action.class, action);
        writer.writeEnum(Mode.class, mode);
        writer.write(STRING, name);
        writer.write(BYTE, (byte) offset.x());
        writer.write(BYTE, (byte) offset.y());
        writer.write(BYTE, (byte) offset.z());
        writer.write(BYTE, (byte) size.x());
        writer.write(BYTE, (byte) size.y());
        writer.write(BYTE, (byte) size.z());
        writer.write(VAR_INT, mirror.ordinal());
        writer.write(VAR_INT, toRestrictedRotation(rotation));
        writer.write(STRING, metadata);
        writer.write(FLOAT, integrity);
        writer.write(VAR_LONG, seed);
        writer.write(BYTE, flags);
    }

    /**
     * Update action, <code>UPDATE_DATA</code> indicates nothing special.
     */
    public enum Action {
        UPDATE_DATA, SAVE, LOAD, DETECT_SIZE
    }

    public enum Mode {
        SAVE, LOAD, CORNER, DATA
    }

    public enum Mirror {
        NONE, LEFT_RIGHT, FRONT_BACK
    }

    private static int toRestrictedRotation(Rotation rotation) {
        return switch (rotation) {
            case NONE -> 0;
            case CLOCKWISE -> 1;
            case FLIPPED -> 2;
            case COUNTER_CLOCKWISE -> 3;
            default ->
                    throw new IllegalArgumentException("ClientUpdateStructurePacket#rotation must be a valid 90-degree rotation.");
        };
    }

    private static Rotation fromRestrictedRotation(int rotation) {
        return switch (rotation) {
            case 0 -> Rotation.NONE;
            case 1 -> Rotation.CLOCKWISE;
            case 2 -> Rotation.FLIPPED;
            case 3 -> Rotation.COUNTER_CLOCKWISE;
            default ->
                    throw new IllegalArgumentException("ClientUpdateStructurePacket#rotation must be a valid 90-degree rotation.");
        };
    }
}
