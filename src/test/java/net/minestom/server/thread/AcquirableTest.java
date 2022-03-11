package net.minestom.server.thread;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AcquirableTest {

    @Test
    public void assignation() {
        AtomicReference<TickThread> tickThread = new AtomicReference<>();
        Entity entity = new Entity(EntityType.ZOMBIE) {
            @Override
            public void tick(long time) {
                super.tick(time);
                tickThread.set(getAcquirable().assignedThread());
            }
        };
        Object first = new Object();
        Object second = new Object();

        ThreadDispatcher<Object> dispatcher = ThreadDispatcher.of(ThreadProvider.counter(), 2);
        dispatcher.createPartition(first);
        dispatcher.createPartition(second);

        dispatcher.updateElement(entity, first);
        dispatcher.updateAndAwait(System.currentTimeMillis());
        TickThread firstThread = tickThread.get();
        assertNotNull(firstThread);

        tickThread.set(null);
        dispatcher.updateElement(entity, second);
        dispatcher.updateAndAwait(System.currentTimeMillis());
        TickThread secondThread = tickThread.get();
        assertNotNull(secondThread);

        assertNotEquals(firstThread, secondThread);
    }
}
