package net.minestom.server.utils;

import net.minestom.server.Viewable;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Defines which players are able to see this element.
 */
@ApiStatus.Internal
public final class ViewEngine {
    private final Entity entity;
    /**
     * Represents viewers that have been added manually using {@link Viewable#addViewer(Player)}.
     */
    private final Set<Player> manualViewers = new HashSet<>();
    /**
     * Represents viewers that should normally be auto-visible
     * but have been manually removed using {@link Viewable#removeViewer(Player)}.
     */
    private final Set<Player> exceptionViewersMap = new HashSet<>();
    /**
     * Predicate used to define if a player should be auto-viewable.
     * <p>
     * Useful if you want to filter based on rank.
     */
    private final Predicate<Player> autoViewPredicate;
    /**
     * References of all the player lists surrounding the entity.
     * <p>
     * Used as an efficient way to represent visible entities
     * without continuously updating an internal collection.
     */
    private List<List<Player>> autoViewable;

    private final Set<Player> set = new SetImpl();
    private final Object mutex = this;

    public ViewEngine(@Nullable Entity entity) {
        this.entity = entity;
        this.autoViewPredicate = player ->
                player != entity &&
                        player.isAutoViewable() &&
                        !setContain(exceptionViewersMap, player);
    }

    public void updateReferences(@Nullable List<List<Player>> references) {
        synchronized (mutex) {
            this.autoViewable = references;
        }
    }

    public boolean manualAdd(@NotNull Player player) {
        synchronized (mutex) {
            this.exceptionViewersMap.remove(player);
            return manualViewers.add(player);
        }
    }

    public boolean manualRemove(@NotNull Player player) {
        synchronized (mutex) {
            if (!manualViewers.isEmpty() && manualViewers.remove(player)) return true;
            return exceptionViewersMap.add(player);
        }
    }

    public boolean hasPredictableViewers() {
        // Verify if this entity's viewers can be predicted from surrounding entities
        synchronized (mutex) {
            return entity != null && entity.isAutoViewable() &&
                    manualViewers.isEmpty() &&
                    exceptionViewersMap.isEmpty();
        }
    }

    public void computeValidAutoViewer(@NotNull Entity entity, Runnable runnable) {
        if (!entity.isAutoViewable()) return;
        // Ensure that an entity can be auto-viewed
        // In this case, it should be neither in the manual nor exception map
        synchronized (mutex) {
            if (!(entity instanceof Player player)) {
                runnable.run();
                return;
            }
            if (setContain(manualViewers, player)) return;
            if (autoViewPredicate.test(player)) runnable.run();
        }
    }

    public void forAutoViewers(Consumer<Player> consumer) {
        synchronized (mutex) {
            if (autoViewable == null) return;
            for (List<Player> players : autoViewable) {
                if (players.isEmpty()) continue;
                for (Player player : players) {
                    computeValidAutoViewer(player, () -> consumer.accept(player));
                }
            }
        }
    }

    public Set<Player> asSet() {
        return set;
    }

    final class SetImpl extends AbstractSet<Player> {
        @Override
        public @NotNull Iterator<Player> iterator() {
            synchronized (mutex) {
                return new It();
            }
        }

        @Override
        public int size() {
            synchronized (mutex) {
                int size = ViewEngine.this.manualViewers.size();
                final List<List<Player>> auto = ViewEngine.this.autoViewable;
                if (auto != null) {
                    for (List<Player> players : auto) {
                        if (players.isEmpty()) continue;
                        for (Player player : players) {
                            if (autoViewPredicate.test(player)) size++;
                        }
                    }
                }
                return size;
            }
        }

        @Override
        public boolean isEmpty() {
            synchronized (mutex) {
                if (!ViewEngine.this.manualViewers.isEmpty()) return false;
                final List<List<Player>> auto = ViewEngine.this.autoViewable;
                if (auto != null) {
                    for (List<Player> players : auto) {
                        if (players.isEmpty()) continue;
                        for (Player player : players) {
                            if (autoViewPredicate.test(player)) return false;
                        }
                    }
                }
                return true;
            }
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Player player)) return false;
            synchronized (mutex) {
                if (setContain(ViewEngine.this.manualViewers, player)) return true;
                // Auto
                final List<List<Player>> auto = ViewEngine.this.autoViewable;
                if (auto != null) {
                    for (List<Player> players : auto) {
                        if (!players.isEmpty() && players.contains(player))
                            return autoViewPredicate.test(player);
                    }
                }
            }
            return false;
        }

        @Override
        public void forEach(Consumer<? super Player> action) {
            synchronized (mutex) {
                // Manual viewers
                ViewEngine.this.manualViewers.forEach(action);
                // Auto
                final List<List<Player>> auto = ViewEngine.this.autoViewable;
                if (auto != null) {
                    for (List<Player> players : auto) {
                        if (players.isEmpty()) continue;
                        for (Player player : players) {
                            if (autoViewPredicate.test(player)) action.accept(player);
                        }
                    }
                }
            }
        }

        final class It implements Iterator<Player> {
            private Iterator<Player> current = ViewEngine.this.manualViewers.iterator();
            private boolean autoIterator = false; // True if the current iterator comes from the auto-viewable references
            private int index;
            private Player next;

            @Override
            public boolean hasNext() {
                synchronized (mutex) {
                    return next != null || (next = findNext()) != null;
                }
            }

            @Override
            public Player next() {
                synchronized (mutex) {
                    if (next == null) return findNext();
                    final Player temp = this.next;
                    this.next = null;
                    return temp;
                }
            }

            private Player findNext() {
                Player result;
                if ((result = nextValidEntry(current)) != null) return result;
                this.autoIterator = true;
                if (autoViewable == null) return null;
                for (int i = index + 1; i < autoViewable.size(); i++) {
                    final List<Player> players = autoViewable.get(i);
                    Iterator<Player> iterator = players.iterator();
                    if ((result = nextValidEntry(iterator)) != null) {
                        this.current = iterator;
                        this.index = i;
                        return result;
                    }
                }
                return null;
            }

            private Player nextValidEntry(Iterator<Player> iterator) {
                while (iterator.hasNext()) {
                    final Player player = iterator.next();
                    if (autoIterator ? autoViewPredicate.test(player) : player != entity) return player;
                }
                return null;
            }
        }
    }

    private static boolean setContain(Set<?> set, Player player) {
        return !set.isEmpty() && set.contains(player);
    }
}
