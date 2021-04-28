package net.minestom.server.particle.data;

import net.minestom.server.MinecraftServer;
import net.minestom.server.color.Color;
import net.minestom.server.instance.block.BlockState;
import net.minestom.server.item.ItemStack;
import net.minestom.server.particle.ParticleType;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.clone.PublicCloneable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

/**
 * Holds a particle type and its (optional) data.
 */
public class Particle implements PublicCloneable<Particle> {
    public static final BiFunction<ParticleType<Particle>, @Nullable String, Particle> READER =
            (particle, data) -> Particle.of(particle);

    private final ParticleType<?> particleType;
    private final boolean longDistance;
    private final float offsetX, offsetY, offsetZ;
    private final float speed;
    private final int count;

    protected Particle(@NotNull ParticleType<?> particleType, boolean longDistance,
                       float offsetX, float offsetY, float offsetZ, float speed, int count) {
        this.particleType = particleType;
        this.longDistance = longDistance;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.speed = speed;
        this.count = count;
    }

    protected Particle(@NotNull ParticleType<?> particleType) {
        this.particleType = particleType;
        this.longDistance = true;
        this.offsetX = 0;
        this.offsetY = 0;
        this.offsetZ = 0;
        this.speed = 0;
        this.count = 1;
    }

    public ParticleType<?> getType() {
        return particleType;
    }

    public boolean isLongDistance() {
        return longDistance;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public float getOffsetZ() {
        return offsetZ;
    }

    public float getSpeed() {
        return speed;
    }

    public int getCount() {
        return count;
    }

    public void write(BinaryWriter writer) {}

    public static @NotNull ParticleBuilder builder() {
        return new ParticleBuilder();
    }

    /**
     * Creates a {@link Particle} without data.
     *
     * @param particleType the type of the particle
     * @return the {@link Particle} formed from the particle type
     */
    public static @NotNull Particle of(@NotNull ParticleType<Particle> particleType) {
        return new Particle(particleType);
    }

    /**
     * Creates a {@link BlockParticle} with type block.
     *
     * @param state the blockstate for the particle
     * @return the block particle with its data
     */
    public static @NotNull BlockParticle block(BlockState state) {
        return new BlockParticle(ParticleType.BLOCK, state);
    }

    /**
     * Creates a {@link BlockParticle} with type falling dust.
     *
     * @param state the blockstate for the particle
     * @return the falling dust particle with its data
     */
    public static @NotNull BlockParticle fallingDust(BlockState state) {
        return new BlockParticle(ParticleType.FALLING_DUST, state);
    }

    /**
     * Creates a {@link DustParticle}.
     *
     * @param color the color of the dust
     * @param scale the scale of the dust (default is 1.0)
     * @return the dust particle with its data
     */
    public static @NotNull DustParticle dust(@NotNull Color color, float scale) {
        return new DustParticle(color, scale);
    }

    /**
     * Creates a {@link ItemParticle}
     *
     * @param item the item for the particle
     * @return the item particle with its data
     */
    public static @NotNull ItemParticle item(@NotNull ItemStack item) {
        return new ItemParticle(item);
    }

    @Override
    public @NotNull Particle clone() {
        try {
            return (Particle) super.clone();
        } catch (CloneNotSupportedException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            return null;
        }
    }
}
