package net.minestom.server.item.component;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public record CustomData(@NotNull CompoundBinaryTag nbt) implements TagReadable {
    public static final CustomData EMPTY = new CustomData(CompoundBinaryTag.empty());

    public static final NetworkBuffer.Type<CustomData> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, CustomData value) {
            buffer.write(NetworkBuffer.NBT, value.nbt);
        }

        @Override
        public CustomData read(@NotNull NetworkBuffer buffer) {
            return new CustomData((CompoundBinaryTag) buffer.read(NetworkBuffer.NBT));
        }
    };

    public static final BinaryTagSerializer<CustomData> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(CustomData::new, CustomData::nbt);

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return tag.read(nbt);
    }

    public <T> @NotNull CustomData withTag(@NotNull Tag<T> tag, T value) {
        CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
        builder.put(nbt);
        tag.write(builder, value);
        return new CustomData(builder.build());
    }
}
