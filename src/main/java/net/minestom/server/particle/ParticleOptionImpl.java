package net.minestom.server.particle;


import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.particle.ParticleOption.*;

public interface ParticleOptionImpl {

    static ParticleOption AMBIENT_ENTITY_EFFECT() {
        return new AMBIENT_ENTITY_EFFECT();
    }

    static ParticleOption ANGRY_VILLAGER() {
        return new ANGRY_VILLAGER();
    }

    static ParticleOption BUBBLE() {
        return new BUBBLE();
    }

    static ParticleOption CLOUD() {
        return new CLOUD();
    }

    static ParticleOption CRIT() {
        return new CRIT();
    }

    static ParticleOption DAMAGE_INDICATOR() {
        return new DAMAGE_INDICATOR();
    }

    static ParticleOption DRAGON_BREATH() {
        return new DRAGON_BREATH();
    }

    static ParticleOption DRIPPING_LAVA() {
        return new DRIPPING_LAVA();
    }

    static ParticleOption FALLING_LAVA() {
        return new FALLING_LAVA();
    }

    static ParticleOption LANDING_LAVA() {
        return new LANDING_LAVA();
    }

    static ParticleOption DRIPPING_WATER() {
        return new DRIPPING_WATER();
    }

    static ParticleOption FALLING_WATER() {
        return new FALLING_WATER();
    }

    static ParticleOption EFFECT() {
        return new EFFECT();
    }

    static ParticleOption ELDER_GUARDIAN() {
        return new ELDER_GUARDIAN();
    }

    static ParticleOption ENCHANTED_HIT() {
        return new ENCHANTED_HIT();
    }

    static ParticleOption ENCHANT() {
        return new ENCHANT();
    }

    static ParticleOption END_ROD() {
        return new END_ROD();
    }

    static ParticleOption ENTITY_EFFECT() {
        return new ENTITY_EFFECT();
    }

    static ParticleOption EXPLOSION_EMITTER() {
        return new EXPLOSION_EMITTER();
    }

    static ParticleOption EXPLOSION() {
        return new EXPLOSION();
    }

    static ParticleOption SONIC_BOOM() {
        return new SONIC_BOOM();
    }

    static ParticleOption FIREWORK() {
        return new FIREWORK();
    }

    static ParticleOption FISHING() {
        return new FISHING();
    }

    static ParticleOption FLAME() {
        return new FLAME();
    }

    static ParticleOption SCULK_SOUL() {
        return new SCULK_SOUL();
    }

    static ParticleOption SCULK_CHARGE_POP() {
        return new SCULK_CHARGE_POP();
    }

    static ParticleOption SOUL_FIRE_FLAME() {
        return new SOUL_FIRE_FLAME();
    }

    static ParticleOption SOUL() {
        return new SOUL();
    }

    static ParticleOption FLASH() {
        return new FLASH();
    }

    static ParticleOption HAPPY_VILLAGER() {
        return new HAPPY_VILLAGER();
    }

    static ParticleOption COMPOSTER() {
        return new COMPOSTER();
    }

    static ParticleOption HEART() {
        return new HEART();
    }

    static ParticleOption INSTANT_EFFECT() {
        return new INSTANT_EFFECT();
    }

    static ParticleOption ITEM_SLIME() {
        return new ITEM_SLIME();
    }

    static ParticleOption ITEM_SNOWBALL() {
        return new ITEM_SNOWBALL();
    }

    static ParticleOption LARGE_SMOKE() {
        return new LARGE_SMOKE();
    }

    static ParticleOption LAVA() {
        return new LAVA();
    }

    static ParticleOption MYCELIUM() {
        return new MYCELIUM();
    }

    static ParticleOption NOTE() {
        return new NOTE();
    }

    static ParticleOption POOF() {
        return new POOF();
    }

    static ParticleOption PORTAL() {
        return new PORTAL();
    }

    static ParticleOption RAIN() {
        return new RAIN();
    }

    static ParticleOption SMOKE() {
        return new SMOKE();
    }

    static ParticleOption SNEEZE() {
        return new SNEEZE();
    }

    static ParticleOption SPIT() {
        return new SPIT();
    }

    static ParticleOption SQUID_INK() {
        return new SQUID_INK();
    }

    static ParticleOption SWEEP_ATTACK() {
        return new SWEEP_ATTACK();
    }

    static ParticleOption TOTEM_OF_UNDYING() {
        return new TOTEM_OF_UNDYING();
    }

    static ParticleOption UNDERWATER() {
        return new UNDERWATER();
    }

    static ParticleOption SPLASH() {
        return new SPLASH();
    }

    static ParticleOption WITCH() {
        return new WITCH();
    }

    static ParticleOption BUBBLE_POP() {
        return new BUBBLE_POP();
    }

    static ParticleOption CURRENT_DOWN() {
        return new CURRENT_DOWN();
    }

