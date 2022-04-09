package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.Rotation;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientUpdateStructureBlockPacket(Point location, Action action,
                                               Mode mode, String name,
                                               Point offset, Point size,
                                               Mirror mirror, Rotation rotation,
                                               String metadata, float integrity,
                                               long seed, byte flags) implements ClientPacket {
    public ClientUpdateStructureBlockPacket(BinaryReader reader) {
        this(reader.readBlockPosition(), Action.values()[reader.readVarInt()],
                Mode.values()[reader.readVarInt()], reader.readSizedString(Short.MAX_VALUE),
                new Vec(reader.readByte(), reader.readByte(), reader.readByte()), new Vec(reader.readByte(), reader.readByte(), reader.readByte()),
                Mirror.values()[reader.readVarInt()], fromRestrictedRotation(reader.readVarInt()),
                reader.readSizedString(Short.MAX_VALUE), reader.readFloat(),
                reader.readVarLong(), reader.readByte());
    }

    // Flag values
    public static final byte IGNORE_ENTITIES = 0x1;
    public static final byte SHOW_AIR = 0x2;
    /**
     * Requires the player to be in creative and have a permission level higher than 2.
     */
    public static final byte SHOW_BOUNDING_BOX = 0x4;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBlockPosition(location);
        writer.writeVarInt(action.ordinal());
        writer.writeVarInt(mode.ordinal());
        writer.writeSizedString(name);
        writer.writeByte((byte) offset.x());
        writer.writeByte((byte) offset.y());
        writer.writeByte((byte) offset.z());
        writer.writeByte((byte) size.x());
        writer.writeByte((byte) size.y());
        writer.writeByte((byte) size.z());
        writer.writeVarInt(mirror.ordinal());
        writer.writeVarInt(toRestrictedRotation(rotation));
        writer.writeSizedString(metadata);
        writer.writeFloat(integrity);
        writer.writeVarLong(seed);
        writer.writeByte(flags);
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
            default -> throw new IllegalArgumentException("ClientUpdateStructurePacket#rotation must be a valid 90-degree rotation.");
        };
    }

    private static Rotation fromRestrictedRotation(int rotation) {
        return switch (rotation) {
            case 0 -> Rotation.NONE;
            case 1 -> Rotation.CLOCKWISE;
            case 2 -> Rotation.FLIPPED;
            case 3 -> Rotation.COUNTER_CLOCKWISE;
            default -> throw new IllegalArgumentException("ClientUpdateStructurePacket#rotation must be a valid 90-degree rotation.");
        };
    }
}
