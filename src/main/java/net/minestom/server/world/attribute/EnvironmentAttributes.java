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
import net.minestom.server.world.attribute.EnvironmentAttribute.Type;

import java.util.List;

import static net.minestom.server.world.attribute.EnvironmentAttributeImpl.register;

sealed interface EnvironmentAttributes permits EnvironmentAttribute {
    EnvironmentAttribute<RGBLike> FOG_COLOR = register("visual/fog_color", Type.RGB_COLOR, Color.BLACK);
    EnvironmentAttribute<Float> FOG_START_DISTANCE = register("visual/fog_start_distance", Type.FLOAT, 0f);
    EnvironmentAttribute<Float> FOG_END_DISTANCE = register("visual/fog_end_distance", Type.FLOAT, 1024f);
    EnvironmentAttribute<Float> SKY_FOG_END_DISTANCE = register("visual/sky_fog_end_distance", Type.FLOAT, 512f);
    EnvironmentAttribute<Float> CLOUD_FOG_END_DISTANCE = register("visual/cloud_fog_end_distance", Type.FLOAT, 2048f);
    EnvironmentAttribute<RGBLike> WATER_FOG_COLOR = register("visual/water_fog_color", Type.RGB_COLOR, new Color(0x050533));
    EnvironmentAttribute<Float> WATER_FOG_START_DISTANCE = register("visual/water_fog_start_distance", Type.FLOAT, -8f);
    EnvironmentAttribute<Float> WATER_FOG_END_DISTANCE = register("visual/water_fog_end_distance", Type.FLOAT, 96f);
    EnvironmentAttribute<RGBLike> SKY_COLOR = register("visual/sky_color", Type.RGB_COLOR, Color.BLACK);
    EnvironmentAttribute<ARGBLike> SUNRISE_SUNSET_COLOR = register("visual/sunrise_sunset_color", Type.ARGB_COLOR, AlphaColor.TRANSPARENT);
    EnvironmentAttribute<ARGBLike> CLOUD_COLOR = register("visual/cloud_color", Type.ARGB_COLOR, AlphaColor.TRANSPARENT);
    EnvironmentAttribute<Float> CLOUD_HEIGHT = register("visual/cloud_height", Type.FLOAT, 192.33f);
    EnvironmentAttribute<Float> SUN_ANGLE = register("visual/sun_angle", Type.FLOAT, 0f);
    EnvironmentAttribute<Float> MOON_ANGLE = register("visual/moon_angle", Type.FLOAT, 0f);
    EnvironmentAttribute<Float> STAR_ANGLE = register("visual/star_angle", Type.FLOAT, 0f);
    EnvironmentAttribute<MoonPhase> MOON_PHASE = register("visual/moon_phase", Type.MOON_PHASE, MoonPhase.FULL_MOON);
    EnvironmentAttribute<Float> STAR_BRIGHTNESS = register("visual/star_brightness", Type.FLOAT, 0f);
    EnvironmentAttribute<RGBLike> SKY_LIGHT_COLOR = register("visual/sky_light_color", Type.RGB_COLOR, Color.WHITE);
    EnvironmentAttribute<Float> SKY_LIGHT_FACTOR = register("visual/sky_light_factor", Type.FLOAT, 1f);
    EnvironmentAttribute<Particle> DEFAULT_DRIPSTONE_PARTICLE = register("visual/default_dripstone_particle", Type.PARTICLE, Particle.DRIPPING_DRIPSTONE_WATER);
    EnvironmentAttribute<List<AmbientParticle>> AMBIENT_PARTICLES = register("visual/ambient_particles", Type.AMBIENT_PARTICLES, List.of());
    EnvironmentAttribute<BackgroundMusic> BACKGROUND_MUSIC = register("audio/background_music", Type.BACKGROUND_MUSIC, BackgroundMusic.EMPTY);
    EnvironmentAttribute<Float> MUSIC_VOLUME = register("audio/music_volume", Type.FLOAT, 1f);
    EnvironmentAttribute<AmbientSounds> AMBIENT_SOUNDS = register("audio/ambient_sounds", Type.AMBIENT_SOUNDS, AmbientSounds.EMPTY);
    EnvironmentAttribute<Boolean> FIREFLY_BUSH_SOUNDS = register("audio/firefly_bush_sounds", Type.BOOLEAN, false);
    EnvironmentAttribute<Float> SKY_LIGHT_LEVEL = register("gameplay/sky_light_level", Type.FLOAT, 15f);
    EnvironmentAttribute<Boolean> CAN_START_RAID = register("gameplay/can_start_raid", Type.BOOLEAN, true);
    EnvironmentAttribute<Boolean> WATER_EVAPORATES = register("gameplay/water_evaporates", Type.BOOLEAN, false);
    EnvironmentAttribute<BedRule> BED_RULE = register("gameplay/bed_rule", Type.BED_RULE, BedRule.CAN_SLEEP_WHEN_DARK);
    EnvironmentAttribute<Boolean> RESPAWN_ANCHOR_WORKS = register("gameplay/respawn_anchor_works", Type.BOOLEAN, false);
    EnvironmentAttribute<Boolean> NETHER_PORTAL_SPAWNS_PIGLINS = register("gameplay/nether_portal_spawns_piglin", Type.BOOLEAN, false);
    EnvironmentAttribute<Boolean> FAST_LAVA = register("gameplay/fast_lava", Type.BOOLEAN, false);
    EnvironmentAttribute<Boolean> INCREASED_FIRE_BURNOUT = register("gameplay/increased_fire_burnout", Type.BOOLEAN, false);
    EnvironmentAttribute<TriState> EYEBLOSSOM_OPEN = register("gameplay/eyeblossom_open", Type.TRI_STATE, TriState.NOT_SET);
    EnvironmentAttribute<Float> TURTLE_EGG_HATCH_CHANCE = register("gameplay/turtle_egg_hatch_chance", Type.FLOAT, 0f);
    EnvironmentAttribute<Boolean> PIGLINS_ZOMBIFY = register("gameplay/piglins_zombify", Type.BOOLEAN, true);
    EnvironmentAttribute<Boolean> SNOW_GOLEM_MELTS = register("gameplay/snow_golem_melts", Type.BOOLEAN, false);
    EnvironmentAttribute<Boolean> CREAKING_ACTIVE = register("gameplay/creaking_active", Type.BOOLEAN, false);
    EnvironmentAttribute<Float> SURFACE_SLIME_SPAWN_CHANCE = register("gameplay/surface_slime_spawn_chance", Type.FLOAT, 0f);
    EnvironmentAttribute<Float> CAT_WAKING_UP_GIFT_CHANCE = register("gameplay/cat_waking_up_gift_chance", Type.FLOAT, 0f);
    EnvironmentAttribute<Boolean> BEES_STAY_IN_HIVE = register("gameplay/bees_stay_in_hive", Type.BOOLEAN, false);
    EnvironmentAttribute<Boolean> MONSTERS_BURN = register("gameplay/monsters_burn", Type.BOOLEAN, false);
    EnvironmentAttribute<Boolean> CAN_PILLAGER_PATROL_SPAWN = register("gameplay/can_pillager_patrol_spawn", Type.BOOLEAN, true);
    EnvironmentAttribute<EntityActivity> VILLAGER_ACTIVITY = register("gameplay/villager_activity", Type.ACTIVITY, EntityActivity.IDLE);
    EnvironmentAttribute<EntityActivity> BABY_VILLAGER_ACTIVITY = register("gameplay/baby_villager_activity", Type.ACTIVITY, EntityActivity.IDLE);

    Codec<EnvironmentAttribute<?>> CODEC = EnvironmentAttributeImpl.CODEC;
}
