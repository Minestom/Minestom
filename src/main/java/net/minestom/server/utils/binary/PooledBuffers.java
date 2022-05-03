package net.minestom.server.utils.binary;

import net.minestom.server.network.socket.Server;
import net.minestom.server.utils.cache.LocalCache;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpmcUnboundedXaddArrayQueue;
import org.jetbrains.annotations.ApiStatus;

import java.lang.ref.Cleaner;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

@ApiStatus.Internal
@ApiStatus.Experimental
public final class PooledBuffers {
    private final static MessagePassingQueue<SoftReference<BinaryBuffer>> POOLED_BUFFERS = new MpmcUnboundedXaddArrayQueue<>(1024);
    private final static int BUFFER_SIZE = Integer.getInteger("minestom.pooled-buffer-size", 262_143);
    private final static Cleaner CLEANER = Cleaner.create();

    private static final LocalCache<ByteBuffer> PACKET_BUFFER = LocalCache.ofBuffer(Server.MAX_PACKET_SIZE);
    private static final LocalCache<ByteBuffer> LOCAL_BUFFER = LocalCache.ofBuffer(Server.MAX_PACKET_SIZE);

    /**
     * Thread local buffer containing raw packet stream.
     */
    public static ByteBuffer packetBuffer() {
        return PACKET_BUFFER.get().clear();
    }

    /**
     * Thread local buffer targeted at very small scope operations (encryption, compression, ...).
     */
    public static ByteBuffer tempBuffer() {
        return LOCAL_BUFFER.get().clear();
    }

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

    public static void clear() {
        POOLED_BUFFERS.clear();
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
