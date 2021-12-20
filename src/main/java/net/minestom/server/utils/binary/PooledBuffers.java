package net.minestom.server.utils.binary;

import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpmcUnboundedXaddArrayQueue;
import org.jetbrains.annotations.ApiStatus;

import java.lang.ref.Cleaner;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

@ApiStatus.Internal
@ApiStatus.Experimental
public final class PooledBuffers {
    private final static MessagePassingQueue<SoftReference<BinaryBuffer>> POOLED_BUFFERS = new MpmcUnboundedXaddArrayQueue<>(1024);
    private final static int BUFFER_SIZE = Integer.getInteger("minestom.pooled-buffer-size", 262_143);
    private final static Cleaner CLEANER = Cleaner.create();

    public static BinaryBuffer get() {
        BinaryBuffer buffer;
        SoftReference<BinaryBuffer> ref;
        while ((ref = POOLED_BUFFERS.relaxedPoll()) != null) {
            if ((buffer = ref.get()) != null) return buffer;
        }
        return BinaryBuffer.ofSize(BUFFER_SIZE);
    }

    public static void add(BinaryBuffer buffer) {
        POOLED_BUFFERS.relaxedOffer(new SoftReference<>(buffer.clear()));
    }

    public static int count() {
        return POOLED_BUFFERS.size();
    }

    public static int bufferSize() {
        return BUFFER_SIZE;
    }

    public static void registerBuffer(Object ref, AtomicReference<BinaryBuffer> buffer) {
        CLEANER.register(ref, new BufferRefCleaner(buffer));
    }

    public static void registerBuffer(Object ref, BinaryBuffer buffer) {
        CLEANER.register(ref, new BufferCleaner(buffer));
    }

    public static void registerBuffers(Object ref, Collection<BinaryBuffer> buffers) {
        CLEANER.register(ref, new BuffersCleaner(buffers));
    }

    private record BufferRefCleaner(AtomicReference<BinaryBuffer> bufferRef) implements Runnable {
        @Override
        public void run() {
            add(bufferRef.get());
        }
    }

    private record BufferCleaner(BinaryBuffer buffer) implements Runnable {
        @Override
        public void run() {
            add(buffer);
        }
    }

    private record BuffersCleaner(Collection<BinaryBuffer> buffers) implements Runnable {
        @Override
        public void run() {
            if (buffers.isEmpty()) return;
            for (BinaryBuffer buffer : buffers) {
                add(buffer);
            }
        }
    }
}
