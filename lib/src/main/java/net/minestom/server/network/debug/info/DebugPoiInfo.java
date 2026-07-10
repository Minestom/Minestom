package net.minestom.server.network.debug.info;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

public record DebugPoiInfo(
        Point position,
        Type type,
        int freeTicketCount
) {
    public static final NetworkBuffer.Type<DebugPoiInfo> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.BLOCK_POSITION, DebugPoiInfo::position,
            Type.SERIALIZER, DebugPoiInfo::type,
            NetworkBuffer.INT, DebugPoiInfo::freeTicketCount,
            DebugPoiInfo::new);

    public enum Type {
        ARMORER,
        BUTCHER,
        CARTOGRAPHER,
        CLERIC,
        FARMER,
        FISHERMAN,
        FLETCHER,
        LEATHERWORKER,
        LIBRARIAN,
        MASON,
        SHEPHERD,
        TOOLSMITH,
        WEAPONSMITH,
        HOME,
        MEETING,
        BEEHIVE,
        BEE_NEST,
        NETHER_PORTAL,
        LODESTONE,
        TEST_INSTANCE,
        LIGHTNING_ROD;

        public static final NetworkBuffer.Type<Type> SERIALIZER = NetworkBuffer.Enum(Type.class);
    }
}
