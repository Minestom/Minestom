package net.minestom.server.network.packet.server.play;

/**
 * Packing of the ground flag and the step count that prefixes the delta of the entity movement packets.
 */
final class MovementProperties {
    private static final int ON_GROUND_FLAG = 1;
    private static final int STEP_COUNT_OFFSET = 1;

    private MovementProperties() {
    }

    static int pack(boolean onGround, int stepCount) {
        return (onGround ? ON_GROUND_FLAG : 0) | stepCount << STEP_COUNT_OFFSET;
    }

    static boolean onGround(int properties) {
        return (properties & ON_GROUND_FLAG) != 0;
    }

    static int stepCount(int properties) {
        return properties >>> STEP_COUNT_OFFSET;
    }
}
