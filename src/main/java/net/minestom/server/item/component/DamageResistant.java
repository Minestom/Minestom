package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.NotNull;

public record DamageResistant(@NotNull String tagKey) {
    public static final NetworkBuffer.Type<DamageResistant> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.STRING, DamageResistant::tagKey,
            DamageResistant::new);
    public static final Codec<DamageResistant> CODEC = StructCodec.struct(
            "types", Codec.STRING, DamageResistant::tagKey,
            DamageResistant::new);
}
