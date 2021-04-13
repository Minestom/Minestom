package net.minestom.server.item.metadata;

import net.minestom.server.color.Color;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.function.Supplier;

public class LeatherArmorMeta extends ItemMeta implements ItemMetaBuilder.Provider<LeatherArmorMeta.Builder> {

    private final Color color;

    protected LeatherArmorMeta(@NotNull ItemMetaBuilder metaBuilder, @Nullable Color color) {
        super(metaBuilder);
        this.color = color;
    }

    public @Nullable Color getColor() {
        return color;
    }

    public static class Builder extends ItemMetaBuilder {

        private Color color;

        public Builder color(@Nullable Color color) {
            this.color = color;
            handleCompound("display", nbtCompound -> {
                if (color != null) {
                    nbtCompound.setInt("color", color.asRGB());
                } else {
                    nbtCompound.removeTag("color");
                }
            });
            return this;
        }

        @Override
        public @NotNull LeatherArmorMeta build() {
            return new LeatherArmorMeta(this, color);
        }

        @Override
        public void read(@NotNull NBTCompound nbtCompound) {
            if (nbtCompound.containsKey("display")) {
                final NBTCompound displayCompound = nbtCompound.getCompound("display");
                if (displayCompound.containsKey("color")) {
                    color(new Color(displayCompound.getInt("color")));
                }
            }
        }

        @Override
        protected @NotNull Supplier<ItemMetaBuilder> getSupplier() {
            return Builder::new;
        }
    }
}
