package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.color.DyeColor;
import net.minestom.server.instance.block.banner.BannerPattern;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.Holder;

import java.util.ArrayList;
import java.util.List;

public record BannerPatterns(List<Layer> layers) {
    public static final int MAX_LAYERS = 1024;

    public static final NetworkBuffer.Type<BannerPatterns> NETWORK_TYPE = Layer.NETWORK_TYPE.list(MAX_LAYERS).transform(BannerPatterns::new, BannerPatterns::layers);
    public static final Codec<BannerPatterns> CODEC = Layer.CODEC.list().transform(BannerPatterns::new, BannerPatterns::layers);

    public record Layer(Holder<BannerPattern> pattern, DyeColor color) {
        public static final NetworkBuffer.Type<Layer> NETWORK_TYPE = NetworkBufferTemplate.template(
                BannerPattern.HOLDER_NETWORK_TYPE, Layer::pattern,
                DyeColor.NETWORK_TYPE, Layer::color,
                Layer::new);
        public static final Codec<Layer> CODEC = StructCodec.struct(
                "pattern", BannerPattern.HOLDER_CODEC, Layer::pattern,
                "color", DyeColor.CODEC, Layer::color,
                Layer::new);
    }

    public BannerPatterns {
        layers = List.copyOf(layers);
    }

    public BannerPatterns(Layer layer) {
        this(List.of(layer));
    }

    public BannerPatterns(Holder<BannerPattern> pattern, DyeColor color) {
        this(new Layer(pattern, color));
    }

    public BannerPatterns with(Layer layer) {
        List<Layer> layers = new ArrayList<>(this.layers);
        layers.add(layer);
        return new BannerPatterns(layers);
    }
}
