package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.Rotation;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientUpdateStructureBlockPacket extends ClientPlayPacket {
    // Flag values
    public static final byte IGNORE_ENTITIES = 0x1;
    public static final byte SHOW_AIR = 0x2;
    /**
     * Requires the player to be in creative and have a permission level higher than 2.
     */
    public static final byte SHOW_BOUNDING_BOX = 0x4;

    public Point location = Vec.ZERO;
    public Action action = Action.UPDATE_DATA;
    public Mode mode = Mode.DATA;
    public String name = "";
    public Point offset = new Vec(0, 1, 0);
    public Point size = Vec.ONE;
    public Mirror mirror = Mirror.NONE;
    public Rotation rotation = Rotation.NONE;
    public String metadata = "";
    public float integrity = 1.0f;
    public long seed = 0;
    public byte flags = 0x0;

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

    @Override
    public void read(@NotNull BinaryReader reader) {
        location = reader.readBlockPosition();
        action = Action.values()[reader.readVarInt()];
        mode = Mode.values()[reader.readVarInt()];
        name = reader.readSizedString(Short.MAX_VALUE);
        offset = new Vec(
                reader.readByte(),
                reader.readByte(),
                reader.readByte()
        );
        size = new Vec(
                reader.readByte(),
                reader.readByte(),
                reader.readByte()
        );
        mirror = Mirror.values()[reader.readVarInt()];
        rotation = fromRestrictedRotation(reader.readVarInt());
        metadata = reader.readSizedString(Short.MAX_VALUE);
        integrity = reader.readFloat();
        seed = reader.readVarLong();
        flags = reader.readByte();
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

    private int toRestrictedRotation(Rotation rotation) {
        return switch (rotation) {
            case NONE -> 0;
            case CLOCKWISE -> 1;
            case FLIPPED -> 2;
            case COUNTER_CLOCKWISE -> 3;
            default -> throw new IllegalArgumentException("ClientUpdateStructurePacket#rotation must be a valid 90-degree rotation.");
        };
    }

    private Rotation fromRestrictedRotation(int rotation) {
        return switch (rotation) {
            case 0 -> Rotation.NONE;
            case 1 -> Rotation.CLOCKWISE;
            case 2 -> Rotation.FLIPPED;
            case 3 -> Rotation.COUNTER_CLOCKWISE;
            default -> throw new IllegalArgumentException("ClientUpdateStructurePacket#rotation must be a valid 90-degree rotation.");
        };
    }
}
