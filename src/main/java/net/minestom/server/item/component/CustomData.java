package net.minestom.server.item.component;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public record CustomData(@NotNull NBTCompound nbt) implements ItemComponent {
    static final Tag<CustomData> TAG = Tag.Structure("ab", CustomData.class);

    static final NetworkBuffer.Type<CustomData> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, CustomData value) {
            buffer.write(NetworkBuffer.NBT, value.nbt);
        }

        @Override
        public CustomData read(@NotNull NetworkBuffer buffer) {
            return new CustomData((NBTCompound) buffer.read(NetworkBuffer.NBT));
        }
    };

}
