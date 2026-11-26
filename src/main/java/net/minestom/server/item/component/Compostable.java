package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.loot.number.ResolvableInt;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

/**
 * Marks an item as usable in a composter.
 *
 * @param layers how many layers the item adds to a composter, a number or the key of a provider that computes it
 */
public record Compostable(ResolvableInt layers) {
    public static final NetworkBuffer.Type<Compostable> NETWORK_TYPE = NetworkBufferTemplate.template(
            ResolvableInt.NETWORK_TYPE, Compostable::layers,
            Compostable::new);
    public static final Codec<Compostable> CODEC = StructCodec.struct(
            "layers", ResolvableInt.CODEC, Compostable::layers,
            Compostable::new);

    /**
     * Creates a Compostable with a fixed layer count.
     *
     * @param layers how many layers the item adds to a composter
     */
    public Compostable(int layers) {
        this(ResolvableInt.of(layers));
    }
}
