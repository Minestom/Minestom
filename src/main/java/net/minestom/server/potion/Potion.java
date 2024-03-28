package net.minestom.server.potion;

import net.minestom.server.entity.Entity;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.play.EntityEffectPacket;
import net.minestom.server.network.packet.server.play.RemoveEntityEffectPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Objects;

import static net.minestom.server.network.NetworkBuffer.*;
import static net.minestom.server.network.NetworkBuffer.NBT;

/**
 * Represents a potion effect that can be added to an {@link net.minestom.server.entity.Entity}.
 *
 * @param effect     the potion effect
 * @param amplifier  the amplifier starting at 0 (level 1)
 * @param duration   the duration (in ticks) that the potion will last
 * @param flags      the flags of the potion, see {@link #flags()}
 * @param factorData used to change how the darkness effect looks, see {@link Potion#darkness(Entity, byte, int, byte)}
 */
public record Potion(@NotNull PotionEffect effect, byte amplifier,
                     int duration, byte flags, @Nullable FactorData factorData) implements NetworkBuffer.Writer {
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
     * A duration constant which sets a Potion duration to infinite.
     */
    public static final int INFINITE_DURATION = -1;

    /**
     * Creates a new Potion with no factor data.
     *
     * @see #Potion(PotionEffect, byte, int, byte, FactorData)
     */
    public Potion(@NotNull PotionEffect effect, byte amplifier, int duration, byte flags) {
        this(effect, amplifier, duration, flags, null);
    }

    /**
     * Creates a new Potion with no flags and factor data.
     *
     * @see #Potion(PotionEffect, byte, int, byte, FactorData)
     */
    public Potion(@NotNull PotionEffect effect, byte amplifier, int duration) {
        this(effect, amplifier, duration, (byte) 0, null);
    }

    public Potion(@NotNull NetworkBuffer reader) {
        this(Objects.requireNonNull(PotionEffect.fromId(reader.read(VAR_INT))), reader.read(BYTE),
                reader.read(VAR_INT), reader.read(BYTE), reader.readOptional(r -> FactorData.fromNBT((NBTCompound) r.read(NBT))));
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

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, effect.id());
        writer.write(BYTE, amplifier);
        writer.write(VAR_INT, duration);
        writer.write(BYTE, flags);
        writer.writeOptional(factorData);
    }

    public static @NotNull Potion darkness(@NotNull Entity entity, byte amplifier, int duration, byte flags) {
        return new Potion(PotionEffect.DARKNESS, amplifier, duration, flags, FactorData.defaultFactorData(entity));
    }
}
