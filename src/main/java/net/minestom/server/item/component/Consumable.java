package net.minestom.server.item.component;

import net.minestom.server.ServerFlag;
import net.minestom.server.item.ItemAnimation;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.nbt.BinaryTagTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record Consumable(
        float consumeSeconds,
        @NotNull ItemAnimation animation,
        @NotNull SoundEvent sound,
        boolean hasConsumeParticles,
        @NotNull List<ConsumeEffect> effects
) {
    public static final float DEFAULT_CONSUME_SECONDS = 1.6f;

    public static final NetworkBuffer.Type<Consumable> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.FLOAT, Consumable::consumeSeconds,
            ItemAnimation.NETWORK_TYPE, Consumable::animation,
            SoundEvent.NETWORK_TYPE, Consumable::sound,
            NetworkBuffer.BOOLEAN, Consumable::hasConsumeParticles,
            ConsumeEffect.NETWORK_TYPE.list(Short.MAX_VALUE), Consumable::effects,
            Consumable::new);
    public static final BinaryTagSerializer<Consumable> NBT_TYPE = BinaryTagTemplate.object(
            "consume_seconds", BinaryTagSerializer.FLOAT.optional(DEFAULT_CONSUME_SECONDS), Consumable::consumeSeconds,
            "animation", ItemAnimation.NBT_TYPE.optional(ItemAnimation.EAT), Consumable::animation,
            "sound", SoundEvent.NBT_TYPE.optional(SoundEvent.ENTITY_GENERIC_EAT), Consumable::sound,
            "has_consume_particles", BinaryTagSerializer.BOOLEAN.optional(true), Consumable::hasConsumeParticles,
            "on_consume_effects", ConsumeEffect.NBT_TYPE.list().optional(List.of()), Consumable::effects,
            Consumable::new);

    public int consumeTicks() {
        return (int) (consumeSeconds * ServerFlag.SERVER_TICKS_PER_SECOND);
    }
}
