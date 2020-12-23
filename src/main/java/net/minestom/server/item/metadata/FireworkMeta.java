package net.minestom.server.item.metadata;

import net.minestom.server.item.firework.FireworkEffect;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a firework rocket meta data and its effects.
 */
public class FireworkMeta extends ItemMeta {

    private List<FireworkEffect> effects = new ArrayList<>();
    private byte flightDuration;

    public void addFireworkEffect(FireworkEffect effect) {
        this.effects.add(effect);
    }

    public void addFireworkEffects(FireworkEffect... effects) {
        this.effects.addAll(Arrays.asList(effects));
    }

    public void removeFireworkEffect(int index) throws IndexOutOfBoundsException {
        this.effects.remove(index);
    }

    public void removeFireworkEffect(FireworkEffect effect) {
        this.effects.remove(effect);
    }

    public List<FireworkEffect> getEffects() {
        return effects;
    }

    public int getEffectSize() {
        return this.effects.size();
    }

    public void clearEffects() {
        this.effects.clear();
    }

    public boolean hasEffects() {
        return this.effects.isEmpty();
    }

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
