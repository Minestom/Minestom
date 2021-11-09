package net.minestom.server.thread;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@ApiStatus.Internal
@ApiStatus.NonExtendable
public class MinestomThread extends Thread {
    public static final AtomicInteger LOCAL_COUNT = new AtomicInteger();
    private Object[] locals = new Object[0];

    public MinestomThread(@Nullable Runnable target, String name) {
        super(target, name);
    }

    public MinestomThread(Runnable target) {
        super(target);
    }

    public MinestomThread(@NotNull String name) {
        super(name);
    }

    @ApiStatus.Internal
    @ApiStatus.Experimental
    public <T> T localCache(int index, Supplier<T> supplier) {
        Object[] array = locals;
        T value;
        final int requiredLength = index + 1;
        if (array.length < requiredLength) {
            Object[] temp = new Object[requiredLength];
            System.arraycopy(temp, 0, temp, 0, array.length);
            array = temp;
            this.locals = array;
        }
        if ((value = (T) array[index]) == null) {
            value = supplier.get();
            array[index] = value;
        }
        return value;
    }
}
