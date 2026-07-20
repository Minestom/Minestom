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

import static net.minestom.server.world.attribute.EnvironmentAttributeImpl.register;

sealed interface EnvironmentAttributes permits EnvironmentAttribute {
    EnvironmentAttribute<RGBLike> FOG_COLOR = register("visual/fog_color", EnvironmentAttribute.Type.RGB_COLOR, Color.BLACK);
    EnvironmentAttribute<Float> FOG_START_DISTANCE = register("visual/fog_start_distance", EnvironmentAttribute.Type.FLOAT, 0f);
    EnvironmentAttribute<Float> FOG_END_DISTANCE = register("visual/fog_end_distance", EnvironmentAttribute.Type.FLOAT, 1024f);
    EnvironmentAttribute<Float> SKY_FOG_END_DISTANCE = register("visual/sky_fog_end_distance", EnvironmentAttribute.Type.FLOAT, 512f);
    EnvironmentAttribute<Float> CLOUD_FOG_END_DISTANCE = register("visual/cloud_fog_end_distance", EnvironmentAttribute.Type.FLOAT, 2048f);
    EnvironmentAttribute<RGBLike> WATER_FOG_COLOR = register("visual/water_fog_color", EnvironmentAttribute.Type.RGB_COLOR, new Color(0x050533));
    EnvironmentAttribute<Float> WATER_FOG_START_DISTANCE = register("visual/water_fog_start_distance", EnvironmentAttribute.Type.FLOAT, -8f);
    EnvironmentAttribute<Float> WATER_FOG_END_DISTANCE = register("visual/water_fog_end_distance", EnvironmentAttribute.Type.FLOAT, 96f);
    EnvironmentAttribute<RGBLike> SKY_COLOR = register("visual/sky_color", EnvironmentAttribute.Type.RGB_COLOR, Color.BLACK);
    EnvironmentAttribute<ARGBLike> SUNRISE_SUNSET_COLOR = register("visual/sunrise_sunset_color", EnvironmentAttribute.Type.ARGB_COLOR, AlphaColor.TRANSPARENT);
    EnvironmentAttribute<ARGBLike> CLOUD_COLOR = register("visual/cloud_color", EnvironmentAttribute.Type.ARGB_COLOR, AlphaColor.TRANSPARENT);
    EnvironmentAttribute<Float> CLOUD_HEIGHT = register("visual/cloud_height", EnvironmentAttribute.Type.FLOAT, 192.33f);
    EnvironmentAttribute<Float> SUN_ANGLE = register("visual/sun_angle", EnvironmentAttribute.Type.ANGLE_DEGREES, 0f);
    EnvironmentAttribute<Float> MOON_ANGLE = register("visual/moon_angle", EnvironmentAttribute.Type.ANGLE_DEGREES, 0f);
    EnvironmentAttribute<Float> STAR_ANGLE = register("visual/star_angle", EnvironmentAttribute.Type.ANGLE_DEGREES, 0f);
    EnvironmentAttribute<MoonPhase> MOON_PHASE = register("visual/moon_phase", EnvironmentAttribute.Type.MOON_PHASE, MoonPhase.FULL_MOON);
    EnvironmentAttribute<Float> STAR_BRIGHTNESS = register("visual/star_brightness", EnvironmentAttribute.Type.FLOAT, 0f);
    EnvironmentAttribute<RGBLike> BLOCK_LIGHT_TINT = register("visual/block_light_tint", EnvironmentAttribute.Type.RGB_COLOR, new Color(0xFFD88C));
    EnvironmentAttribute<RGBLike> SKY_LIGHT_COLOR = register("visual/sky_light_color", EnvironmentAttribute.Type.RGB_COLOR, Color.WHITE);
    EnvironmentAttribute<Float> SKY_LIGHT_FACTOR = register("visual/sky_light_factor", EnvironmentAttribute.Type.FLOAT, 1f);
    EnvironmentAttribute<RGBLike> NIGHT_VISION_COLOR = register("visual/night_vision_color", EnvironmentAttribute.Type.RGB_COLOR, new Color(0x999999));
    EnvironmentAttribute<RGBLike> AMBIENT_LIGHT_COLOR = register("visual/ambient_light_color", EnvironmentAttribute.Type.RGB_COLOR, new Color(0x000000));
    EnvironmentAttribute<Particle> DEFAULT_DRIPSTONE_PARTICLE = register("visual/default_dripstone_particle", EnvironmentAttribute.Type.PARTICLE, Particle.DRIPPING_DRIPSTONE_WATER);
    EnvironmentAttribute<List<AmbientParticle>> AMBIENT_PARTICLES = register("visual/ambient_particles", EnvironmentAttribute.Type.AMBIENT_PARTICLES, List.of());
    EnvironmentAttribute<BackgroundMusic> BACKGROUND_MUSIC = register("audio/background_music", EnvironmentAttribute.Type.BACKGROUND_MUSIC, BackgroundMusic.EMPTY);
    EnvironmentAttribute<Float> MUSIC_VOLUME = register("audio/music_volume", EnvironmentAttribute.Type.FLOAT, 1f);
    EnvironmentAttribute<AmbientSounds> AMBIENT_SOUNDS = register("audio/ambient_sounds", EnvironmentAttribute.Type.AMBIENT_SOUNDS, AmbientSounds.EMPTY);
    EnvironmentAttribute<Boolean> FIREFLY_BUSH_SOUNDS = register("audio/firefly_bush_sounds", EnvironmentAttribute.Type.BOOLEAN, false);
    EnvironmentAttribute<Float> SKY_LIGHT_LEVEL = register("gameplay/sky_light_level", EnvironmentAttribute.Type.FLOAT, 15f);
    EnvironmentAttribute<Boolean> CAN_START_RAID = register("gameplay/can_start_raid", EnvironmentAttribute.Type.BOOLEAN, true);
    EnvironmentAttribute<Boolean> WATER_EVAPORATES = register("gameplay/water_evaporates", EnvironmentAttribute.Type.BOOLEAN, false);
    EnvironmentAttribute<BedRule> BED_RULE = register("gameplay/bed_rule", EnvironmentAttribute.Type.BED_RULE, BedRule.CAN_SLEEP_WHEN_DARK);
    EnvironmentAttribute<Boolean> RESPAWN_ANCHOR_WORKS = register("gameplay/respawn_anchor_works", EnvironmentAttribute.Type.BOOLEAN, false);
    EnvironmentAttribute<Boolean> NETHER_PORTAL_SPAWNS_PIGLINS = register("gameplay/nether_portal_spawns_piglin", EnvironmentAttribute.Type.BOOLEAN, false);
    EnvironmentAttribute<Boolean> FAST_LAVA = register("gameplay/fast_lava", EnvironmentAttribute.Type.BOOLEAN, false);
    EnvironmentAttribute<Boolean> INCREASED_FIRE_BURNOUT = register("gameplay/increased_fire_burnout", EnvironmentAttribute.Type.BOOLEAN, false);
    EnvironmentAttribute<TriState> EYEBLOSSOM_OPEN = register("gameplay/eyeblossom_open", EnvironmentAttribute.Type.TRI_STATE, TriState.NOT_SET);
    EnvironmentAttribute<Float> TURTLE_EGG_HATCH_CHANCE = register("gameplay/turtle_egg_hatch_chance", EnvironmentAttribute.Type.FLOAT, 0.02f);
    EnvironmentAttribute<Boolean> PIGLINS_ZOMBIFY = register("gameplay/piglins_zombify", EnvironmentAttribute.Type.BOOLEAN, true);
    EnvironmentAttribute<Boolean> SNOW_GOLEM_MELTS = register("gameplay/snow_golem_melts", EnvironmentAttribute.Type.BOOLEAN, false);
    EnvironmentAttribute<Boolean> CREAKING_ACTIVE = register("gameplay/creaking_active", EnvironmentAttribute.Type.BOOLEAN, false);
    EnvironmentAttribute<Float> SURFACE_SLIME_SPAWN_CHANCE = register("gameplay/surface_slime_spawn_chance", EnvironmentAttribute.Type.FLOAT, 0f);
    EnvironmentAttribute<Float> CAT_WAKING_UP_GIFT_CHANCE = register("gameplay/cat_waking_up_gift_chance", EnvironmentAttribute.Type.FLOAT, 0f);
    EnvironmentAttribute<Boolean> BEES_STAY_IN_HIVE = register("gameplay/bees_stay_in_hive", EnvironmentAttribute.Type.BOOLEAN, false);
    EnvironmentAttribute<Boolean> MONSTERS_BURN = register("gameplay/monsters_burn", EnvironmentAttribute.Type.BOOLEAN, false);
    EnvironmentAttribute<Boolean> CAN_PILLAGER_PATROL_SPAWN = register("gameplay/can_pillager_patrol_spawn", EnvironmentAttribute.Type.BOOLEAN, true);
    EnvironmentAttribute<EntityActivity> VILLAGER_ACTIVITY = register("gameplay/villager_activity", EnvironmentAttribute.Type.ACTIVITY, EntityActivity.IDLE);
    EnvironmentAttribute<EntityActivity> BABY_VILLAGER_ACTIVITY = register("gameplay/baby_villager_activity", EnvironmentAttribute.Type.ACTIVITY, EntityActivity.IDLE);

    Codec<EnvironmentAttribute<?>> CODEC = EnvironmentAttributeImpl.CODEC;
}
