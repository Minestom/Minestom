package net.minestom.server.item.component;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

public record ProvidesTrimMaterial(@NotNull Key key) {
    // This can be either a key or a holder of trim material. we need to support holders better.

    public static final NetworkBuffer.Type<ProvidesTrimMaterial> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, ProvidesTrimMaterial value) {
            buffer.write(NetworkBuffer.BOOLEAN, false);
            buffer.write(NetworkBuffer.STRING, value.key.asString());
        }

        @Override
        public ProvidesTrimMaterial read(@NotNull NetworkBuffer buffer) {
            if (buffer.read(NetworkBuffer.BOOLEAN))
                throw new IllegalArgumentException("Cannot read direct trim material");
            return new ProvidesTrimMaterial(Key.key(buffer.read(NetworkBuffer.STRING)));
        }
    };
    public static final Codec<ProvidesTrimMaterial> CODEC = Codec.KEY.transform(ProvidesTrimMaterial::new, ProvidesTrimMaterial::key);
}
