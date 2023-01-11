package net.minestom.server.particle;

import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.particle.ParticleOptions.*;

public sealed abstract class ParticleOptions permits BlockStateOptions, Dust, DustColorTransition, Item, SculkCharge, Shriek, Vibration {
    protected final BinaryWriter writer = new BinaryWriter();
    private final Particle type;

    public ParticleOptions(Particle type) {
        this.type = type;
    }

    public byte[] getData() {
        return writer.toByteArray();
    }

    public Particle type() {
        return type;
    }
    
    /**
     * Options for {@link Particle#DUST}
     */
    public static final class Dust extends ParticleOptions {

        public Dust(Color color, float scale) {
            super(Particle.DUST);
            writer.writeColor(color);
            writer.writeFloat(Math.max(0.01f, Math.min(4f, scale)));
        }
    }

    /**
     * Options for {@link Particle#DUST_COLOR_TRANSITION}
     */
    public static final class DustColorTransition extends ParticleOptions {

        public DustColorTransition(Color from, Color to, float scale) {
            super(Particle.DUST_COLOR_TRANSITION);
            writer.writeColor(from);
            writer.writeFloat(Math.max(0.01f, Math.min(4f, scale)));
            writer.writeColor(to);
        }
    }

    /**
     * Options for {@link Particle#VIBRATION}
     */
    public static final class Vibration extends ParticleOptions {

        public Vibration(@NotNull Entity entity, int ticks) {
            super(Particle.VIBRATION);
            writer.writeSizedString("minecraft:entity");
            writer.writeVarInt(entity.getEntityId());
            writer.writeFloat((float) entity.getEyeHeight());
            writer.writeVarInt(ticks);
        }

        public Vibration(@NotNull Point blockPosition, int ticks) {
            super(Particle.VIBRATION);
            writer.writeSizedString("minecraft:block");
            writer.writeBlockPosition(blockPosition);
            writer.writeVarInt(ticks);
        }
    }

    private sealed static class BlockStateOptions extends ParticleOptions permits Block, BlockMarker, FallingDust {

        public BlockStateOptions(Particle particle, @NotNull net.minestom.server.instance.block.Block block) {
            super(particle);
            writer.writeVarInt(block.id());
        }
    }

    public static final class Block extends BlockStateOptions {

        public Block(@NotNull net.minestom.server.instance.block.Block block) {
            super(Particle.BLOCK, block);
        }
    }

    public static final class BlockMarker extends BlockStateOptions {

        public BlockMarker(@NotNull net.minestom.server.instance.block.Block block) {
            super(Particle.BLOCK_MARKER, block);
        }
    }

    public static final class FallingDust extends BlockStateOptions {

        public FallingDust(@NotNull net.minestom.server.instance.block.Block block) {
            super(Particle.FALLING_DUST, block);
        }
    }

    public static final class Item extends ParticleOptions {

        public Item(@NotNull ItemStack item) {
            super(Particle.ITEM);
            writer.writeItemStack(item);
        }

    }

    public static final class SculkCharge extends ParticleOptions {

        public SculkCharge(float angle) {
            super(Particle.SCULK_CHARGE);
            Check.argCondition(MathUtils.isBetween(angle, 0, 2*Math.PI), "Angle is not within bounds");
            writer.writeFloat(angle);
        }
    }

    public static final class Shriek extends ParticleOptions {

        public Shriek(int ticks) {
            super(Particle.SHRIEK);
            writer.writeVarInt(ticks);
        }
    }
}