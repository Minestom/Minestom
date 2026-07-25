package net.minestom.server.entity.metadata.cube;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.item.Material;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.*;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public sealed interface SulfurCubeArchetype extends SulfurCubeArchetypes permits SulfurCubeArchetypeImpl {
    NetworkBuffer.Type<RegistryKey<SulfurCubeArchetype>> NETWORK_TYPE = RegistryKey.networkType(
            Registries::sulfurCubeArchetype);
    Codec<RegistryKey<SulfurCubeArchetype>> CODEC = RegistryKey.codec(Registries::sulfurCubeArchetype);

    Codec<SulfurCubeArchetype> REGISTRY_CODEC = StructCodec.struct(
            "items", RegistryTag.codec(Registries::material), SulfurCubeArchetype::items,
            "attribute_modifiers", AttributeEntry.CODEC.list(), SulfurCubeArchetype::attributeModifiers,
            "buoyant", Codec.BOOLEAN.optional(false), SulfurCubeArchetype::buoyant,
            "explosion", Explosion.CODEC.optional(), SulfurCubeArchetype::explosion,
            "contact_damage", ContactDamage.CODEC.optional(), SulfurCubeArchetype::contactDamage,
            "knockback_modifiers", KnockbackModifiers.CODEC, SulfurCubeArchetype::knockbackModifiers,
            "sound_settings", SoundSettings.CODEC, SulfurCubeArchetype::soundSettings,
            SulfurCubeArchetype::create);

    /**
     * Creates a new instance of the "minecraft:sulfur_cube_archetype" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<SulfurCubeArchetype> createDefaultRegistry(Registries registries) {
        return DynamicRegistry.create(
                BuiltinRegistries.SULFUR_CUBE_ARCHETYPE, REGISTRY_CODEC, registries);
    }

    static SulfurCubeArchetype create(
            RegistryTag<Material> items,
            List<AttributeEntry> attributeModifiers,
            boolean buoyant,
            @Nullable Explosion explosion,
            @Nullable ContactDamage contactDamage,
            KnockbackModifiers knockbackModifiers,
            SoundSettings soundSettings
    ) {
        return new SulfurCubeArchetypeImpl(
                items,
                attributeModifiers,
                buoyant,
                explosion,
                contactDamage,
                knockbackModifiers,
                soundSettings
        );
    }

    static Builder builder() {
        return new Builder();
    }

    RegistryTag<Material> items();

    List<AttributeEntry> attributeModifiers();

    boolean buoyant();

    @Nullable Explosion explosion();

    @Nullable ContactDamage contactDamage();

    KnockbackModifiers knockbackModifiers();

    SoundSettings soundSettings();


    record AttributeEntry(Attribute attribute, AttributeModifier modifier) {
        public static final Codec<AttributeEntry> CODEC = StructCodec.struct(
                "attribute", Attribute.CODEC, AttributeEntry::attribute,
                StructCodec.INLINE, AttributeModifier.CODEC, AttributeEntry::modifier,
                AttributeEntry::new
        );

    }

    record Explosion(int power, boolean causesFire, int fuse) {
        public static final Codec<Explosion> CODEC = StructCodec.struct(
                "power", Codec.INT, Explosion::power,
                "causes_fire", Codec.BOOLEAN, Explosion::causesFire,
                "fuse", Codec.INT, Explosion::fuse,
                Explosion::new
        );

    }

    record ContactDamage(
            RegistryKey<DamageType> damageType,
            Codec.RawValue amount, // float provider
            boolean attributeToSource
    ) {
        public static final Codec<ContactDamage> CODEC = StructCodec.struct(
                "damage_type", DamageType.CODEC, ContactDamage::damageType,
                "amount", Codec.RAW_VALUE, ContactDamage::amount,
                "attribute_to_source", Codec.BOOLEAN, ContactDamage::attributeToSource,
                ContactDamage::new
        );

    }

    record KnockbackModifiers(float horizontalPower, float verticalPower) {
        public static final Codec<KnockbackModifiers> CODEC = StructCodec.struct(
                "horizontal_power", Codec.FLOAT, KnockbackModifiers::horizontalPower,
                "vertical_power", Codec.FLOAT, KnockbackModifiers::verticalPower,
                KnockbackModifiers::new
        );

        public static final KnockbackModifiers DEFAULT = new KnockbackModifiers(0.33f, 0.06f);

    }

    record SoundSettings(
            SoundEvent hitSound,
            SoundEvent pushSound,
            float pushSoundImpulseThreshold,
            float pushSoundCooldown
    ) {
        public static final Codec<SoundSettings> CODEC = StructCodec.struct(
                "hit_sound", SoundEvent.CODEC, SoundSettings::hitSound,
                "push_sound", SoundEvent.CODEC, SoundSettings::pushSound,
                "push_sound_impulse_threshold", Codec.FLOAT, SoundSettings::pushSoundImpulseThreshold,
                "push_sound_cooldown", Codec.FLOAT, SoundSettings::pushSoundCooldown,
                SoundSettings::new
        );

        public static final SoundSettings DEFAULT = new SoundSettings(
                SoundEvent.ENTITY_SULFUR_CUBE_REGULAR_HIT,
                SoundEvent.ENTITY_SULFUR_CUBE_REGULAR_PUSH,
                0.2f,
                0.5f
        );

    }

    final class Builder {
        private RegistryTag<Material> items = RegistryTag.empty();
        private List<AttributeEntry> attributeModifiers = List.of();
        private boolean buoyant = false;
        private @Nullable Explosion explosion = null;
        private @Nullable ContactDamage contactDamage = null;
        private KnockbackModifiers knockbackModifiers = KnockbackModifiers.DEFAULT;
        private SoundSettings soundSettings = SoundSettings.DEFAULT;

        private Builder() {}

        public Builder items(RegistryTag<Material> items) {
            this.items = Objects.requireNonNull(items, "items");
            return this;
        }

        public Builder attributeModifiers(List<AttributeEntry> attributeModifiers) {
            this.attributeModifiers = Objects.requireNonNull(attributeModifiers, "attributeModifiers");
            return this;
        }

        public Builder buoyant(boolean buoyant) {
            this.buoyant = buoyant;
            return this;
        }

        public Builder explosion(Explosion explosion) {
            this.explosion = explosion;
            return this;
        }

        public Builder contactDamage(ContactDamage contactDamage) {
            this.contactDamage = contactDamage;
            return this;
        }

        public Builder knockbackModifiers(KnockbackModifiers knockbackModifiers) {
            this.knockbackModifiers = Objects.requireNonNull(knockbackModifiers, "knockbackModifiers");
            return this;
        }

        public Builder soundSettings(SoundSettings soundSettings) {
            this.soundSettings = Objects.requireNonNull(soundSettings, "soundSettings");
            return this;
        }

        public SulfurCubeArchetype build() {
            return new SulfurCubeArchetypeImpl(
                    items,
                    attributeModifiers,
                    buoyant,
                    explosion,
                    contactDamage,
                    knockbackModifiers,
                    soundSettings
            );
        }
    }

}
