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
     * References of all the player lists surrounding the entity.
     * <p>
     * Used as an efficient way to represent visible entities
     * without continuously updating an internal collection.
     */
    private List<List<Player>> autoViewableReferences;

    private final Consumer<Entity> addition;
    private final Consumer<Entity> removal;
    private Predicate<Player> autoPredicate = p -> true;
    private boolean autoViewable = true;

    private final Set<Player> set = new SetImpl();
    private final Object mutex = this;

    public ViewEngine(@Nullable Entity entity, Consumer<Entity> addition, Consumer<Entity> removal) {
        this.entity = entity;
        this.addition = addition;
        this.removal = removal;
    }

    public ViewEngine() {
        this(null, null, null);
    }

    public void updateReferences(@Nullable List<List<Player>> references) {
        synchronized (mutex) {
            this.autoViewableReferences = references;
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
            return entity != null && autoViewable &&
                    manualViewers.isEmpty() &&
                    exceptionViewersMap.isEmpty();
        }
    }

    public void computeValidAutoViewer(@NotNull Entity entity, Runnable runnable) {
        // Ensure that an entity can be auto-viewed
        synchronized (mutex) {
            if (!(entity instanceof Player player) || isAutoViewable(player)) {
                runnable.run();
            }
        }
    }

    public void updateRule(@NotNull Predicate<Player> predicate) {
        if (removal == null && addition == null)
            throw new IllegalArgumentException("This viewable element does not support auto addition/removal");
        synchronized (mutex) {
            if (autoViewableReferences == null) return;
            for (List<Player> players : autoViewableReferences) {
                if (players.isEmpty()) continue;
                for (Player player : players) {
                    if (!isPotentialAutoViewable(player)) continue;
                    final boolean upd = predicate.test(player);
                    if (autoPredicate.test(player) != upd) {
                        if (upd && addition != null) addition.accept(player);
                        if (!upd && removal != null) removal.accept(player);
                    }
                }
            }
            this.autoPredicate = predicate;
        }
    }

    public boolean isAutoViewable() {
        synchronized (mutex) {
            return autoViewable;
        }
    }

    public void setAutoViewable(boolean autoViewable) {
        synchronized (mutex) {
            final boolean prev = this.autoViewable;
            this.autoViewable = autoViewable;
            if (prev != autoViewable && entity instanceof Player player) {
                // View state changed, either add or remove all auto-viewers
                if (autoViewable) {
                    forAutoViewers(p -> p.updateNewViewer(player));
                } else {
                    forAutoViewers(p -> p.updateOldViewer(player));
                }
            }
        }
    }

    public void forAutoViewers(Consumer<Player> consumer) {
        synchronized (mutex) {
            if (autoViewableReferences == null) return;
            for (List<Player> players : autoViewableReferences) {
                if (players.isEmpty()) continue;
                for (Player player : players) {
                    if (isAutoViewable(player)) consumer.accept(player);
                }
            }
        }
    }

    private boolean isPotentialAutoViewable(Player player) {
        return player != entity && player.isAutoViewable() &&
                !setContain(exceptionViewersMap, player) &&
                !setContain(manualViewers, player);
    }

    private boolean isAutoViewable(Player player) {
        if (!isPotentialAutoViewable(player)) return false;
        return autoPredicate == null || autoPredicate.test(player);
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
                final List<List<Player>> auto = ViewEngine.this.autoViewableReferences;
                if (auto != null) {
                    for (List<Player> players : auto) {
                        if (players.isEmpty()) continue;
                        for (Player player : players) {
                            if (isAutoViewable(player)) size++;
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
                final List<List<Player>> auto = ViewEngine.this.autoViewableReferences;
                if (auto != null) {
                    for (List<Player> players : auto) {
                        if (players.isEmpty()) continue;
                        for (Player player : players) {
                            if (isAutoViewable(player)) return false;
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
                final List<List<Player>> auto = ViewEngine.this.autoViewableReferences;
                if (auto != null) {
                    for (List<Player> players : auto) {
                        if (!players.isEmpty() && players.contains(player))
                            return isAutoViewable(player);
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
                final List<List<Player>> auto = ViewEngine.this.autoViewableReferences;
                if (auto != null) {
                    for (List<Player> players : auto) {
                        if (players.isEmpty()) continue;
                        for (Player player : players) {
                            if (isAutoViewable(player)) action.accept(player);
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
                if (autoViewableReferences == null) return null;
                for (int i = index + 1; i < autoViewableReferences.size(); i++) {
                    final List<Player> players = autoViewableReferences.get(i);
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
                    if (autoIterator ? isAutoViewable(player) : player != entity) return player;
                }
                return null;
            }
        }
    }

    private static boolean setContain(Set<?> set, Player player) {
        return !set.isEmpty() && set.contains(player);
    }
}
