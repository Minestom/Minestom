package net.minestom.server.config;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.kyori.adventure.nbt.*;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

public record NoiseConfig(int firstOctave, DoubleList amplitudes) {
    public static final BinaryTagSerializer<NoiseConfig> NBT_TYPE = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull Context context, @NotNull NoiseConfig value) {
            CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
            ListBinaryTag.Builder<DoubleBinaryTag> list = ListBinaryTag.builder(BinaryTagTypes.DOUBLE);
            for (double amplitude : value.amplitudes()) {
                list.add(DoubleBinaryTag.doubleBinaryTag(amplitude));
            }

            builder.putInt("firstOctave", value.firstOctave());
            builder.put("amplitudes", list.build());

            return builder.build();
        }

        @Override
        public @NotNull NoiseConfig read(@NotNull Context context, @NotNull BinaryTag tag) {
            if (!(tag instanceof CompoundBinaryTag compound))
                throw new IllegalArgumentException("Compound expected for Noise");

            ListBinaryTag listTag = compound.getList("amplitudes", BinaryTagTypes.DOUBLE);
            DoubleList amplitudes = new DoubleArrayList();
            for (BinaryTag binaryTag : listTag) {
                amplitudes.add(((NumberBinaryTag) binaryTag).doubleValue());
            }

            return new NoiseConfig(compound.getInt("firstOctave"), amplitudes);
        }
    };
}
