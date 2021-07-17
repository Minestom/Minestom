package net.minestom.server.item.metadata;

import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import net.minestom.server.item.firework.FireworkEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.function.Supplier;

public class FireworkEffectMeta extends ItemMeta implements ItemMetaBuilder.Provider<FireworkEffectMeta.Builder> {

    private final FireworkEffect fireworkEffect;

    protected FireworkEffectMeta(@NotNull ItemMetaBuilder metaBuilder, FireworkEffect fireworkEffect) {
        super(metaBuilder);
        this.fireworkEffect = fireworkEffect;
    }

    public FireworkEffect getFireworkEffect() {
        return fireworkEffect;
    }

    public static class Builder extends ItemMetaBuilder {

        private FireworkEffect fireworkEffect;

        public Builder effect(@Nullable FireworkEffect fireworkEffect) {
            this.fireworkEffect = fireworkEffect;
            mutateNbt(compound -> compound.set("Explosion", this.fireworkEffect.asCompound()));
            return this;
        }

        @Override
        public @NotNull FireworkEffectMeta build() {
            return new FireworkEffectMeta(this, fireworkEffect);
        }

        @Override
        public void read(@NotNull NBTCompound nbtCompound) {
            if (nbtCompound.containsKey("Explosion")) {
                effect(FireworkEffect.fromCompound(nbtCompound.getCompound("Explosion")));
            }
        }

        @Override
        protected @NotNull Supplier<ItemMetaBuilder> getSupplier() {
            return Builder::new;
        }
    }
}