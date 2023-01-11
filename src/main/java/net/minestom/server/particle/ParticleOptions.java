package net.minestom.server.particle;

import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Point;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.particle.ParticleOptions.*;

public sealed interface ParticleOptions extends Writeable permits ParticleOptions.Block, BlockMarker, Dust, DustColorTransition, FallingDust, Item, SculkCharge, Shriek, Vibration {

    Particle type();

    /**
     * Options for {@link Particle#DUST}
     */
    record Dust(Color color, float scale) implements ParticleOptions {

        @Override
        public Particle type() {
            return Particle.DUST;
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeColor(color);
            writer.writeFloat(Math.min(4f, scale));
        }
    }

    /**
     * Options for {@link Particle#DUST_COLOR_TRANSITION}
     */
    record DustColorTransition(Color from, Color to, float scale) implements ParticleOptions {

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeColor(from);
            writer.writeFloat(Math.max(0.01f, Math.min(4f, scale)));
            writer.writeColor(to);
        }

        @Override
        public Particle type() {
            return Particle.DUST_COLOR_TRANSITION;
        }
    }

    /**
     * Options for {@link Particle#VIBRATION}
     */
    record Vibration(Target target, int ticks) implements ParticleOptions {

        private sealed interface Target extends Writeable permits Block, Entity {}

        public record Block(Point blockPosition) implements Target {

            @Override
            public void write(@NotNull BinaryWriter writer) {
                writer.writeSizedString("minecraft:block");
                writer.writeBlockPosition(blockPosition);
            }
        }

        public record Entity(int entityId, float eyeHeight) implements Target {

            public Entity(@NotNull net.minestom.server.entity.Entity e) {
                this(e.getEntityId(), (float) e.getEyeHeight());
            }

            @Override
            public void write(@NotNull BinaryWriter writer) {
                writer.writeSizedString("minecraft:entity");
                writer.writeVarInt(entityId);
                writer.writeFloat(eyeHeight);
            }
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            target.write(writer);
            writer.writeVarInt(ticks);
        }

        @Override
        public Particle type() {
            return Particle.VIBRATION;
        }
    }

    /**
     * Options for {@link Particle#BLOCK}
     */
    record Block(net.minestom.server.instance.block.Block block) implements ParticleOptions {

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeVarInt(block.id());
        }

        @Override
        public Particle type() {
            return Particle.BLOCK;
        }
    }

    /**
     * Options for {@link Particle#BLOCK_MARKER}
     */
    record BlockMarker(net.minestom.server.instance.block.Block block) implements ParticleOptions {

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeVarInt(block.id());
        }

        @Override
        public Particle type() {
            return Particle.BLOCK_MARKER;
        }
    }

    /**
     * Options for {@link Particle#FALLING_DUST}
     */
    record FallingDust(net.minestom.server.instance.block.Block block) implements ParticleOptions {

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeVarInt(block.id());
        }

        @Override
        public Particle type() {
            return Particle.FALLING_DUST;
        }
    }

    /**
     * Options for {@link Particle#ITEM}
     */
    record Item(ItemStack item) implements ParticleOptions {

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeItemStack(item);
        }

        @Override
        public Particle type() {
            return Particle.ITEM;
        }
    }

    /**
     * Options for {@link Particle#SCULK_CHARGE}
     */
    record SculkCharge(float angle) implements ParticleOptions {

        public SculkCharge {
            Check.argCondition(MathUtils.isBetween(angle, 0, 2 * Math.PI), "Angle is not within bounds");
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeFloat(angle);
        }

        @Override
        public Particle type() {
            return Particle.SCULK_CHARGE;
        }
    }

    /**
     * Options for {@link Particle#SHRIEK}
     */
    record Shriek(int ticks) implements ParticleOptions {

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeVarInt(ticks);
        }

        @Override
        public Particle type() {
            return Particle.SHRIEK;
        }
    }
}