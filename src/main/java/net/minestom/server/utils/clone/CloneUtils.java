package net.minestom.server.utils.clone;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class CloneUtils {

    @NotNull
    public static <T extends PublicCloneable> CopyOnWriteArrayList cloneCopyOnWriteArrayList(@NotNull List<T> list) {
        CopyOnWriteArrayList<T> result = new CopyOnWriteArrayList<>();
        for (T element : list) {
            result.add((T) element.clone());
        }
        return result;
    }

}
