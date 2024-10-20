package net.minestom.server.item.component;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.nbt.BinaryTagTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record FireworkList(int flightDuration, @NotNull List<FireworkExplosion> explosions) {
    public static final FireworkList EMPTY = new FireworkList(0, List.of());

    public static final NetworkBuffer.Type<FireworkList> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, FireworkList::flightDuration,
            FireworkExplosion.NETWORK_TYPE.list(256), FireworkList::explosions,
            FireworkList::new);
    public static final BinaryTagSerializer<FireworkList> NBT_TYPE = BinaryTagTemplate.object(
            "flight_duration", BinaryTagSerializer.INT, FireworkList::flightDuration,
            "explosions", FireworkExplosion.NBT_TYPE.list().optional(List.of()), FireworkList::explosions,
            FireworkList::new);

    public FireworkList {
        explosions = List.copyOf(explosions);
    }

    public @NotNull FireworkList withFlightDuration(int flightDuration) {
        return new FireworkList(flightDuration, explosions);
    }

    public @NotNull FireworkList withExplosions(@NotNull List<FireworkExplosion> explosions) {
        return new FireworkList(flightDuration, explosions);
    }
}
