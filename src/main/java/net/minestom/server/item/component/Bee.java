package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.NotNull;

public record Bee(@NotNull CustomData entityData, int ticksInHive, int minTicksInHive) {
    public static final NetworkBuffer.Type<Bee> NETWORK_TYPE = NetworkBufferTemplate.template(
            CustomData.NETWORK_TYPE, Bee::entityData,
            NetworkBuffer.VAR_INT, Bee::ticksInHive,
            NetworkBuffer.VAR_INT, Bee::minTicksInHive,
            Bee::new);
    public static final Codec<Bee> CODEC = StructCodec.struct(
            "entity_data", CustomData.CODEC, Bee::entityData,
            "ticks_in_hive", Codec.INT, Bee::ticksInHive,
            "min_ticks_in_hive", Codec.INT, Bee::minTicksInHive,
            Bee::new);

    public @NotNull Bee withEntityData(@NotNull CustomData entityData) {
        return new Bee(entityData, ticksInHive, minTicksInHive);
    }

    public @NotNull Bee withTicksInHive(int ticksInHive) {
        return new Bee(entityData, ticksInHive, minTicksInHive);
    }

    public @NotNull Bee withMinTicksInHive(int minTicksInHive) {
        return new Bee(entityData, ticksInHive, minTicksInHive);
    }
}
