package net.minestom.server.item.component;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.TagReadable;
import org.jetbrains.annotations.UnknownNullability;

public record CustomData(CompoundBinaryTag nbt) implements TagReadable {
    public static final CustomData EMPTY = new CustomData(CompoundBinaryTag.empty());

    public static final NetworkBuffer.Type<CustomData> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(NetworkBuffer buffer, CustomData value) {
            buffer.write(NetworkBuffer.NBT, value.nbt);
        }

        @Override
        public CustomData read(NetworkBuffer buffer) {
            return new CustomData((CompoundBinaryTag) buffer.read(NetworkBuffer.NBT));
        }
    };

    public static final Codec<CustomData> CODEC = Codec.NBT_COMPOUND
            .transform(CustomData::new, CustomData::nbt);

    @Override
    public <T> @UnknownNullability T getTag(Tag<T> tag) {
        final TagHandler tagHandler = TagHandler.fromCompound(nbt);
        return tagHandler.getTag(tag);
    }

    public <T> CustomData withTag(Tag<T> tag, T value) {
        TagHandler tagHandler = TagHandler.fromCompound(nbt);
        tagHandler.setTag(tag, value);
        return new CustomData(tagHandler.asCompound());
    }
}
