package net.minestom.server.item.metadata;

import net.minestom.server.color.Color;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import net.minestom.server.potion.CustomPotionEffect;
import net.minestom.server.potion.PotionType;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PotionMeta extends ItemMeta implements ItemMetaBuilder.Provider<PotionMeta.Builder> {

    private final PotionType potionType;
    private final List<CustomPotionEffect> customPotionEffects;
    private final Color color;

    protected PotionMeta(@NotNull ItemMetaBuilder metaBuilder, @Nullable PotionType potionType,
                         List<CustomPotionEffect> customPotionEffects,
                         Color color) {
        super(metaBuilder);
        this.potionType = potionType;
        this.customPotionEffects = new ArrayList<>(customPotionEffects);
        this.color = color;
    }

    public PotionType getPotionType() {
        return potionType;
    }

    public List<CustomPotionEffect> getCustomPotionEffects() {
        return customPotionEffects;
    }

    public Color getColor() {
        return color;
    }

    public static class Builder extends ItemMetaBuilder {

        private PotionType potionType;
        private List<CustomPotionEffect> customPotionEffects = new ArrayList<>();
        private Color color;

        public Builder potionType(@NotNull PotionType potionType) {
            this.potionType = potionType;
            this.nbt.setString("Potion", potionType.getId().asString());
            return this;
        }

        public Builder effects(@NotNull List<CustomPotionEffect> customPotionEffects) {
            this.customPotionEffects = customPotionEffects;

            NBTList<NBTCompound> potionList = new NBTList<>(NBTTypes.TAG_Compound);
            for (CustomPotionEffect customPotionEffect : customPotionEffects) {
                NBTCompound potionCompound = new NBTCompound();
                potionCompound.setByte("Id", customPotionEffect.getId());
                potionCompound.setByte("Amplifier", customPotionEffect.getAmplifier());
                potionCompound.setInt("Duration", customPotionEffect.getDuration());
                potionCompound.setByte("Ambient", (byte) (customPotionEffect.isAmbient() ? 1 : 0));
                potionCompound.setByte("ShowParticles", (byte) (customPotionEffect.showParticles() ? 1 : 0));
                potionCompound.setByte("ShowIcon", (byte) (customPotionEffect.showIcon() ? 1 : 0));

                potionList.add(potionCompound);
            }
            this.nbt.set("CustomPotionEffects", potionList);

            return this;
        }

        public Builder color(@NotNull Color color) {
            this.color = color;
            this.nbt.setInt("CustomPotionColor", color.asRGB());
            return this;
        }

        @Override
        public @NotNull PotionMeta build() {
            return new PotionMeta(this, potionType, customPotionEffects, color);
        }

        @Override
        public void read(@NotNull NBTCompound nbtCompound) {
            if (nbtCompound.containsKey("Potion")) {
                potionType(Registry.POTION_TYPE_REGISTRY.get(nbtCompound.getString("Potion")));
            }

            if (nbtCompound.containsKey("CustomPotionEffects")) {
                NBTList<NBTCompound> customEffectList = nbtCompound.getList("CustomPotionEffects");
                for (NBTCompound potionCompound : customEffectList) {
                    final byte id = potionCompound.getAsByte("Id");
                    final byte amplifier = potionCompound.getAsByte("Amplifier");
                    final int duration = potionCompound.containsKey("Duration") ? potionCompound.getNumber("Duration").intValue() : (int) TimeUnit.SECOND.toMilliseconds(30);
                    final boolean ambient = potionCompound.containsKey("Ambient") ? potionCompound.getAsByte("Ambient") == 1 : false;
                    final boolean showParticles = potionCompound.containsKey("ShowParticles") ? potionCompound.getAsByte("ShowParticles") == 1 : true;
                    final boolean showIcon = potionCompound.containsKey("ShowIcon") ? potionCompound.getAsByte("ShowIcon") == 1 : true;

                    this.customPotionEffects.add(
                            new CustomPotionEffect(id, amplifier, duration, ambient, showParticles, showIcon));
                }
                effects(customPotionEffects);
            }

            if (nbtCompound.containsKey("CustomPotionColor")) {
                color(new Color(nbtCompound.getInt("CustomPotionColor")));
            }
        }

        @Override
        protected @NotNull Supplier<ItemMetaBuilder> getSupplier() {
            return Builder::new;
        }
    }
}