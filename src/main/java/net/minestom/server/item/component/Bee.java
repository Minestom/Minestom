package net.minestom.server.item.component;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

public record Bee(@NotNull CustomData entityData, int ticksInHive, int minTicksInHive) {

    public static @NotNull NetworkBuffer.Type<Bee> NETWORK_TYPE = new NetworkBuffer.Type<Bee>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Bee value) {
            buffer.write(CustomData.NETWORK_TYPE, value.entityData);
            buffer.write(NetworkBuffer.VAR_INT, value.ticksInHive);
            buffer.write(NetworkBuffer.VAR_INT, value.minTicksInHive);
        }

        @Override
        public Bee read(@NotNull NetworkBuffer buffer) {
            return new Bee(buffer.read(CustomData.NETWORK_TYPE),
                    buffer.read(NetworkBuffer.VAR_INT),
                    buffer.read(NetworkBuffer.VAR_INT));
        }
    };
    public static @NotNull BinaryTagSerializer<Bee> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> new Bee(CustomData.NBT_TYPE.read(tag.getCompound("entity_data")),
                    tag.getInt("ticks_in_hive"),
                    tag.getInt("min_ticks_in_hive")),
            value -> CompoundBinaryTag.builder()
                    .put("entity_data", CustomData.NBT_TYPE.write(value.entityData))
                    .putInt("ticks_in_hive", value.ticksInHive)
                    .putInt("min_ticks_in_hive", value.minTicksInHive)
                    .build()
    );

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
