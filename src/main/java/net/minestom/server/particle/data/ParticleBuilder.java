package net.minestom.server.particle.data;

import net.minestom.server.color.Color;
import net.minestom.server.instance.block.BlockState;
import net.minestom.server.item.ItemStack;
import net.minestom.server.particle.ParticleType;
import org.jetbrains.annotations.NotNull;

public class ParticleBuilder {
    private boolean longDistance = true;
    private float offsetX, offsetY, offsetZ = 0;
    private float speed = 0;
    private int count = 1;

    /**
     * Sets whether the client should force this particle to show, even when it is far away
     *
     * @param longDistance if the client should force show this particle
     * @return this
     */
    public @NotNull ParticleBuilder longDistance(boolean longDistance) {
        this.longDistance = longDistance;
        return this;
    }

    /**
     * Sets the random offset of this particle
     *
     * @param offsetX random x offset
     * @param offsetY random y offset
     * @param offsetZ random z offset
     * @return this
     */
    public @NotNull ParticleBuilder offset(float offsetX, float offsetY, float offsetZ) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        return this;
    }

    /**
     * Sets the speed of this particle
     *
     * @param speed speed of this particle
     * @return this
     */
    public @NotNull ParticleBuilder speed(float speed) {
        this.speed = speed;
        return this;
    }

    /**
     * Sets the amount of times this particle is shown.
     *
     * @param count the amount of times this particle is shown
     * @return this
     */
    public @NotNull ParticleBuilder count(int count) {
        this.count = count;
        return this;
    }

    /**
     * Creates a {@link Particle} without data.
     *
     * @param particleType the type of the particle
     * @return the {@link Particle} formed from the particle type
     */
    public @NotNull Particle build(@NotNull ParticleType<Particle> particleType) {
        return new Particle(particleType, longDistance, offsetX, offsetY, offsetZ, speed, count);
    }

    /**
     * Creates a {@link BlockParticle} with type block.
     *
     * @param state the blockstate for the particle
     * @return the block particle with its data
     */
    public @NotNull BlockParticle buildBlock(BlockState state) {
        return new BlockParticle(ParticleType.BLOCK, state, longDistance, offsetX, offsetY, offsetZ, speed, count);
    }

    /**
     * Creates a {@link BlockParticle} with type falling dust.
     *
     * @param state the blockstate for the particle
     * @return the falling dust particle with its data
     */
    public @NotNull BlockParticle buildFallingDust(BlockState state) {
        return new BlockParticle(ParticleType.FALLING_DUST, state, longDistance, offsetX, offsetY, offsetZ, speed, count);
    }

    /**
     * Creates a {@link DustParticle}.
     *
     * @param color the color of the dust
     * @param scale the scale of the dust (default is 1.0)
     * @return the dust particle with its data
     */
    public @NotNull DustParticle buildDust(@NotNull Color color, float scale) {
        return new DustParticle(color, scale, longDistance, offsetX, offsetY, offsetZ, speed, count);
    }

    /**
     * Creates a {@link ItemParticle}
     *
     * @param item the item for the particle
     * @return the item particle with its data
     */
    public @NotNull ItemParticle buildItem(@NotNull ItemStack item) {
        return new ItemParticle(item, longDistance, offsetX, offsetY, offsetZ, speed, count);
    }
}
