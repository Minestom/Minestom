package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

import java.util.List;

public record FireworkList(int flightDuration, List<FireworkExplosion> explosions) {
    public static final FireworkList EMPTY = new FireworkList(0, List.of());

    public static final NetworkBuffer.Type<FireworkList> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, FireworkList::flightDuration,
            FireworkExplosion.NETWORK_TYPE.list(256), FireworkList::explosions,
            FireworkList::new);
    public static final Codec<FireworkList> NBT_TYPE = StructCodec.struct(
            // Mojang uses a byte here but var int for protocol so we map to byte here
            "flight_duration", Codec.BYTE.transform(Byte::intValue, Integer::byteValue), FireworkList::flightDuration,
            "explosions", FireworkExplosion.CODEC.list().optional(List.of()), FireworkList::explosions,
            FireworkList::new);

    public FireworkList {
        explosions = List.copyOf(explosions);
    }

    public FireworkList withFlightDuration(int flightDuration) {
        return new FireworkList(flightDuration, explosions);
    }

    public FireworkList withExplosions(List<FireworkExplosion> explosions) {
        return new FireworkList(flightDuration, explosions);
    }
}
