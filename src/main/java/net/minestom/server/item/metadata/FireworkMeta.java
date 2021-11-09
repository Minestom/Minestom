package net.minestom.server.item.metadata;

import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import net.minestom.server.item.firework.FireworkEffect;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public class FireworkMeta extends ItemMeta implements ItemMetaBuilder.Provider<FireworkMeta.Builder> {

    private final List<FireworkEffect> effects;
    private final byte flightDuration;

    protected FireworkMeta(@NotNull ItemMetaBuilder metaBuilder, List<FireworkEffect> effects,
                           byte flightDuration) {
        super(metaBuilder);
        this.effects = new ArrayList<>(effects);
        this.flightDuration = flightDuration;
    }

    public List<FireworkEffect> getEffects() {
        return Collections.unmodifiableList(effects);
    }

    public byte getFlightDuration() {
        return flightDuration;
    }

    public static class Builder extends ItemMetaBuilder {

        private List<FireworkEffect> effects = new CopyOnWriteArrayList<>();
        private byte flightDuration;

        public Builder effects(List<FireworkEffect> effects) {
            this.effects = effects;
            handleCompound("Fireworks", nbtCompound -> {
                NBTList<NBTCompound> explosions = new NBTList<>(NBTTypes.TAG_Compound);
                for (FireworkEffect effect : this.effects) {
                    explosions.add(effect.asCompound());
                }
                nbtCompound.set("Explosions", explosions);
            });
            return this;
        }

        public Builder flightDuration(byte flightDuration) {
            this.flightDuration = flightDuration;
            handleCompound("Fireworks", nbtCompound ->
                    nbtCompound.setByte("Flight", this.flightDuration));
            return this;
        }

        @Override
        public @NotNull FireworkMeta build() {
            return new FireworkMeta(this, effects, flightDuration);
        }

        @Override
        public void read(@NotNull NBTCompound nbtCompound) {
            if (nbtCompound.containsKey("Fireworks")) {
                NBTCompound fireworksCompound = nbtCompound.getCompound("Fireworks");

                if (fireworksCompound.containsKey("Flight")) {
                    flightDuration(fireworksCompound.getAsByte("Flight"));
                }

                if (fireworksCompound.containsKey("Explosions")) {
                    NBTList<NBTCompound> explosions = fireworksCompound.getList("Explosions");

                    for (NBTCompound explosion : explosions) {
                        this.effects.add(FireworkEffect.fromCompound(explosion));
                    }
                    effects(effects);
                }
            }
        }

        @Override
        protected @NotNull Supplier<ItemMetaBuilder> getSupplier() {
            return Builder::new;
        }
    }
}