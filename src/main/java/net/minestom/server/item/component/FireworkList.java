package net.minestom.server.item.component;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record FireworkList(byte flightDuration, @NotNull List<FireworkExplosion> explosions) {
    public static final FireworkList EMPTY = new FireworkList((byte) 0, List.of());

    public static final NetworkBuffer.Type<FireworkList> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, FireworkList value) {
            buffer.write(NetworkBuffer.BYTE, value.flightDuration);
            buffer.writeCollection(FireworkExplosion.NETWORK_TYPE, value.explosions);
        }

        @Override
        public FireworkList read(@NotNull NetworkBuffer buffer) {
            return new FireworkList(buffer.read(NetworkBuffer.BYTE),
                    buffer.readCollection(FireworkExplosion.NETWORK_TYPE, 256));
        }
    };

    public static final BinaryTagSerializer<FireworkList> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                byte flightDuration = tag.getByte("flight_duration");
                ListBinaryTag explosionsTag = tag.getList("explosions", BinaryTagTypes.COMPOUND);
                List<FireworkExplosion> explosions = new ArrayList<>(explosionsTag.size());
                for (BinaryTag explosionTag : explosionsTag)
                    explosions.add(FireworkExplosion.NBT_TYPE.read(explosionTag));
                return new FireworkList(flightDuration, explosions);
            },
            value -> {
                ListBinaryTag.Builder<BinaryTag> explosionsTag = ListBinaryTag.builder();
                for (FireworkExplosion explosion : value.explosions)
                    explosionsTag.add(FireworkExplosion.NBT_TYPE.write(explosion));
                return CompoundBinaryTag.builder()
                        .putByte("flight_duration", value.flightDuration)
                        .put("explosions", explosionsTag.build())
                        .build();
            }
    );

    public FireworkList {
        explosions = List.copyOf(explosions);
    }

    public @NotNull FireworkList withFlightDuration(byte flightDuration) {
        return new FireworkList(flightDuration, explosions);
    }

    public @NotNull FireworkList withExplosions(@NotNull List<FireworkExplosion> explosions) {
        return new FireworkList(flightDuration, explosions);
    }
}
