package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jspecify.annotations.Nullable;

public record UseCooldown(float seconds, @Nullable String cooldownGroup) {
    public static final NetworkBuffer.Type<UseCooldown> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.FLOAT, UseCooldown::seconds,
            NetworkBuffer.STRING.optional(), UseCooldown::cooldownGroup,
            UseCooldown::new);
    public static final Codec<UseCooldown> CODEC = StructCodec.struct(
            "seconds", Codec.FLOAT, UseCooldown::seconds,
            "cooldown_group", Codec.STRING.optional(), UseCooldown::cooldownGroup,
            UseCooldown::new);
}
