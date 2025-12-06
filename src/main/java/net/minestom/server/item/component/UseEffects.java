package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

public record UseEffects(
        boolean canSprint,
        boolean interactVibrations,
        float speedMultiplier
) {
    public static final UseEffects DEFAULT = new UseEffects(true, true, 0.2f);

    public static final NetworkBuffer.Type<UseEffects> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.BOOLEAN, UseEffects::canSprint,
            NetworkBuffer.BOOLEAN, UseEffects::interactVibrations,
            NetworkBuffer.FLOAT, UseEffects::speedMultiplier,
            UseEffects::new);
    public static final Codec<UseEffects> CODEC = StructCodec.struct(
            "can_sprint", Codec.BOOLEAN.optional(true), UseEffects::canSprint,
            "interact_vibrations", Codec.BOOLEAN.optional(true), UseEffects::interactVibrations,
            "speed_multiplier", Codec.FLOAT.optional(0.2f), UseEffects::speedMultiplier,
            UseEffects::new);
}
