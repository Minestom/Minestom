package net.minestom.server.instance.block.predicate;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.TagStringIOExt;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public record NbtPredicate(@Nullable CompoundBinaryTag nbt) implements Predicate<@Nullable BinaryTag> {
    private static final Codec<CompoundBinaryTag> NBT_STRING_CODEC = Codec.STRING.transform(
            string -> {
                BinaryTag value = TagStringIOExt.readTag(string);
                if (!(value instanceof CompoundBinaryTag compound))
                    throw new IllegalArgumentException("Not a compound: " + value);
                return compound;
            },
            TagStringIOExt::writeTag
    );
    public static final Codec<NbtPredicate> CODEC = Codec.NBT_COMPOUND.orElse(NBT_STRING_CODEC).transform(NbtPredicate::new, NbtPredicate::nbt);
    public static final NetworkBuffer.Type<NbtPredicate> NETWORK_TYPE = NetworkBuffer.NBT_COMPOUND.transform(NbtPredicate::new, NbtPredicate::nbt);

    /**
     * Checks to see if everything in {@code standard} is contained in {@code comparison}. The comparison is allowed to
     * have extra fields that are not contained in the standard.
     *
     * @param standard   the standard that the comparison must have all elements of
     * @param comparison the comparison, that is being compared against the standard. NBT compounds in this parameter,
     *                   whether deeper in the tree or not, are allowed to have keys that the standard does not - it's
     *                   basically compared against a standard.
     * @return true if the comparison fits the standard, otherwise false
     */
    // Modified from GoldenStack's trove library under MIT license: https://github.com/GoldenStack/trove/blob/d8f329644c2025cd71802450c7c36a4a6a0c0410/src/main/java/net/goldenstack/loot/util/nbt/NBTUtils.java#L13-L64
    public static boolean compareNBT(@Nullable BinaryTag standard, @Nullable BinaryTag comparison) {
        if (standard == null) {
            return true; // If there's no standard, it must always pass
        } else if (comparison == null) {
            return false; // If it's null at this point, we already assured that standard is null, so it must be invalid
        } else if (!standard.type().equals(comparison.type())) {
            return false; // If the classes aren't equal it can't fulfill the standard anyway
        }

        if (standard instanceof ListBinaryTag guaranteeList) {
            ListBinaryTag comparisonList = ((ListBinaryTag) comparison);
            if (guaranteeList.isEmpty()) {
                return comparisonList.isEmpty();
            }
            for (BinaryTag nbt : guaranteeList) {
                boolean contains = false;
                for (BinaryTag compare : comparisonList) {
                    if (compareNBT(nbt, compare)) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    return false;
                }
            }
            return true;
        }

        if (standard instanceof CompoundBinaryTag standardCompound) {
            CompoundBinaryTag comparisonCompound = ((CompoundBinaryTag) comparison);
            for (String key : standardCompound.keySet()) {
                if (!compareNBT(standardCompound.get(key), comparisonCompound.get(key))) {
                    return false;
                }
            }
            return true;
        }

        return standard.equals(comparison);
    }

    @Override
    public boolean test(@Nullable BinaryTag other) {
        return compareNBT(nbt, other);
    }
}
