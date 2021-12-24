package net.minestom.server.item.metadata;

import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import net.minestom.server.item.firework.FireworkEffect;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FireworkMeta extends ItemMeta implements ItemMetaBuilder.Provider<FireworkMeta.Builder> {

    private final List<FireworkEffect> effects;
    private final byte flightDuration;

    protected FireworkMeta(@NotNull ItemMetaBuilder metaBuilder, List<FireworkEffect> effects,
                           byte flightDuration) {
        super(metaBuilder);
        this.effects = List.copyOf(effects);
        this.flightDuration = flightDuration;
    }

    public List<FireworkEffect> getEffects() {
        return effects;
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
                nbtCompound.set("Explosions", NBT.List(
                        NBTType.TAG_Compound,
                        effects.stream()
                                .map(FireworkEffect::asCompound)
                                .toList()
                ));
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
            if (nbtCompound.get("Fireworks") instanceof NBTCompound fireworksCompound) {
                if (fireworksCompound.get("Flight") instanceof NBTByte flight) {
                    this.flightDuration = flight.getValue();
                }

                if (fireworksCompound.get("Explosions") instanceof NBTList<?> list &&
                        list.getSubtagType() == NBTType.TAG_Compound) {
                    for (NBTCompound explosion : list.<NBTCompound>asListOf()) {
                        this.effects.add(FireworkEffect.fromCompound(explosion));
                    }
                }
            }
        }
    }
}