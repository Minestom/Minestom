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
    public static final NetworkBuffer.Type<ClientUpdateStructureBlockPacket> SERIALIZER = new Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, ClientUpdateStructureBlockPacket value) {
            buffer.write(BLOCK_POSITION, value.location);
            buffer.writeEnum(Action.class, value.action);
            buffer.writeEnum(Mode.class, value.mode);
            buffer.write(STRING, value.name);
            buffer.write(BYTE, (byte) value.offset.x());
            buffer.write(BYTE, (byte) value.offset.y());
            buffer.write(BYTE, (byte) value.offset.z());
            buffer.write(BYTE, (byte) value.size.x());
            buffer.write(BYTE, (byte) value.size.y());
            buffer.write(BYTE, (byte) value.size.z());
            buffer.write(VAR_INT, value.mirror.ordinal());
            buffer.write(VAR_INT, toRestrictedRotation(value.rotation));
            buffer.write(STRING, value.metadata);
            buffer.write(FLOAT, value.integrity);
            buffer.write(VAR_LONG, value.seed);
            buffer.write(BYTE, value.flags);
        }

        @Override
        public ClientUpdateStructureBlockPacket read(@NotNull NetworkBuffer buffer) {
            return new ClientUpdateStructureBlockPacket(buffer.read(BLOCK_POSITION), buffer.readEnum(Action.class),
                    buffer.readEnum(Mode.class), buffer.read(STRING),
                    new Vec(buffer.read(BYTE), buffer.read(BYTE), buffer.read(BYTE)), new Vec(buffer.read(BYTE), buffer.read(BYTE), buffer.read(BYTE)),
                    Mirror.values()[buffer.read(VAR_INT)], fromRestrictedRotation(buffer.read(VAR_INT)),
                    buffer.read(STRING), buffer.read(FLOAT),
                    buffer.read(VAR_LONG), buffer.read(BYTE));
        }
    };

    // Flag values
    public static final byte IGNORE_ENTITIES = 0x1;
    public static final byte SHOW_AIR = 0x2;
    /**
     * Requires the player to be in creative and have a permission level higher than 2.
     */
    public static final byte SHOW_BOUNDING_BOX = 0x4;

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