    static ParticleOption BUBBLE_COLUMN_UP() {
        return new BUBBLE_COLUMN_UP();
    }

    static ParticleOption NAUTILUS() {
        return new NAUTILUS();
    }

    static ParticleOption DOLPHIN() {
        return new DOLPHIN();
    }

    static ParticleOption CAMPFIRE_COSY_SMOKE() {
        return new CAMPFIRE_COSY_SMOKE();
    }

    static ParticleOption CAMPFIRE_SIGNAL_SMOKE() {
        return new CAMPFIRE_SIGNAL_SMOKE();
    }

    static ParticleOption DRIPPING_HONEY() {
        return new DRIPPING_HONEY();
    }

    static ParticleOption FALLING_HONEY() {
        return new FALLING_HONEY();
    }

    static ParticleOption LANDING_HONEY() {
        return new LANDING_HONEY();
    }

    static ParticleOption FALLING_NECTAR() {
        return new FALLING_NECTAR();
    }

    static ParticleOption FALLING_SPORE_BLOSSOM() {
        return new FALLING_SPORE_BLOSSOM();
    }

    static ParticleOption ASH() {
        return new ASH();
    }

    static ParticleOption CRIMSON_SPORE() {
        return new CRIMSON_SPORE();
    }

    static ParticleOption WARPED_SPORE() {
        return new WARPED_SPORE();
    }

    static ParticleOption SPORE_BLOSSOM_AIR() {
        return new SPORE_BLOSSOM_AIR();
    }

    static ParticleOption DRIPPING_OBSIDIAN_TEAR() {
        return new DRIPPING_OBSIDIAN_TEAR();
    }

    static ParticleOption FALLING_OBSIDIAN_TEAR() {
        return new FALLING_OBSIDIAN_TEAR();
    }

    static ParticleOption LANDING_OBSIDIAN_TEAR() {
        return new LANDING_OBSIDIAN_TEAR();
    }

    static ParticleOption REVERSE_PORTAL() {
        return new REVERSE_PORTAL();
    }

    static ParticleOption WHITE_ASH() {
        return new WHITE_ASH();
    }

    static ParticleOption SMALL_FLAME() {
        return new SMALL_FLAME();
    }

    static ParticleOption SNOWFLAKE() {
        return new SNOWFLAKE();
    }

    static ParticleOption DRIPPING_DRIPSTONE_LAVA() {
        return new DRIPPING_DRIPSTONE_LAVA();
    }

    static ParticleOption FALLING_DRIPSTONE_LAVA() {
        return new FALLING_DRIPSTONE_LAVA();
    }

    static ParticleOption DRIPPING_DRIPSTONE_WATER() {
        return new DRIPPING_DRIPSTONE_WATER();
    }

    static ParticleOption FALLING_DRIPSTONE_WATER() {
        return new FALLING_DRIPSTONE_WATER();
    }

    static ParticleOption GLOW_SQUID_INK() {
        return new GLOW_SQUID_INK();
    }

    static ParticleOption GLOW() {
        return new GLOW();
    }

    static ParticleOption WAX_ON() {
        return new WAX_ON();
    }

    static ParticleOption WAX_OFF() {
        return new WAX_OFF();
    }

    static ParticleOption ELECTRIC_SPARK() {
        return new ELECTRIC_SPARK();
    }

    static ParticleOption SCRAPE() {
        return new SCRAPE();
    }

    static ParticleOption BLOCK(@NotNull Block block) {
        return new BLOCK(block);
    }

    static ParticleOption BLOCK_MARKER(@NotNull Block block) {
        return new BLOCK_MARKER(block);
    }

    static ParticleOption DUST(@NotNull Color color, float scale) {
        return new DUST(color, scale);
    }

    static ParticleOption DUST_COLOR_TRANSITION(@NotNull Color from, @NotNull Color to, float scale) {
        return new DUST_COLOR_TRANSITION(from, to, scale);
    }

    static ParticleOption FALLING_DUST(@NotNull Block block) {
        return new FALLING_DUST(block);
    }

    static ParticleOption SCULK_CHARGE(float angle) {
        return new SCULK_CHARGE(angle);
    }

    static ParticleOption ITEM(@NotNull ItemStack item) {
        return new ITEM(item);
    }

    static ParticleOption VIBRATION_ENTITY(@NotNull Entity entity, int ticks) {
        final VIBRATION.Entity target = new VIBRATION.Entity(entity);
        return new VIBRATION(target, ticks);
    }

    static ParticleOption VIBRATION_ENTITY(@NotNull Point blockPosition, int ticks) {
        final VIBRATION.Block target = new VIBRATION.Block(blockPosition);
        return new VIBRATION(target, ticks);
    }

    static ParticleOption SHRIEK(int ticks) {
        return new SHRIEK(ticks);
    }
}