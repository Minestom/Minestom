package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.potion.CustomPotionEffect;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.registry.ObjectSet;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public sealed interface ConsumeEffect {
    NetworkBuffer.Type<ConsumeEffect> NETWORK_TYPE = ConsumeEffectType.NETWORK_TYPE
            .unionType(ConsumeEffect::networkType, ConsumeEffect::consumeEffectToType);
    Codec<ConsumeEffect> CODEC = ConsumeEffectType.CODEC
            .unionType(ConsumeEffect::nbtType, ConsumeEffect::consumeEffectToType);

    record ApplyEffects(@NotNull List<CustomPotionEffect> effects, float probability) implements ConsumeEffect {
        private static final int MAX_EFFECTS = 256;

        public static final NetworkBuffer.Type<ApplyEffects> NETWORK_TYPE = NetworkBufferTemplate.template(
                CustomPotionEffect.NETWORK_TYPE.list(MAX_EFFECTS), ApplyEffects::effects,
                NetworkBuffer.FLOAT, ApplyEffects::probability,
                ApplyEffects::new);
        public static final Codec<ApplyEffects> CODEC = StructCodec.struct(
                "effects", CustomPotionEffect.CODEC.list(), ApplyEffects::effects,
                "probability", Codec.FLOAT.optional(1f), ApplyEffects::probability,
                ApplyEffects::new);

        public ApplyEffects {
            Check.argCondition(probability < 0 || probability > 1, "Probability must be between 0 and 1");
            effects = List.copyOf(effects);
        }

        public ApplyEffects(@NotNull CustomPotionEffect effect, float probability) {
            this(List.of(effect), probability);
        }
    }

    record RemoveEffects(@NotNull ObjectSet<PotionEffect> effects) implements ConsumeEffect {
        public static final NetworkBuffer.Type<RemoveEffects> NETWORK_TYPE = NetworkBufferTemplate.template(
                ObjectSet.networkType(Tag.BasicType.POTION_EFFECTS), RemoveEffects::effects,
                RemoveEffects::new);
        public static final Codec<RemoveEffects> CODEC = StructCodec.struct(
                "effects", ObjectSet.codec(Tag.BasicType.POTION_EFFECTS), RemoveEffects::effects,
                RemoveEffects::new);
    }

    final class ClearAllEffects implements ConsumeEffect {
        public static final ClearAllEffects INSTANCE = new ClearAllEffects();

        public static final NetworkBuffer.Type<ClearAllEffects> NETWORK_TYPE = NetworkBuffer.UNIT
                .transform(buffer -> INSTANCE, ignored -> null);
        public static final Codec<ClearAllEffects> CODEC = Codec.UNIT
                .transform(ignored -> INSTANCE, ignored -> null);

        private ClearAllEffects() {
        }
    }

    record TeleportRandomly(float diameter) implements ConsumeEffect {
        public static final float DEFAULT_DIAMETER = 16.0f;

        public static final NetworkBuffer.Type<TeleportRandomly> NETWORK_TYPE = NetworkBufferTemplate.template(
                NetworkBuffer.FLOAT, TeleportRandomly::diameter,
                TeleportRandomly::new);
        public static final Codec<TeleportRandomly> CODEC = StructCodec.struct(
                "diameter", Codec.FLOAT.optional(DEFAULT_DIAMETER), TeleportRandomly::diameter,
                TeleportRandomly::new);

        public TeleportRandomly() {
            this(DEFAULT_DIAMETER);
        }
    }

    record PlaySound(@NotNull SoundEvent sound) implements ConsumeEffect {
        public static final NetworkBuffer.Type<PlaySound> NETWORK_TYPE = NetworkBufferTemplate.template(
                SoundEvent.NETWORK_TYPE, PlaySound::sound,
                PlaySound::new);
        public static final Codec<PlaySound> CODEC = StructCodec.struct(
                "sound", SoundEvent.CODEC, PlaySound::sound,
                PlaySound::new);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static NetworkBuffer.Type<ConsumeEffect> networkType(@NotNull ConsumeEffectType type) {
        return (NetworkBuffer.Type) switch (type) {
            case APPLY_EFFECTS -> ApplyEffects.NETWORK_TYPE;
            case REMOVE_EFFECTS -> RemoveEffects.NETWORK_TYPE;
            case CLEAR_ALL_EFFECTS -> ClearAllEffects.NETWORK_TYPE;
            case TELEPORT_RANDOMLY -> TeleportRandomly.NETWORK_TYPE;
            case PLAY_SOUND -> PlaySound.NETWORK_TYPE;
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Codec<ConsumeEffect> nbtType(@NotNull ConsumeEffectType type) {
        return (Codec) switch (type) {
            case APPLY_EFFECTS -> ApplyEffects.CODEC;
            case REMOVE_EFFECTS -> RemoveEffects.CODEC;
            case CLEAR_ALL_EFFECTS -> ClearAllEffects.CODEC;
            case TELEPORT_RANDOMLY -> TeleportRandomly.CODEC;
            case PLAY_SOUND -> PlaySound.CODEC;
        };
    }

    private static ConsumeEffectType consumeEffectToType(@NotNull ConsumeEffect consumeEffect) {
        return switch (consumeEffect) {
            case ApplyEffects ignored -> ConsumeEffectType.APPLY_EFFECTS;
            case RemoveEffects ignored -> ConsumeEffectType.REMOVE_EFFECTS;
            case ClearAllEffects ignored -> ConsumeEffectType.CLEAR_ALL_EFFECTS;
            case TeleportRandomly ignored -> ConsumeEffectType.TELEPORT_RANDOMLY;
            case PlaySound ignored -> ConsumeEffectType.PLAY_SOUND;
        };
    }

}
