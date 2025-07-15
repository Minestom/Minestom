package net.minestom.testing;

import net.minestom.server.ServerProcess;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.time.Duration;
import java.util.function.BooleanSupplier;

public interface Env {
    ServerProcess process();

    TestConnection createConnection();

    <E extends Event, H> Collector<E> trackEvent(Class<E> eventType, EventFilter<? super E, H> filter, H actor);

    <E extends Event> FlexibleListener<E> listen(Class<E> eventType);

    default void tick() {
        process().ticker().tick(System.nanoTime());
    }

    default boolean tickWhile(BooleanSupplier condition, Duration timeout) {
        var ticker = process().ticker();
        final long start = System.nanoTime();
        while (condition.getAsBoolean()) {
            final long tick = System.nanoTime();
            ticker.tick(tick);
            if (timeout != null && System.nanoTime() - start > timeout.toNanos()) {
                return false;
            }
        }
        return true;
    }

    default Player createPlayer(Instance instance, Pos pos) {
        return createConnection().connect(instance, pos);
    }

    default Instance createFlatInstance() {
        return createFlatInstance(null);
    }

    default Instance createFlatInstance(IChunkLoader chunkLoader) {
        var instance = process().instance().createInstanceContainer(chunkLoader);
        instance.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.STONE));
        return instance;
    }

    default Instance createEmptyInstance() {
        return process().instance().createInstanceContainer();
    }

    default void destroyInstance(Instance instance) {
        process().instance().unregisterInstance(instance);
    }
}
