package net.minestom.server.instance.light;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractLight implements Light {
    private final AtomicBoolean requiresSend = new AtomicBoolean(true);
    private final AtomicLong indexGenerator = new AtomicLong(Long.MIN_VALUE);
    private final ReentrantLock lock = new ReentrantLock();
    /**
     * Whether this chunk section requires an update
     */
    private volatile boolean requiresUpdate = true;
    /**
     * The content of this section. This does not consider neighbors/propagated light.
     * Basically a standalone computation of only this chunk section.
     */
    private byte @Nullable [] contentSelf;
    /**
     * The content propagated from other sections into this section.
     * This does NOT include the "self" chunk section.
     */
    private byte @Nullable [] contentPropagated;
    /**
     * A builder for {@link #contentPropagated}.
     * This will get modified and then when it is complete swapped into {@link #contentPropagated}.
     */
    private byte @Nullable [] contentPropagatedBuilding;
    /**
     * Fully baked content, saved for caching/faster repeated access
     */
    private byte @Nullable [] contentBaked;

    @Override
    public boolean requiresSend() {
        return requiresSend.getAndSet(false);
    }

    @Override
    public byte[] array() {
        lock.lock();
        try {
            var baked = this.contentBaked;
            if (baked != null) return baked;
            var contentSelf = this.contentSelf;
            if (contentSelf == null) return NewLightComputation.EMPTY_CONTENT;
            var contentPropagated = this.contentPropagated;
            if (contentPropagated == null) return contentSelf;
            var contentBaked = NewLightComputation.bake(contentSelf, contentPropagated);
            this.contentBaked = contentBaked;
            return contentBaked;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void flip() {
        lock.lock();
        try {
            if (this.contentPropagatedBuilding != null) {
                this.contentPropagated = this.contentPropagatedBuilding;
                this.contentPropagatedBuilding = null;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getLevel(int x, int y, int z) {
        lock.lock();
        try {
            if (contentBaked != null) {
                return NewLightComputation.getLight(contentBaked, x, y, z);
            }
            if (contentSelf == null) {
                // Light not yet initialized
                return 0;
            }
            var index = NewLightComputation.index(x, y, z);
            if (contentPropagated == null) {
                // There is no propagated content available, so this should just be a single-loaded chunk
                return NewLightComputation.getLight(contentSelf, index);
            }
            // Get the maximum of internal/propagated light
            return Math.max(NewLightComputation.getLight(contentSelf, index), NewLightComputation.getLight(contentPropagated, index));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void invalidate() {
        lock.lock();
        try {
            this.contentPropagated = null;
            this.requiresSend.set(true);
            this.requiresUpdate = true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean requiresUpdate() {
        return requiresUpdate;
    }

    @Override
    public void set(byte[] copyArray) {
        lock.lock();
        try {
            this.contentSelf = copyArray.clone();
            this.contentPropagated = NewLightComputation.EMPTY_CONTENT;
            this.requiresUpdate = false;
            this.requiresSend.set(true);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public LightCalculation createInternalCalculation(Palette blockPalette, int chunkX, int chunkY, int chunkZ, int[] heightmap, int maxY, LightLookup lightLookup) {
        lock.lock();
        try {
            return new LightCalculation() {
                private final long index = indexGenerator.getAndIncrement();
                private final InternalCalculation calculation = internalCalculation(blockPalette, chunkX, chunkY, chunkZ, heightmap, maxY, lightLookup);

                @Override
                public long index() {
                    return index;
                }

                @Override
                public void calculate() {
                    calculation.calculate();
                }
            };
        } finally {
            lock.unlock();
        }
    }

    @Override
    public LightCalculation createExternalCalculation(Palette blockPalette, Point[] neighbors, LightLookup lightLookup, PaletteLookup paletteLookup) {
        lock.lock();
        try {
            return new LightCalculation() {
                private final long index = indexGenerator.getAndIncrement();
                private final ExternalCalculation calculation = externalCalculation(blockPalette, neighbors, lightLookup, paletteLookup);

                @Override
                public long index() {
                    return index;
                }

                @Override
                public void calculate() {
                    calculation.calculate();
                }
            };
        } finally {
            lock.unlock();
        }
    }

    protected abstract InternalCalculation internalCalculation(Palette blockPalette, int chunkX, int chunkY, int chunkZ, int[] heightmap, int maxY, LightLookup lightLookup);

    protected abstract ExternalCalculation externalCalculation(Palette blockPalette, Point[] neighbors, LightLookup lightLookup, PaletteLookup paletteLookup);

    public interface InternalCalculation {
        void calculate();
    }

    public interface ExternalCalculation {
        void calculate();
    }
}
