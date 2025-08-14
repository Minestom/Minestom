package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

import java.util.List;

public record DeathProtection(List<ConsumeEffect> deathEffects) {
    public static final NetworkBuffer.Type<DeathProtection> NETWORK_TYPE = NetworkBufferTemplate.template(
            ConsumeEffect.NETWORK_TYPE.list(256), DeathProtection::deathEffects,
            DeathProtection::new);
    public static final Codec<DeathProtection> CODEC = StructCodec.struct(
            "death_effects", ConsumeEffect.CODEC.list().optional(List.of()), DeathProtection::deathEffects,
            DeathProtection::new);
}
