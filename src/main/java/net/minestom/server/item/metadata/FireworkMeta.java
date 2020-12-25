package net.minestom.server.item.metadata;

import net.minestom.server.item.firework.FireworkEffect;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTTypes;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a firework rocket meta data and its effects.
 */
public class FireworkMeta extends ItemMeta {

    private List<FireworkEffect> effects = new CopyOnWriteArrayList<>();
    private byte flightDuration;

    /**
     * Adds a firework effect to this firework.
     *
     * @param effect The firework effect to be added.
     */
    public void addFireworkEffect(FireworkEffect effect) {
        this.effects.add(effect);
    }

    /**
     * Adds an array of firework effects to this firework.
     *
     * @param effects An array of firework effects to be added.
     */
    public void addFireworkEffects(FireworkEffect... effects) {
        this.effects.addAll(Arrays.asList(effects));
    }

    /**
     * Removes a firework effect from this firework.
     *
     * @param index The index of the firework effect to be removed.
     * @throws IndexOutOfBoundsException If index {@literal < 0 or index >} {@link #getEffectSize()}
     */
    public void removeFireworkEffect(int index) throws IndexOutOfBoundsException {
        this.effects.remove(index);
    }

    /**
     * Removes a firework effects from this firework.
     *
     * @param effect The effect to be removed.
     */
    public void removeFireworkEffect(FireworkEffect effect) {
        this.effects.remove(effect);
    }

    /**
     * Retrieves a collection with all effects in this firework.
     *
     * @return A collection with all effects in this firework.
     */
    public List<FireworkEffect> getEffects() {
        return effects;
    }

    /**
     * Retrieves the size of effects in this firework.
     *
     * @return The size of the effects.
     */
    public int getEffectSize() {
        return this.effects.size();
    }

    /**
     * Removes all effects from this firework.
     */
    public void clearEffects() {
        this.effects.clear();
    }

    /**
     * Whether this firework has any effects.
     *
     * @return {@code true} if this firework has any effects, otherwise {@code false}.
     */
    public boolean hasEffects() {
        return this.effects.isEmpty();
    }

    /**
     * Changes the flight duration of this firework.
     *
     * @param flightDuration The new flight duration for this firework.
     */
    public void setFlightDuration(byte flightDuration) {
        this.flightDuration = flightDuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNbt() {
        return this.flightDuration == 0 || !this.effects.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSimilar(@NotNull ItemMeta itemMeta) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(@NotNull NBTCompound compound) {
        this.effects.clear();
        if (compound.containsKey("Fireworks")) {
            NBTCompound fireworksCompound = compound.getCompound("Fireworks");

            if (fireworksCompound.containsKey("Flight")) {
                this.flightDuration = fireworksCompound.getAsByte("Flight");
            }

            if (fireworksCompound.containsKey("Explosions")) {
                NBTList<NBTCompound> explosions = fireworksCompound.getList("Explosions");

                for (NBTCompound explosion : explosions) {
                    this.effects.add(FireworkEffect.fromCompound(explosion));
                }
            }

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(@NotNull NBTCompound compound) {
        NBTCompound fireworksCompound = new NBTCompound();
        fireworksCompound.setByte("Flight", this.flightDuration);

        NBTList<NBTCompound> explosions = new NBTList<>(NBTTypes.TAG_Compound);
        for (FireworkEffect effect : this.effects) {
            explosions.add(effect.asCompound());
        }

        fireworksCompound.set("Explosions", explosions);

        compound.set("Fireworks", fireworksCompound);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public ItemMeta clone() {
        FireworkMeta fireworkMeta = (FireworkMeta) super.clone();
        fireworkMeta.effects = this.effects;
        fireworkMeta.flightDuration = this.flightDuration;

        return fireworkMeta;
    }
}
