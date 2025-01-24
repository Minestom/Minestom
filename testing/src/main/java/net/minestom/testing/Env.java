package net.minestom.testing;

import net.minestom.server.ServerProcess;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.function.BooleanSupplier;

public interface Env {
    @NotNull ServerProcess process();

    @NotNull TestConnection createConnection();

    <E extends Event, H> @NotNull Collector<E> trackEvent(@NotNull Class<E> eventType, @NotNull EventFilter<? super E, H> filter, @NotNull H actor);

    <E extends Event> @NotNull FlexibleListener<E> listen(@NotNull Class<E> eventType);

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

    default @NotNull Player createPlayer(@NotNull Instance instance, @NotNull Pos pos) {
        return createConnection().connect(instance, pos);
    }

    default @NotNull Instance createFlatInstance() {
        return createFlatInstance(null);
    }

    default @NotNull Instance createFlatInstance(IChunkLoader chunkLoader) {
        var instance = process().instance().createInstanceContainer(chunkLoader);
        instance.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.STONE));
        return instance;
    }

    default void destroyInstance(Instance instance) {
        process().instance().unregisterInstance(instance);
    }
}
