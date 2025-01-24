package net.minestom.server.item.component;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.color.DyeColor;
import net.minestom.server.instance.block.banner.BannerPattern;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record BannerPatterns(@NotNull List<Layer> layers) {
    public static final int MAX_LAYERS = 1024;

    public static final NetworkBuffer.Type<BannerPatterns> NETWORK_TYPE = Layer.NETWORK_TYPE.list(MAX_LAYERS).transform(BannerPatterns::new, BannerPatterns::layers);
    public static final BinaryTagSerializer<BannerPatterns> NBT_TYPE = Layer.NBT_TYPE.list().map(BannerPatterns::new, BannerPatterns::layers);

    public record Layer(@NotNull DynamicRegistry.Key<BannerPattern> pattern, @NotNull DyeColor color) {
        public static final NetworkBuffer.Type<Layer> NETWORK_TYPE = NetworkBufferTemplate.template(
                BannerPattern.NETWORK_TYPE, Layer::pattern,
                DyeColor.NETWORK_TYPE, Layer::color,
                Layer::new
        );
        public static final BinaryTagSerializer<Layer> NBT_TYPE = new BinaryTagSerializer<Layer>() {
            @Override
            public @NotNull BinaryTag write(@NotNull Context context, @NotNull Layer value) {
                return CompoundBinaryTag.builder()
                        .put("pattern", BannerPattern.NBT_TYPE.write(value.pattern))
                        .put("color", DyeColor.NBT_TYPE.write(value.color))
                        .build();
            }

            @Override
            public @NotNull Layer read(@NotNull Context context, @NotNull BinaryTag tag) {
                if (!(tag instanceof CompoundBinaryTag compound))
                    throw new IllegalArgumentException("Expected a compound tag");
                return new Layer(BannerPattern.NBT_TYPE.read(context, compound.get("pattern")),
                        DyeColor.NBT_TYPE.read(context, compound.get("color")));
            }
        };
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
