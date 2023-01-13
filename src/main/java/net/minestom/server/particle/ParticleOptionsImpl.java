package net.minestom.server.particle;


import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.particle.ParticleOptions.*;

public interface ParticleOptionsImpl {
    ParticleOptions AMBIENT_ENTITY_EFFECT = new AMBIENT_ENTITY_EFFECT();
    ParticleOptions ANGRY_VILLAGER = new ANGRY_VILLAGER();
    ParticleOptions BUBBLE = new BUBBLE();
    ParticleOptions CLOUD = new CLOUD();
    ParticleOptions CRIT = new CRIT();
    ParticleOptions DAMAGE_INDICATOR = new DAMAGE_INDICATOR();
    ParticleOptions DRAGON_BREATH = new DRAGON_BREATH();
    ParticleOptions DRIPPING_LAVA = new DRIPPING_LAVA();
    ParticleOptions FALLING_LAVA = new FALLING_LAVA();
    ParticleOptions LANDING_LAVA = new LANDING_LAVA();
    ParticleOptions DRIPPING_WATER = new DRIPPING_WATER();
    ParticleOptions FALLING_WATER = new FALLING_WATER();
    ParticleOptions EFFECT = new EFFECT();
    ParticleOptions ELDER_GUARDIAN = new ELDER_GUARDIAN();
    ParticleOptions ENCHANTED_HIT = new ENCHANTED_HIT();
    ParticleOptions ENCHANT = new ENCHANT();
    ParticleOptions END_ROD = new END_ROD();
    ParticleOptions ENTITY_EFFECT = new ENTITY_EFFECT();
    ParticleOptions EXPLOSION_EMITTER = new EXPLOSION_EMITTER();
    ParticleOptions EXPLOSION = new EXPLOSION();
    ParticleOptions SONIC_BOOM = new SONIC_BOOM();
    ParticleOptions FIREWORK = new FIREWORK();
    ParticleOptions FISHING = new FISHING();
    ParticleOptions FLAME = new FLAME();
    ParticleOptions SCULK_SOUL = new SCULK_SOUL();
    ParticleOptions SCULK_CHARGE_POP = new SCULK_CHARGE_POP();
    ParticleOptions SOUL_FIRE_FLAME = new SOUL_FIRE_FLAME();
    ParticleOptions SOUL = new SOUL();
    ParticleOptions FLASH = new FLASH();
    ParticleOptions HAPPY_VILLAGER = new HAPPY_VILLAGER();
    ParticleOptions COMPOSTER = new COMPOSTER();
    ParticleOptions HEART = new HEART();
    ParticleOptions INSTANT_EFFECT = new INSTANT_EFFECT();
    ParticleOptions ITEM_SLIME = new ITEM_SLIME();
    ParticleOptions ITEM_SNOWBALL = new ITEM_SNOWBALL();
    ParticleOptions LARGE_SMOKE = new LARGE_SMOKE();
    ParticleOptions LAVA = new LAVA();
    ParticleOptions MYCELIUM = new MYCELIUM();
    ParticleOptions NOTE = new NOTE();
    ParticleOptions POOF = new POOF();
    ParticleOptions PORTAL = new PORTAL();
    ParticleOptions RAIN = new RAIN();
    ParticleOptions SMOKE = new SMOKE();
    ParticleOptions SNEEZE = new SNEEZE();
    ParticleOptions SPIT = new SPIT();
    ParticleOptions SQUID_INK = new SQUID_INK();
    ParticleOptions SWEEP_ATTACK = new SWEEP_ATTACK();
    ParticleOptions TOTEM_OF_UNDYING = new TOTEM_OF_UNDYING();
    ParticleOptions UNDERWATER = new UNDERWATER();
    ParticleOptions SPLASH = new SPLASH();
    ParticleOptions WITCH = new WITCH();
    ParticleOptions BUBBLE_POP = new BUBBLE_POP();
    ParticleOptions CURRENT_DOWN = new CURRENT_DOWN();
    ParticleOptions BUBBLE_COLUMN_UP = new BUBBLE_COLUMN_UP();
    ParticleOptions NAUTILUS = new NAUTILUS();
    ParticleOptions DOLPHIN = new DOLPHIN();
    ParticleOptions CAMPFIRE_COSY_SMOKE = new CAMPFIRE_COSY_SMOKE();
    ParticleOptions CAMPFIRE_SIGNAL_SMOKE = new CAMPFIRE_SIGNAL_SMOKE();
    ParticleOptions DRIPPING_HONEY = new DRIPPING_HONEY();
    ParticleOptions FALLING_HONEY = new FALLING_HONEY();
    ParticleOptions LANDING_HONEY = new LANDING_HONEY();
    ParticleOptions FALLING_NECTAR = new FALLING_NECTAR();
    ParticleOptions FALLING_SPORE_BLOSSOM = new FALLING_SPORE_BLOSSOM();
    ParticleOptions ASH = new ASH();
    ParticleOptions CRIMSON_SPORE = new CRIMSON_SPORE();
    ParticleOptions WARPED_SPORE = new WARPED_SPORE();
    ParticleOptions SPORE_BLOSSOM_AIR = new SPORE_BLOSSOM_AIR();
    ParticleOptions DRIPPING_OBSIDIAN_TEAR = new DRIPPING_OBSIDIAN_TEAR();
    ParticleOptions FALLING_OBSIDIAN_TEAR = new FALLING_OBSIDIAN_TEAR();
    ParticleOptions LANDING_OBSIDIAN_TEAR = new LANDING_OBSIDIAN_TEAR();
    ParticleOptions REVERSE_PORTAL = new REVERSE_PORTAL();
    ParticleOptions WHITE_ASH = new WHITE_ASH();
    ParticleOptions SMALL_FLAME = new SMALL_FLAME();
    ParticleOptions SNOWFLAKE = new SNOWFLAKE();
    ParticleOptions DRIPPING_DRIPSTONE_LAVA = new DRIPPING_DRIPSTONE_LAVA();
    ParticleOptions FALLING_DRIPSTONE_LAVA = new FALLING_DRIPSTONE_LAVA();
    ParticleOptions DRIPPING_DRIPSTONE_WATER = new DRIPPING_DRIPSTONE_WATER();
    ParticleOptions FALLING_DRIPSTONE_WATER = new FALLING_DRIPSTONE_WATER();
    ParticleOptions GLOW_SQUID_INK = new GLOW_SQUID_INK();
    ParticleOptions GLOW = new GLOW();
    ParticleOptions WAX_ON = new WAX_ON();
    ParticleOptions WAX_OFF = new WAX_OFF();
    ParticleOptions ELECTRIC_SPARK = new ELECTRIC_SPARK();
    ParticleOptions SCRAPE = new SCRAPE();

