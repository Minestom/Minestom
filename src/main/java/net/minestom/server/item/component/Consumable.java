package net.minestom.server.item.component;

import net.minestom.server.ServerFlag;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.item.ItemAnimation;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.sound.SoundEvent;

import java.util.List;

public record Consumable(
        float consumeSeconds,
        ItemAnimation animation,
        SoundEvent sound,
        boolean hasConsumeParticles,
        List<ConsumeEffect> effects
) {
    public static final float DEFAULT_CONSUME_SECONDS = 1.6f;

    public static final NetworkBuffer.Type<Consumable> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.FLOAT, Consumable::consumeSeconds,
            ItemAnimation.NETWORK_TYPE, Consumable::animation,
            SoundEvent.NETWORK_TYPE, Consumable::sound,
            NetworkBuffer.BOOLEAN, Consumable::hasConsumeParticles,
            ConsumeEffect.NETWORK_TYPE.list(Short.MAX_VALUE), Consumable::effects,
            Consumable::new);
    public static final Codec<Consumable> CODEC = StructCodec.struct(
            "consume_seconds", Codec.FLOAT.optional(DEFAULT_CONSUME_SECONDS), Consumable::consumeSeconds,
            "animation", ItemAnimation.CODEC.optional(ItemAnimation.EAT), Consumable::animation,
            "sound", SoundEvent.CODEC.optional(SoundEvent.ENTITY_GENERIC_EAT), Consumable::sound,
            "has_consume_particles", Codec.BOOLEAN.optional(true), Consumable::hasConsumeParticles,
            "on_consume_effects", ConsumeEffect.CODEC.list().optional(List.of()), Consumable::effects,
            Consumable::new);

    public int consumeTicks() {
        return (int) (consumeSeconds * ServerFlag.SERVER_TICKS_PER_SECOND);
    }
}
