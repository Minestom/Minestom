package net.minestom.server.thread.batch;

import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.lock.AcquirableElement;
import net.minestom.server.thread.BatchThread;
import net.minestom.server.utils.callback.validator.EntityValidator;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BatchSetupHandler implements BatchHandler {

    private static final int INSTANCE_COST = 5;
    private static final int CHUNK_COST = 5;
    private static final int ENTITY_COST = 5;

    private ArrayList<AcquirableElement<?>> elements = new ArrayList<>();
    private int cost;

    @Override
    public void updateInstance(@NotNull Instance instance, long time) {
        this.elements.add(instance.getAcquiredElement());
        cost += INSTANCE_COST;
    }

    @Override
    public void updateChunk(@NotNull Instance instance, @NotNull Chunk chunk, long time) {
        this.elements.add(chunk.getAcquiredElement());
        cost += CHUNK_COST;
    }

    @Override
    public void conditionalEntityUpdate(@NotNull Instance instance, @NotNull Chunk chunk, long time, @Nullable EntityValidator condition) {
        final Set<Entity> entities = instance.getChunkEntities(chunk);

        for (Entity entity : entities) {
            if (shouldTick(entity, condition)) {
                this.elements.add(entity.getAcquiredElement());
                cost += ENTITY_COST;
            }
        }
    }

    public void pushTask(@NotNull List<BatchThread> threads, long time) {
        BatchThread fitThread = null;
        int minCost = Integer.MAX_VALUE;

        for (BatchThread thread : threads) {
            final boolean switchThread = fitThread == null || thread.getCost() < minCost;
            if (switchThread) {
                fitThread = thread;
                minCost = thread.getCost();
            }
        }

        Check.notNull(fitThread, "The task thread returned null, something went terribly wrong.");

        // The thread has been decided, all elements need to be have its identifier
        {
            final UUID threadIdentifier = fitThread.getIdentifier();
            final Queue<AcquirableElement.AcquisitionLock> acquisitionQueue = fitThread.getWaitingAcquisitionQueue();
            for (AcquirableElement<?> element : elements) {
                element.getHandler().refreshThread(threadIdentifier, acquisitionQueue);
            }
        }

        final Runnable runnable = createRunnable(time);

        fitThread.addRunnable(runnable, cost);
    }

    @NotNull
    private Runnable createRunnable(long time) {
        return () -> {
            for (AcquirableElement<?> element : elements) {
                Object unwrapElement = element.unsafeUnwrap();

                if (unwrapElement instanceof Instance) {
                    ((Instance) unwrapElement).tick(time);
                    // FIXME: shared instance
                } else if (unwrapElement instanceof Chunk) {
                    // FIXME: instance null
                    ((Chunk) unwrapElement).tick(time, null);
                } else if (unwrapElement instanceof Entity) {
                    ((Entity) unwrapElement).tick(time);
                }
            }
        };
    }

}
