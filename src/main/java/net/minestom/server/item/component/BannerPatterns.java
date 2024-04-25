package net.minestom.server.item.component;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.color.DyeColor;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record BannerPatterns(@NotNull List<Layer> layers) {
    public static final int MAX_LAYERS = 1024;

    //    public static final NetworkBuffer.Type<BannerPatterns> NETWORK_TYPE = Layer.NETWORK_TYPE.list(MAX_LAYERS).map(BannerPatterns::new, BannerPatterns::layers);
    public static final NetworkBuffer.Type<BannerPatterns> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, BannerPatterns value) {
            buffer.write(NetworkBuffer.VAR_INT, 0);
        }

        @Override
        public BannerPatterns read(@NotNull NetworkBuffer buffer) {
            throw new UnsupportedOperationException("todo, banner pattern registry");
        }
    };
    public static final BinaryTagSerializer<BannerPatterns> NBT_TYPE = Layer.NBT_TYPE.list().map(BannerPatterns::new, BannerPatterns::layers);

    public record Layer(@NotNull String pattern, @NotNull DyeColor color) {
        public static final NetworkBuffer.Type<Layer> NETWORK_TYPE = new NetworkBuffer.Type<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, Layer value) {
                throw new UnsupportedOperationException("todo, banner pattern registry");
            }

            @Override
            public Layer read(@NotNull NetworkBuffer buffer) {
                throw new UnsupportedOperationException("todo, banner pattern registry");
            }
        };
        public static final BinaryTagSerializer<Layer> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
                tag -> new Layer(tag.getString("pattern"), DyeColor.NBT_TYPE.read(tag.get("color"))),
                layer -> CompoundBinaryTag.builder()
                        .putString("pattern", layer.pattern)
                        .put("color", DyeColor.NBT_TYPE.write(layer.color))
                        .build()
        );
    }
}
