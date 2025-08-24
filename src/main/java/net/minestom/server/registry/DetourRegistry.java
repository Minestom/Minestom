package net.minestom.server.registry;

import net.kyori.adventure.key.Keyed;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.util.function.Function;

/**
 * A registry for {@code Detour}'s, which allows modifying the behavior of registry entries.
 * <p>
 *     Detours are used in pre-initialization of a {@link Registry}.
 *     They are used to modify the behavior of registry entries before they are fully initialized.
 *     This way they can be "frozen", which prohibits further modifications to the registry entries.
 * </p>
 * <p>
 *     Note: The detour registry is only used during the pre-initialization phase of the server.
 * </p>
 */
public sealed interface DetourRegistry permits DetourRegistryImpl {
    /**
     * Returns the singleton instance of the {@link DetourRegistry}.
     * Used to register detours for registry keys and tags.
     * <p>
     * @return the singleton instance of the DetourRegistry
     */
    static DetourRegistry detourRegistry() {
        return DetourRegistryImpl.INSTANCE;
    }

    /**
     * Registers a detour for the given registry key.
     *
     * @param key    the registry key to register the detour for {@link BuiltinRegistries}
     * @param detour the detour to register
     * @param <T>    the type of the registry entry
     */
    <T> void register(RegistryKey<T> key, Detour<T> detour);

    /**
     * Registers a detour for the given tag key.
     * @param key the tag key to register the detour for
     * @param detour the detour to register
     * @param <T> the type of the registry entry
     */
    <T> void register(TagKey<T> key, Detour<RegistryTag.Builder<T>> detour);

    /**
     * Checks if a detour is registered for the given registry key.
     * @param key the registry key to check
     * @return true if a detour is registered for the key, false otherwise
     */
    boolean hasDetour(Keyed key);

    /**
     * Checks if there are any detours in the registry.
     *
     * <p>Note this should be false after all detours have been consumed</p>
     *
     * @return true if there are any detours left, false otherwise
     */
    boolean hasDetours();

    /**
     * Consumes the detour for the given registry key and applies it to the provided value.
     * @param key the registry key to consume the detour for
     * @param value the value to apply the detour to
     * @return the modified value after applying the detour
     * @param <T> the type of the registry entry
     */
    @ApiStatus.Internal
    <T> T consume(RegistryKey<T> key, T value);

    /**
     * Consumes the detour for the given registry key and applies it to the provided value.
     * @param key the registry key to consume the detour for
     * @param <T> the type of the registry entry
     */
    @ApiStatus.Internal
    @Contract(mutates = "param2")
    <T> void consume(TagKey<T> key, RegistryTag.Builder<T> builder);

    /**
     * A functional interface representing a detour that can be applied to a value.
     *
     * @param <T> the type of the value that the detour applies to
     */
    @FunctionalInterface
    interface Detour<T> extends Function<T, T> {

        /**
         * Applies the detour to the given value.
         *
         * @param value the value to apply the detour to
         * @return the modified value
         */
        T apply(T value);

        /**
         * Combines this detour with another detour, creating a new detour that applies both in sequence.
         * @param after the detour to apply after this one
         * @return a new detour that applies this detour followed by the after detour
         */
        default Detour<T> andThen(Detour<T> after) {
            Check.notNull(after, "After detour cannot be null");
            return value -> {
                T intermediate = this.apply(value);
                return after.apply(intermediate);
            };
        }
    }
}
