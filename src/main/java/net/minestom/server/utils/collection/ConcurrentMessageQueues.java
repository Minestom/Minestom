package net.minestom.server.utils.collection;

import net.minestom.server.ServerFlag;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpmcUnboundedXaddArrayQueue;
import org.jctools.queues.MpscArrayQueue;
import org.jctools.queues.MpscUnboundedXaddArrayQueue;
import org.jctools.queues.atomic.MpmcAtomicArrayQueue;
import org.jctools.queues.atomic.MpscAtomicArrayQueue;
import org.jctools.queues.atomic.MpscUnboundedAtomicArrayQueue;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class ConcurrentMessageQueues {

    public static <T> MessagePassingQueue<T> mpscArrayQueue(int capacity) {
        return ServerFlag.UNSAFE_COLLECTIONS ? new MpscArrayQueue<>(capacity) : new MpscAtomicArrayQueue<>(capacity);
    }

    public static <T> MessagePassingQueue<T> mpscUnboundedArrayQueue(int chunkSize) {
        return ServerFlag.UNSAFE_COLLECTIONS ? new MpscUnboundedXaddArrayQueue<>(chunkSize) : new MpscUnboundedAtomicArrayQueue<>(chunkSize);
    }

    // Atomic is bounded; no unbounded atomic variant exists that is MPMC.
    public static <T> MessagePassingQueue<T> mpmcSpecialUnboundedArrayQueue(int value) {
        return ServerFlag.UNSAFE_COLLECTIONS ? new MpmcUnboundedXaddArrayQueue<>(value) : new MpmcAtomicArrayQueue<>(value);
    }
}