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
        public void write(@NotNull NetworkBuffer writer, ClientUpdateStructureBlockPacket value) {
            writer.write(BLOCK_POSITION, value.location);
            writer.writeEnum(Action.class, value.action);
            writer.writeEnum(Mode.class, value.mode);
            writer.write(STRING, value.name);
            writer.write(BYTE, (byte) value.offset.x());
            writer.write(BYTE, (byte) value.offset.y());
            writer.write(BYTE, (byte) value.offset.z());
            writer.write(BYTE, (byte) value.size.x());
            writer.write(BYTE, (byte) value.size.y());
            writer.write(BYTE, (byte) value.size.z());
            writer.write(VAR_INT, value.mirror.ordinal());
            writer.write(VAR_INT, toRestrictedRotation(value.rotation));
            writer.write(STRING, value.metadata);
            writer.write(FLOAT, value.integrity);
            writer.write(VAR_LONG, value.seed);
            writer.write(BYTE, value.flags);
        }

        @Override
        public ClientUpdateStructureBlockPacket read(@NotNull NetworkBuffer reader) {
            return new ClientUpdateStructureBlockPacket(reader.read(BLOCK_POSITION), reader.readEnum(Action.class),
                    reader.readEnum(Mode.class), reader.read(STRING),
                    new Vec(reader.read(BYTE), reader.read(BYTE), reader.read(BYTE)), new Vec(reader.read(BYTE), reader.read(BYTE), reader.read(BYTE)),
                    Mirror.values()[reader.read(VAR_INT)], fromRestrictedRotation(reader.read(VAR_INT)),
                    reader.read(STRING), reader.read(FLOAT),
                    reader.read(VAR_LONG), reader.read(BYTE));
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
