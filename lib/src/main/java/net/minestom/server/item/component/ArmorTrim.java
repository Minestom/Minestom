package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.item.armor.TrimMaterial;
import net.minestom.server.item.armor.TrimPattern;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.Holder;

public record ArmorTrim(
        Holder<TrimMaterial> material,
        Holder<TrimPattern> pattern
) {

    public static final NetworkBuffer.Type<ArmorTrim> NETWORK_TYPE = NetworkBufferTemplate.template(
            TrimMaterial.NETWORK_TYPE, ArmorTrim::material,
            TrimPattern.NETWORK_TYPE, ArmorTrim::pattern,
            ArmorTrim::new);
    public static final Codec<ArmorTrim> CODEC = StructCodec.struct(
            "material", TrimMaterial.CODEC, ArmorTrim::material,
            "pattern", TrimPattern.CODEC, ArmorTrim::pattern,
            ArmorTrim::new);

}
