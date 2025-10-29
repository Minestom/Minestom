package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.Rotation;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientUpdateStructureBlockPacket(
        Point location, Action action,
        Mode mode, String name,
        Point offset, Point size,
        Mirror mirror, Rotation rotation,
        String metadata, float integrity,
        long seed, byte flags
) implements ClientPacket.Play {

    public static final NetworkBuffer.Type<ClientUpdateStructureBlockPacket> SERIALIZER = NetworkBufferTemplate.template(
            BLOCK_POSITION, ClientUpdateStructureBlockPacket::location,
            Action.NETWORK_TYPE, ClientUpdateStructureBlockPacket::action,
            Mode.NETWORK_TYPE, ClientUpdateStructureBlockPacket::mode,
            STRING, ClientUpdateStructureBlockPacket::name,
            VECTOR3B, ClientUpdateStructureBlockPacket::offset,
            VECTOR3B, ClientUpdateStructureBlockPacket::size,
            Mirror.NETWORK_TYPE, ClientUpdateStructureBlockPacket::mirror,
            VAR_INT.transform(ClientUpdateStructureBlockPacket::fromRestrictedRotation, ClientUpdateStructureBlockPacket::toRestrictedRotation), ClientUpdateStructureBlockPacket::rotation,
            STRING, ClientUpdateStructureBlockPacket::metadata,
            FLOAT, ClientUpdateStructureBlockPacket::integrity,
            LONG, ClientUpdateStructureBlockPacket::seed,
            BYTE, ClientUpdateStructureBlockPacket::flags,
            ClientUpdateStructureBlockPacket::new
    );

    // Flag values
    public static final byte IGNORE_ENTITIES = 0x1;
    public static final byte SHOW_AIR = 0x2;
    /**
     * Requires the player to be in creative and have a permission level higher than 2.
     */
    public static final byte SHOW_BOUNDING_BOX = 0x4;
    public static final byte STRICT = 0x8;

    /**
     * Update action, <code>UPDATE_DATA</code> indicates nothing special.
     */
    public enum Action {
        UPDATE_DATA,
        SAVE,
        LOAD,
        DETECT_SIZE;

        public static final NetworkBuffer.Type<Action> NETWORK_TYPE = NetworkBuffer.Enum(Action.class);
    }

    public enum Mode {
        SAVE,
        LOAD,
        CORNER,
        DATA;

        public static final NetworkBuffer.Type<Mode> NETWORK_TYPE = NetworkBuffer.Enum(Mode.class);
    }

    public enum Mirror {
        NONE,
        LEFT_RIGHT,
        FRONT_BACK;

        public static final NetworkBuffer.Type<Mirror> NETWORK_TYPE = NetworkBuffer.Enum(Mirror.class);
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
