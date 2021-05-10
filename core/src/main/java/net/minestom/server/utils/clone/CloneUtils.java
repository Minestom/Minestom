package net.minestom.server.utils.clone;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.IntFunction;

/**
 * Convenient interface to deep-copy single object or collections.
 * <p>
 * Most of the methods require object to implement the {@link PublicCloneable} interface.
 */
public final class CloneUtils {

    @Nullable
    public static <T extends PublicCloneable<T>> T optionalClone(@Nullable T object) {
        return object != null ? object.clone() : null;
    }

    @NotNull
    public static <T extends PublicCloneable<T>> CopyOnWriteArrayList<T> cloneCopyOnWriteArrayList(@NotNull List<T> list) {
        CopyOnWriteArrayList<T> result = new CopyOnWriteArrayList<>();
        for (T element : list) {
            result.add(element.clone());
        }
        return result;
    }

    public static <T extends PublicCloneable<T>> T[] cloneArray(@NotNull T[] array, IntFunction<T[]> arraySupplier) {
        T[] result = arraySupplier.apply(array.length);
        for (int i = 0; i < result.length; i++) {
            result[i] = optionalClone(array[i]);
        }
        return result;
    }

}
