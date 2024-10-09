package net.minestom.server.item.component;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record Consumable(
        float consumeSeconds,
        @NotNull Animation animation,
        @NotNull SoundEvent sound,
        boolean hasParticles,
        @NotNull List<ConsumeEffect> effects
) {
    public static final NetworkBuffer.Type<Consumable> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.FLOAT, Consumable::consumeSeconds,
            Animation.NETWORK_TYPE, Consumable::animation,
            SoundEvent.NETWORK_TYPE, Consumable::sound,
            NetworkBuffer.BOOLEAN, Consumable::hasParticles,
            ConsumeEffect.NETWORK_TYPE.list(Short.MAX_VALUE), Consumable::effects,
            Consumable::new);
    public static final BinaryTagSerializer<Consumable> NBT_TYPE = BinaryTagSerializer.object(
            "consume_seconds", BinaryTagSerializer.FLOAT, Consumable::consumeSeconds,
            "animation", Animation.NBT_TYPE, Consumable::animation,
            "sound", SoundEvent.NBT_TYPE, Consumable::sound,
            "has_particles", BinaryTagSerializer.BOOLEAN, Consumable::hasParticles,
            "effects", ConsumeEffect.NBT_TYPE.list(), Consumable::effects,
            Consumable::new);

    public enum Animation {
        NONE,
        EAT,
        DRINK,
        BLOCK,
        BOW,
        SPEAR,
        CROSSBOW,
        SPYGLASS,
        TOOT_HORN,
        BRUSH;

        public static final NetworkBuffer.Type<Animation> NETWORK_TYPE = NetworkBuffer.Enum(Animation.class);
        public static final BinaryTagSerializer<Animation> NBT_TYPE = BinaryTagSerializer.fromEnumStringable(Animation.class);
    }
}
