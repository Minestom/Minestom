package net.minestom.server.adventure.audience;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Holder of custom audiences.
 */
public class AudienceRegistry {

    private final Map<Key, Collection<Audience>> registry;
    private final Function<Key, Collection<Audience>> provider;

    /**
     * Creates a new audience registrar with a given backing map.
     *
     * @param backingMap        the backing map
     * @param backingCollection a provider for the backing collection
     */
    public AudienceRegistry(@NotNull Map<Key, Collection<Audience>> backingMap, @NotNull Supplier<Collection<Audience>> backingCollection) {
        this.registry = backingMap;
        this.provider = key -> backingCollection.get();
    }

    /**
     * Checks if this registry is empty.
     *
     * @return {@code true} if it is, {@code false} otherwise
     */
    public boolean isEmpty() {
        return this.registry.isEmpty();
    }

    /**
     * Adds some audiences to the registry.
     *
     * @param keyed     the provider of the key
     * @param audiences the audiences
     */
    public void register(@NotNull Keyed keyed, @NotNull Audience... audiences) {
        this.register(keyed.key(), audiences);
    }

    /**
     * Adds some audiences to the registry.
     *
     * @param keyed     the provider of the key
     * @param audiences the audiences
     */
    public void register(@NotNull Keyed keyed, @NotNull Collection<Audience> audiences) {
        this.register(keyed.key(), audiences);
    }

    /**
     * Adds some audiences to the registry.
     *
     * @param key       the key to store the audiences under
     * @param audiences the audiences
     */
    public void register(@NotNull Key key, @NotNull Audience... audiences) {
        if (audiences == null || audiences.length == 0) {
            return;
        }

        this.register(key, Arrays.asList(audiences));
    }

    /**
     * Adds some audiences to the registry.
     *
     * @param key       the key to store the audiences under
     * @param audiences the audiences
     */
    public void register(@NotNull Key key, @NotNull Collection<Audience> audiences) {
        if (!audiences.isEmpty()) {
            this.registry.computeIfAbsent(key, this.provider).addAll(audiences);
        }
    }

    /**
     * Gets every audience in the registry.
     *
     * @return an iterable containing every audience member
     */
    public @NotNull Iterable<? extends Audience> all() {
        if (this.isEmpty()) {
            return Collections.emptyList();
        } else {
            return this.registry.values().stream().flatMap(Collection::stream).toList();
        }
    }

    /**
     * Gets every audience in the registry under a specific key.
     *
     * @param keyed the key provider
     * @return an iterable containing the audience members
     */
    public @NotNull Iterable<? extends Audience> of(@NotNull Keyed keyed) {
        return this.of(keyed.key());
    }

    /**
     * Gets every audience in the registry under a specific key.
     *
     * @param key the key
     * @return an iterable containing the audience members
     */
    public @NotNull Iterable<? extends Audience> of(@NotNull Key key) {
        return Collections.unmodifiableCollection(this.registry.getOrDefault(key, this.provider.apply(null)));
    }

    /**
     * Gets every audience member in the registry who matches a given predicate.
     *
     * @param filter the predicate
     * @return the matching audience members
     */
    public @NotNull Iterable<? extends Audience> of(@NotNull Predicate<Audience> filter) {
        return this.registry.values().stream().flatMap(Collection::stream).filter(filter).toList();
    }
}
