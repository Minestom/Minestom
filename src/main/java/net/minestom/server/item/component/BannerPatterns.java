package net.minestom.server.item.component;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.color.DyeColor;
import net.minestom.server.instance.block.banner.BannerPattern;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record BannerPatterns(@NotNull List<Layer> layers) {
    public static final int MAX_LAYERS = 1024;

    public static final NetworkBuffer.Type<BannerPatterns> NETWORK_TYPE = Layer.NETWORK_TYPE.list(MAX_LAYERS).map(BannerPatterns::new, BannerPatterns::layers);
    public static final BinaryTagSerializer<BannerPatterns> NBT_TYPE = Layer.NBT_TYPE.list().map(BannerPatterns::new, BannerPatterns::layers);

    public record Layer(@NotNull DynamicRegistry.Key<BannerPattern> pattern, @NotNull DyeColor color) {
        public static final NetworkBuffer.Type<Layer> NETWORK_TYPE = new NetworkBuffer.Type<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, Layer value) {
                buffer.write(BannerPattern.NETWORK_TYPE, value.pattern);
                buffer.write(DyeColor.NETWORK_TYPE, value.color);
            }

            @Override
            public Layer read(@NotNull NetworkBuffer buffer) {
                return new Layer(buffer.read(BannerPattern.NETWORK_TYPE), buffer.read(DyeColor.NETWORK_TYPE));
            }
        };
        public static final BinaryTagSerializer<Layer> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
                tag -> new Layer(BannerPattern.NBT_TYPE.read(tag.get("pattern")), DyeColor.NBT_TYPE.read(tag.get("color"))),
                layer -> CompoundBinaryTag.builder()
                        .put("pattern", BannerPattern.NBT_TYPE.write(layer.pattern))
                        .put("color", DyeColor.NBT_TYPE.write(layer.color))
                        .build()
        );
    }

    public BannerPatterns {
        layers = List.copyOf(layers);
    }

    public BannerPatterns(@NotNull Layer layer) {
        this(List.of(layer));
    }

    public BannerPatterns(@NotNull DynamicRegistry.Key<BannerPattern> pattern, @NotNull DyeColor color) {
        this(new Layer(pattern, color));
    }

    public @NotNull BannerPatterns with(@NotNull Layer layer) {
        List<Layer> layers = new ArrayList<>(this.layers);
        layers.add(layer);
        return new BannerPatterns(layers);
    }
}