    static ParticleOptions BLOCK(@NotNull Block block) {
        return new BLOCK(block);
    }

    static ParticleOptions BLOCK_MARKER(@NotNull Block block) {
        return new BLOCK_MARKER(block);
    }

    static ParticleOptions DUST(@NotNull Color color, float scale) {
        return new DUST(color, scale);
    }

    static ParticleOptions DUST_COLOR_TRANSITION(@NotNull Color from, @NotNull Color to, float scale) {
        return new DUST_COLOR_TRANSITION(from, to, scale);
    }

    static ParticleOptions FALLING_DUST(@NotNull Block block) {
        return new FALLING_DUST(block);
    }

    static ParticleOptions SCULK_CHARGE(float angle) {
        return new SCULK_CHARGE(angle);
    }

    static ParticleOptions ITEM(@NotNull ItemStack item) {
        return new ITEM(item);
    }

    static ParticleOptions VIBRATION_ENTITY(@NotNull Entity entity, int ticks) {
        final VIBRATION.Entity target = new VIBRATION.Entity(entity);
        return new VIBRATION(target, ticks);
    }

    static ParticleOptions VIBRATION_ENTITY(@NotNull Point blockPosition, int ticks) {
        final VIBRATION.Block target = new VIBRATION.Block(blockPosition);
        return new VIBRATION(target, ticks);
    }
    static ParticleOptions SHRIEK(int ticks) {
        return new SHRIEK(ticks);
    }
}