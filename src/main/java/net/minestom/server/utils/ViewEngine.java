package net.minestom.server.utils;

import net.minestom.server.Viewable;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
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
        this.autoViewPredicate = player -> {
            if (player == entity) return false;
            return !setContain(exceptionViewersMap, player);
        };
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

    public boolean ensureAutoViewer(@NotNull Entity entity) {
        if (!(entity instanceof Player)) return true;
        // Ensure that an entity can be auto-viewed
        // In this case, it should be neither in the manual nor exception map
        synchronized (mutex) {
            if (setContain(manualViewers, entity)) return false;
            return autoViewPredicate.test((Player) entity);
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
        public boolean contains(Object o) {
            synchronized (mutex) {
                if (setContain(ViewEngine.this.manualViewers, o)) return true;
                // Auto
                final List<List<Player>> auto = ViewEngine.this.autoViewable;
                if (auto != null) {
                    for (List<Player> players : auto) {
                        if (players.isEmpty()) continue;
                        return players.contains(o) && autoViewPredicate.test((Player) o);
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

            private Player getMaybeNext() {
                if(current.hasNext()) return current.next();
                this.current = nextIterator();
                if(current != null && current.hasNext()) return current.next();
                return null;
            }

            @Override
            @Contract(mutates = "this")
            public boolean hasNext() {
                synchronized (mutex) {
                    if (next == null) {
                        do {
                            next = getMaybeNext();
                            if (next == null) return false;
                        } while (autoIterator ? !ViewEngine.this.autoViewPredicate.test(next) : next == entity);
                    }
                }
                return true;
            }

            @Override
            public Player next() {
                synchronized (mutex) {
                    final Player value = this.next;
                    if (value == null && hasNext()) return this.next;
                    this.next = null;
                    return value;
                }
            }

            private Iterator<Player> nextIterator() {
                final List<List<Player>> auto = autoViewable;
                if (auto == null) return null;
                while (true) {
                    final int updated = index++;
                    if (updated >= auto.size()) return null;
                    final List<Player> players = auto.get(updated);
                    if (players.isEmpty()) continue;
                    this.autoIterator = true;
                    return players.iterator();
                }
            }
        }
    }

    private static boolean setContain(Set<?> set, Object object) {
        return !set.isEmpty() && set.contains(object);
    }
}
