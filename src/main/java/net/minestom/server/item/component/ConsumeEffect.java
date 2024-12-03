package net.minestom.server.item.component;

import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.potion.CustomPotionEffect;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.registry.ObjectSet;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.nbt.BinaryTagTemplate;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public sealed interface ConsumeEffect {
    NetworkBuffer.Type<ConsumeEffect> NETWORK_TYPE = ConsumeEffectType.NETWORK_TYPE
            .unionType(ConsumeEffect::networkType, ConsumeEffect::consumeEffectToType);
    BinaryTagSerializer<ConsumeEffect> NBT_TYPE = ConsumeEffectType.NBT_TYPE
            .unionType(ConsumeEffect::nbtType, ConsumeEffect::consumeEffectToType);

    record ApplyEffects(@NotNull List<CustomPotionEffect> effects, float probability) implements ConsumeEffect {
        private static final int MAX_EFFECTS = 256;

        public static final NetworkBuffer.Type<ApplyEffects> NETWORK_TYPE = NetworkBufferTemplate.template(
                CustomPotionEffect.NETWORK_TYPE.list(MAX_EFFECTS), ApplyEffects::effects,
                NetworkBuffer.FLOAT, ApplyEffects::probability,
                ApplyEffects::new);
        public static final BinaryTagSerializer<ApplyEffects> NBT_TYPE = BinaryTagTemplate.object(
                "effects", CustomPotionEffect.NBT_TYPE.list(), ApplyEffects::effects,
                "probability", BinaryTagSerializer.FLOAT.optional(1f), ApplyEffects::probability,
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
        public static final BinaryTagSerializer<RemoveEffects> NBT_TYPE = BinaryTagTemplate.object(
                "effects", ObjectSet.nbtType(Tag.BasicType.POTION_EFFECTS), RemoveEffects::effects,
                RemoveEffects::new);
    }

    final class ClearAllEffects implements ConsumeEffect {
        public static final ClearAllEffects INSTANCE = new ClearAllEffects();

        public static final NetworkBuffer.Type<ClearAllEffects> NETWORK_TYPE = NetworkBuffer.UNIT
                .transform(buffer -> INSTANCE, ignored -> null);
        public static final BinaryTagSerializer<ClearAllEffects> NBT_TYPE = BinaryTagSerializer.UNIT
                .map(ignored -> INSTANCE, ignored -> null);

        private ClearAllEffects() {}
    }

    record TeleportRandomly(float diameter) implements ConsumeEffect {
        public static final float DEFAULT_DIAMETER = 16.0f;

        public static final NetworkBuffer.Type<TeleportRandomly> NETWORK_TYPE = NetworkBufferTemplate.template(
                NetworkBuffer.FLOAT, TeleportRandomly::diameter,
                TeleportRandomly::new);
        public static final BinaryTagSerializer<TeleportRandomly> NBT_TYPE = BinaryTagTemplate.object(
                "diameter", BinaryTagSerializer.FLOAT.optional(DEFAULT_DIAMETER), TeleportRandomly::diameter,
                TeleportRandomly::new);

        public TeleportRandomly() {
            this(DEFAULT_DIAMETER);
        }
    }

    record PlaySound(@NotNull SoundEvent sound) implements ConsumeEffect {
        public static final NetworkBuffer.Type<PlaySound> NETWORK_TYPE = NetworkBufferTemplate.template(
                SoundEvent.NETWORK_TYPE, PlaySound::sound,
                PlaySound::new);
        public static final BinaryTagSerializer<PlaySound> NBT_TYPE = BinaryTagTemplate.object(
                "sound", SoundEvent.NBT_TYPE, PlaySound::sound,
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
    private static BinaryTagSerializer<ConsumeEffect> nbtType(@NotNull ConsumeEffectType type) {
        return (BinaryTagSerializer) switch (type) {
            case APPLY_EFFECTS -> ApplyEffects.NBT_TYPE;
            case REMOVE_EFFECTS -> RemoveEffects.NBT_TYPE;
            case CLEAR_ALL_EFFECTS -> ClearAllEffects.NBT_TYPE;
            case TELEPORT_RANDOMLY -> TeleportRandomly.NBT_TYPE;
            case PLAY_SOUND -> PlaySound.NBT_TYPE;
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
