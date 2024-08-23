package net.minestom.server.item.component;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record SuspiciousStewEffects(@NotNull List<Effect> effects) {
    public static final int DEFAULT_DURATION = 160;
    public static final SuspiciousStewEffects EMPTY = new SuspiciousStewEffects(List.of());

    public static final NetworkBuffer.Type<SuspiciousStewEffects> NETWORK_TYPE = Effect.NETWORK_TYPE.list(Short.MAX_VALUE).transform(SuspiciousStewEffects::new, SuspiciousStewEffects::effects);
    public static final BinaryTagSerializer<SuspiciousStewEffects> NBT_TYPE = Effect.NBT_TYPE.list().map(SuspiciousStewEffects::new, SuspiciousStewEffects::effects);

    public SuspiciousStewEffects {
        effects = List.copyOf(effects);
    }

    public SuspiciousStewEffects(@NotNull Effect effect) {
        this(List.of(effect));
    }

    public @NotNull SuspiciousStewEffects with(@NotNull Effect effect) {
        List<Effect> newEffects = new ArrayList<>(effects);
        newEffects.add(effect);
        return new SuspiciousStewEffects(newEffects);
    }

    public record Effect(@NotNull PotionEffect id, int durationTicks) {

        public static final NetworkBuffer.Type<Effect> NETWORK_TYPE = NetworkBufferTemplate.template(
                PotionEffect.NETWORK_TYPE, Effect::id,
                NetworkBuffer.VAR_INT, Effect::durationTicks,
                Effect::new
        );

        public static final BinaryTagSerializer<Effect> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
                tag -> new Effect(PotionEffect.fromNamespaceId(tag.getString("id")),
                        tag.getInt("duration", DEFAULT_DURATION)),
                value -> CompoundBinaryTag.builder()
                        .putString("id", value.id.name())
                        .putInt("duration", value.durationTicks)
                        .build()
        );

        public Effect(@NotNull PotionEffect id) {
            this(id, DEFAULT_DURATION);
        }
    }
}
