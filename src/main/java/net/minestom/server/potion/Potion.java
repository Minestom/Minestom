package net.minestom.server.potion;

import net.minestom.server.entity.Entity;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.play.EntityEffectPacket;
import net.minestom.server.network.packet.server.play.RemoveEntityEffectPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

/**
 * Represents a potion effect that can be added to an {@link net.minestom.server.entity.Entity}.
 *
 * @param effect    the potion effect
 * @param amplifier the amplifier starting at 0 (level 1)
 * @param duration  the duration (in ticks) that the potion will last
 * @param flags     the flags of the potion, see {@link #flags()}
 */
public record Potion(@NotNull PotionEffect effect, int amplifier, int duration, byte flags) {
    /**
     * A flag indicating that this Potion is ambient (it came from a beacon).
     *
     * @see #PARTICLES_FLAG
     * @see #ICON_FLAG
     * @see #flags()
     */
    public static final byte AMBIENT_FLAG = 0x01;

    /**
     * A flag indicating that this Potion has particles.
     *
     * @see #AMBIENT_FLAG
     * @see #ICON_FLAG
     * @see #flags()
     */
    public static final byte PARTICLES_FLAG = 0x02;

    /**
     * A flag indicating that this Potion has an icon.
     *
     * @see #AMBIENT_FLAG
     * @see #PARTICLES_FLAG
     * @see #flags()
     */
    public static final byte ICON_FLAG = 0x04;

    /**
     * A flag instructing the client to use its builtin blending effect, only used with the darkness effect currently.
     */
    public static final byte BLEND_FLAG = 0x08;

    /**
     * A duration constant which sets a Potion duration to infinite.
     */
    public static final int INFINITE_DURATION = -1;

    /**
     * @see #Potion(PotionEffect, int, int, byte)
     */
    public Potion(@NotNull PotionEffect effect, int amplifier, int duration, int flags) {
        this(effect, amplifier, duration, (byte) flags);
    }

    /**
     * Creates a new Potion with no flags.
     *
     * @see #Potion(PotionEffect, int, int, byte)
     */
    public Potion(@NotNull PotionEffect effect, int amplifier, int duration) {
        this(effect, amplifier, duration, (byte) 0);
    }

    /**
     * Returns the flags that this Potion has.
     *
     * @see #AMBIENT_FLAG
     * @see #PARTICLES_FLAG
     * @see #ICON_FLAG
     */
    @Override
    public byte flags() {
        return flags;
    }

    /**
     * Returns whether this Potion is ambient (it came from a beacon) or not.
     *
     * @return <code>true</code> if the Potion is ambient
     */
    public boolean isAmbient() {
        return (flags & AMBIENT_FLAG) == AMBIENT_FLAG;
    }

    /**
     * Returns whether this Potion has particles or not.
     *
     * @return <code>true</code> if the Potion has particles
     */
    public boolean hasParticles() {
        return (flags & PARTICLES_FLAG) == PARTICLES_FLAG;
    }

    /**
     * Returns whether this Potion has an icon or not.
     *
     * @return <code>true</code> if the Potion has an icon
     */
    public boolean hasIcon() {
        return (flags & ICON_FLAG) == ICON_FLAG;
    }

    public boolean hasBlend() {
        return (flags & BLEND_FLAG) == BLEND_FLAG;
    }

    /**
     * Sends a packet that a potion effect has been applied to the entity.
     * <p>
     * Used internally by {@link net.minestom.server.entity.Player#addEffect(Potion)}
     *
     * @param entity the entity to add the effect to
     */
    public void sendAddPacket(@NotNull Entity entity) {
        entity.sendPacketToViewersAndSelf(new EntityEffectPacket(entity.getEntityId(), this));
    }

    /**
     * Sends a packet that a potion effect has been removed from the entity.
     * <p>
     * Used internally by {@link net.minestom.server.entity.Player#removeEffect(PotionEffect)}
     *
     * @param entity the entity to remove the effect from
     */
    public void sendRemovePacket(@NotNull Entity entity) {
        entity.sendPacketToViewersAndSelf(new RemoveEntityEffectPacket(entity.getEntityId(), effect));
    }

    public static final NetworkBuffer.Type<Potion> NETWORK_TYPE = NetworkBufferTemplate.template(
            PotionEffect.NETWORK_TYPE, Potion::effect,
            VAR_INT, Potion::amplifier,
            VAR_INT, Potion::duration,
            BYTE, Potion::flags,
            Potion::new
    );
}
