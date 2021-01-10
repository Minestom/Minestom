package net.minestom.server.utils.clone;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Convenient interface to deep-copy single object or collections.
 * <p>
 * Most of the methods require object to implement the {@link PublicCloneable} interface.
 */
public final class CloneUtils {

    @SuppressWarnings("unchecked")
	@Nullable
    public static <T extends PublicCloneable<?>> T optionalClone(@Nullable T object) {
        return object != null ? (T) object.clone() : null;
    }

    @SuppressWarnings("unchecked")
	@NotNull
    public static <T extends PublicCloneable<?>> CopyOnWriteArrayList<T> cloneCopyOnWriteArrayList(@NotNull List<T> list) {
        CopyOnWriteArrayList<T> result = new CopyOnWriteArrayList<>();
        for (T element : list) {
            result.add((T) element.clone());
        }
        return result;
    }

}
