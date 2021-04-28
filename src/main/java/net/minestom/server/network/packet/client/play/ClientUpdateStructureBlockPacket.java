package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.BlockPosition;
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

    public BlockPosition location = new BlockPosition(0, 0, 0);
    public Action action = Action.UPDATE_DATA;
    public Mode mode = Mode.DATA;
    public String name = "";
    public BlockPosition offset = new BlockPosition(0, 1, 0);
    public BlockPosition size = new BlockPosition(1, 1, 1);
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
        writer.writeByte((byte) offset.getX());
        writer.writeByte((byte) offset.getY());
        writer.writeByte((byte) offset.getZ());
        writer.writeByte((byte) size.getX());
        writer.writeByte((byte) size.getY());
        writer.writeByte((byte) size.getZ());
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
        offset = new BlockPosition(
                reader.readByte(),
                reader.readByte(),
                reader.readByte()
        );
        size = new BlockPosition(
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
        switch (rotation) {
            case NONE: return 0;
            case CLOCKWISE: return 1;
            case FLIPPED: return 2;
            case COUNTER_CLOCKWISE: return 3;
            default: throw new IllegalArgumentException("ClientUpdateStructurePacket#rotation must be a valid 90-degree rotation.");
        }
    }

    private Rotation fromRestrictedRotation(int rotation) {
        switch (rotation) {
            case 0: return Rotation.NONE;
            case 1: return Rotation.CLOCKWISE;
            case 2: return Rotation.FLIPPED;
            case 3: return Rotation.COUNTER_CLOCKWISE;
            default: throw new IllegalArgumentException("ClientUpdateStructurePacket#rotation must be a valid 90-degree rotation.");
        }
    }

}
