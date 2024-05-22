package net.minestom.server.utils;

import net.kyori.adventure.nbt.IntArrayBinaryTag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * An utilities class for {@link UUID}.
 */
@ApiStatus.Internal
public final class UniqueIdUtils {

    public static final String UUID_GROUP_REPLACEMENT = "$1-$2-$3-$4-$5"; // Microtus - improve string pattern usage
    public static final Pattern UNIQUE_ID_PATTERN = Pattern.compile("\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b");

    // Microtus - improve string pattern usage + add private constructor
    private UniqueIdUtils() {}

    /**
     * Checks whether the {@code input} string is an {@link UUID}.
     *
     * @param input The input string to be checked
     * @return {@code true} if the input an unique identifier, otherwise {@code false}
     */
    public static boolean isUniqueId(@NotNull String input) {
        return !input.trim().isEmpty() && UNIQUE_ID_PATTERN.matcher(input).matches(); // Microtus - improve string pattern usage
    }

    public static @NotNull UUID fromNbt(@NotNull IntArrayBinaryTag tag) {
        return intArrayToUuid(tag.value());
    }

    public static @NotNull IntArrayBinaryTag toNbt(@NotNull UUID uuid) {
        return IntArrayBinaryTag.intArrayBinaryTag(uuidToIntArray(uuid));
    }

    public static int[] uuidToIntArray(UUID uuid) {
        final long uuidMost = uuid.getMostSignificantBits();
        final long uuidLeast = uuid.getLeastSignificantBits();
        return new int[]{
                (int) (uuidMost >> 32),
                (int) uuidMost,
                (int) (uuidLeast >> 32),
                (int) uuidLeast
        };
    }

    public static UUID intArrayToUuid(int[] array) {
        final long uuidMost = (long) array[0] << 32 | array[1] & 0xFFFFFFFFL;
        final long uuidLeast = (long) array[2] << 32 | array[3] & 0xFFFFFFFFL;

        return new UUID(uuidMost, uuidLeast);
    }
}
