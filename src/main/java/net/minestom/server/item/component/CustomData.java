package net.minestom.server.item.component;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

public record CustomData(@NotNull CompoundBinaryTag nbt) implements ItemComponent {
    static final Tag<CustomData> TAG = Tag.Structure("ab", CustomData.class);

    static final NetworkBuffer.Type<CustomData> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, CustomData value) {
            buffer.write(NetworkBuffer.NBT, value.nbt);
        }

        @Override
        public CustomData read(@NotNull NetworkBuffer buffer) {
            return new CustomData((CompoundBinaryTag) buffer.read(NetworkBuffer.NBT));
        }
    };

}
