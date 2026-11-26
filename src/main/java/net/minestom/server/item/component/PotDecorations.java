package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.ItemStackTemplate;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.Nullable;

/**
 * The sherds shown on each side of a decorated pot. A null side is drawn as plain terracotta.
 */
public record PotDecorations(
        @Nullable ItemStack back,
        @Nullable ItemStack left,
        @Nullable ItemStack right,
        @Nullable ItemStack front
) {
    public static final PotDecorations EMPTY = new PotDecorations(null, null, null, null);

    public static final NetworkBuffer.Type<PotDecorations> NETWORK_TYPE = NetworkBufferTemplate.template(
            ItemStackTemplate.NETWORK_TYPE.optional(), PotDecorations::back,
            ItemStackTemplate.NETWORK_TYPE.optional(), PotDecorations::left,
            ItemStackTemplate.NETWORK_TYPE.optional(), PotDecorations::right,
            ItemStackTemplate.NETWORK_TYPE.optional(), PotDecorations::front,
            PotDecorations::new);
    public static final Codec<PotDecorations> NBT_TYPE = StructCodec.struct(
            "back", ItemStackTemplate.CODEC.optional(), PotDecorations::back,
            "left", ItemStackTemplate.CODEC.optional(), PotDecorations::left,
            "right", ItemStackTemplate.CODEC.optional(), PotDecorations::right,
            "front", ItemStackTemplate.CODEC.optional(), PotDecorations::front,
            PotDecorations::new);

    public PotDecorations(@Nullable ItemStack sherd) {
        this(sherd, sherd, sherd, sherd);
    }
}
