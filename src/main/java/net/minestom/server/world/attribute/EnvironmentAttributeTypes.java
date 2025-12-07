package net.minestom.server.world.attribute;

import net.kyori.adventure.util.ARGBLike;
import net.kyori.adventure.util.RGBLike;
import net.kyori.adventure.util.TriState;
import net.minestom.server.codec.Codec;
import net.minestom.server.color.AlphaColor;
import net.minestom.server.color.Color;
import net.minestom.server.entity.EntityActivity;
import net.minestom.server.particle.Particle;
import net.minestom.server.world.MoonPhase;

import java.util.List;
import java.util.Map;

import static net.minestom.server.world.attribute.EnvironmentAttributeTypeImpl.register;

sealed interface EnvironmentAttributeTypes permits EnvironmentAttribute.Type {
    EnvironmentAttribute.Type<Boolean> BOOLEAN = register("boolean", Codec.BOOLEAN, EnvironmentAttribute.Modifier.BOOLEAN_OPERATORS);
    EnvironmentAttribute.Type<TriState> TRI_STATE = register("tri_state", Codec.TRI_STATE, Map.of());
    EnvironmentAttribute.Type<Float> FLOAT = register("float", Codec.FLOAT, EnvironmentAttribute.Modifier.FLOAT_OPERATORS);
    EnvironmentAttribute.Type<Float> ANGLE_DEGREES = register("angle_degrees", Codec.FLOAT, EnvironmentAttribute.Modifier.FLOAT_OPERATORS);
    EnvironmentAttribute.Type<RGBLike> RGB_COLOR = register("rgb_color", Color.STRING_CODEC, Map.of()); // TODO
    EnvironmentAttribute.Type<ARGBLike> ARGB_COLOR = register("argb_color", AlphaColor.STRING_CODEC, Map.of()); // TODO
    EnvironmentAttribute.Type<MoonPhase> MOON_PHASE = register("moon_phase", MoonPhase.CODEC, Map.of());
    EnvironmentAttribute.Type<EntityActivity> ACTIVITY = register("activity", EntityActivity.CODEC, Map.of());
    EnvironmentAttribute.Type<BedRule> BED_RULE = register("bed_rule", BedRule.CODEC, Map.of());
    EnvironmentAttribute.Type<Particle> PARTICLE = register("particle", Particle.CODEC, Map.of());
    EnvironmentAttribute.Type<List<AmbientParticle>> AMBIENT_PARTICLES = register("ambient_particles", AmbientParticle.CODEC.list(), Map.of());
    EnvironmentAttribute.Type<BackgroundMusic> BACKGROUND_MUSIC = register("background_music", BackgroundMusic.CODEC, Map.of());
    EnvironmentAttribute.Type<AmbientSounds> AMBIENT_SOUNDS = register("ambient_sounds", AmbientSounds.CODEC, Map.of());
}
