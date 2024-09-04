package net.minestom.testing;

import net.minestom.server.ServerProcess;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.function.BooleanSupplier;

/**
 * The {@code Env} class facilitates the creation of tests (e.g., JUnit tests) that interact directly
 * with the server. It provides a simple way to set up a test environment where server
 * components like players, instances, and other entities can be created and manipulated.
 *
 * <p>This class is particularly useful for testing code that interacts with server components, which would
 * otherwise require the use of a mocking framework. By using this class, developers can create more realistic
 * tests that involve actual server behavior, without needing to mock every component.
 *
 * <p>The {@code Env} class provides utility methods to:
 * <ul>
 *   <li>Create new object references for {@link net.minestom.server.entity.Player Player}</li>
 *   <li>Initialize and manipulate server {@link net.minestom.server.instance.Instance Instances}</li>
 *   <li>Simulate server events and other interactions</li>
 * </ul>
 *
 * <p>This class is intended for use in unit tests, integration tests, and other testing scenarios where
 * interaction with a live server environment is necessary.
 *
 * @version 1.0.1
 * @since 1.0.0
 */
public interface Env {

    /**
     * Creates a new instance of {@link Env} with the given {@link ServerProcess}.
     *
     * @param process the process to use
     * @return a new instance of {@link Env}
     */
    @Contract(value = "_ -> new", pure = true)
    static @NotNull Env createInstance(@NotNull ServerProcess process) {
        return new EnvImpl(process);
    }

    /**
     * Gets the {@link ServerProcess} used by this environment.
     *
     * @return the server process
     */
    @NotNull ServerProcess process();

    /**
     * Creates a new {@link TestConnection} which can be used in the test environment.
     *
     * @return the created connection
     */
    @NotNull TestConnection createConnection();

    /**
     * Tracks a specific event type in the test environment.
     *
     * @param eventType the event type to track
     * @param <E>       the event type
     * @return the {@link Collector} instance to use
     */
    <E extends Event, H> @NotNull Collector<E> trackEvent(@NotNull Class<E> eventType, @NotNull EventFilter<? super E, H> filter, @NotNull H actor);

    /**
     * Listen for a specific event type in the test environment.
     *
     * @param eventType the event type to listen for
     * @param <E>       the event type
     * @return the {@link FlexibleListener} instance to use
     */
    <E extends Event> @NotNull FlexibleListener<E> listen(@NotNull Class<E> eventType);

    /**
     * Ticks the {@link ServerProcess} which is involved into the env instance.
     */
    default void tick() {
        process().ticker().tick(System.nanoTime());
    }

    /**
     * Ticks the {@link ServerProcess} until the given condition is met.
     *
     * @param condition the condition to check
     */
    default boolean tickWhile(@NotNull BooleanSupplier condition, @Nullable Duration timeout) {
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

    /**
     * Creates a new instance of an {@link Player} which can be used in the test environment.
     *
     * @param instance the instance to spawn the player in
     * @param pos      the position to spawn the player at
     * @return the created player
     */
    default @NotNull Player createPlayer(@NotNull Instance instance, @NotNull Pos pos) {
        return createConnection().connect(instance, pos).join();
    }

    /**
     * Creates a new instance of an {@link Player} which can be used in the test environment.
     *
     * @param instance the instance to spawn the player in
     * @return the created player
     */
    default @NotNull Player createPlayer(@NotNull Instance instance) {
        return createPlayer(instance, Pos.ZERO);
    }

    /**
     * Creates a new {@link Instance} which contains only one layer of stone blocks.
     *
     * @return the created instance
     */
    default @NotNull Instance createFlatInstance() {
        return createFlatInstance(null);
    }

    /**
     * Creates a new {@link Instance} which contains only one layer of stone blocks.
     *
     * @param chunkLoader the chunk loader to use for the instance
     * @return the created instance
     */
    default @NotNull Instance createFlatInstance(@Nullable IChunkLoader chunkLoader) {
        var instance = process().instance().createInstanceContainer(chunkLoader);
        instance.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.STONE));
        return instance;
    }

    /**
     * Destroys the given {@link Instance} from the test environment.
     * Note: This method does not remove players from the instance.
     *
     * @param instance the instance to destroy
     */
    default void destroyInstance(@NotNull Instance instance) {
        destroyInstance(instance, false);
    }

    /**
     * Destroys the given {@link Instance} from the test environment.
     *
     * @param instance       the instance to destroy
     * @param cleanUpPlayers whether to remove players from the instance
     */
    default void destroyInstance(@NotNull Instance instance, boolean cleanUpPlayers) {
        if (cleanUpPlayers && !instance.getPlayers().isEmpty()) {
            instance.getPlayers().forEach(Player::remove);
        }
        process().instance().unregisterInstance(instance);
    }

    /**
     * Cleanup the test environment
     *
     * @since 1.4.1
     */
    void cleanup();
}
