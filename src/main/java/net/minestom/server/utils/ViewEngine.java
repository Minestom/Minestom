package net.minestom.server.utils;

import net.minestom.server.Viewable;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
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

    // Decide if this entity should be viewable to X players
    private final AtomicBoolean autoViewable = new AtomicBoolean(true);
    private List<List<Player>> autoViewableReferences;
    private final Consumer<Player> autoViewableAddition, autoViewableRemoval;
    private Predicate<Player> autoViewablePredicate = p -> true;
    // Decide if this entity should view X entities
    private final AtomicBoolean autoViewer = new AtomicBoolean(true);
    private List<List<Entity>> autoViewerReferences;
    private final Consumer<Entity> autoViewerAddition, autoViewerRemoval;
    private Predicate<Entity> autoViewerPredicate = p -> true;

    private final Set<Player> set = new SetImpl();
    private final Object mutex = this;

    public ViewEngine(@Nullable Entity entity,
                      Consumer<Player> autoViewableAddition, Consumer<Player> autoViewableRemoval,
                      Consumer<Entity> autoViewerAddition, Consumer<Entity> autoViewerRemoval) {
        this.entity = entity;
        this.autoViewableAddition = autoViewableAddition;
        this.autoViewableRemoval = autoViewableRemoval;
        this.autoViewerAddition = autoViewerAddition;
        this.autoViewerRemoval = autoViewerRemoval;
    }

    public ViewEngine() {
        this(null, null, null, null, null);
    }

    public void updateReferences(@Nullable List<List<Entity>> entitiesRef,
                                 @Nullable List<List<Player>> playersRef) {
        synchronized (mutex) {
            this.autoViewableReferences = playersRef;
            this.autoViewerReferences = entitiesRef;
        }
    }

    public boolean manualAdd(@NotNull Player player) {
        // Manual viewer addition into the manual set
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
            return entity != null && isAutoViewable() &&
                    manualViewers.isEmpty() &&
                    exceptionViewersMap.isEmpty();
        }
    }

    public void handleAutoViewAddition(Entity entity) {
        handleAutoView(entity, autoViewerAddition, autoViewableAddition);
    }

    public void handleAutoViewRemoval(Entity entity) {
        handleAutoView(entity, autoViewerRemoval, autoViewableRemoval);
    }

    private void handleAutoView(Entity entity, Consumer<Entity> viewer, Consumer<Player> viewable) {
        if (this.entity instanceof Player && isAutoViewer()) {
            viewer.accept(entity); // Send packet to this player
        }
        if (entity instanceof Player player && isAutoViewable()) {
            viewable.accept(player); // Send packet to the range-visible player
        }
    }

    public boolean isAutoViewable() {
        return autoViewable.get();
    }

    public void setAutoViewable(boolean autoViewable) {
        final boolean previous = this.autoViewable.getAndSet(autoViewable);
        if (previous != autoViewable) {
            // View state changed, either add or remove itself from all auto-viewers
            updateReferences(autoViewableReferences, autoViewable,
                    autoViewableAddition, autoViewableRemoval);
        }
    }

    public void updateViewableRule(@NotNull Predicate<Player> predicate) {
        if (autoViewableRemoval == null && autoViewableAddition == null)
            throw new IllegalArgumentException("This viewable element does not support auto addition/removal");
        synchronized (mutex) {
            updateReferencesRule(autoViewableReferences, this::isPotentialAutoViewable,
                    autoViewablePredicate, predicate,
                    autoViewableAddition, autoViewableRemoval);
            this.autoViewablePredicate = predicate;
        }
    }

    public boolean isAutoViewer() {
        return autoViewer.get();
    }

    public void setAutoViewer(boolean autoViewer) {
        final boolean previous = this.autoViewer.getAndSet(autoViewer);
        if (previous != autoViewer) {
            // View state changed, either add or remove all auto-viewers
            updateReferences(autoViewerReferences, autoViewer,
                    autoViewerAddition, autoViewerRemoval);
        }
    }

    public void updateViewerRule(@NotNull Predicate<Entity> predicate) {
        if (autoViewableRemoval == null && autoViewableAddition == null)
            throw new IllegalArgumentException("This viewable element does not support auto addition/removal");
        synchronized (mutex) {
            updateReferencesRule(autoViewerReferences, this::isPotentialAutoViewer,
                    autoViewerPredicate, predicate,
                    autoViewerAddition, autoViewerRemoval);
            this.autoViewerPredicate = predicate;
        }
    }

    private <T extends Entity> void updateReferences(List<List<T>> references,
                                                     boolean newValue,
                                                     Consumer<T> addition, Consumer<T> removal) {
        synchronized (mutex) {
            if (references == null) return;
            for (List<T> entities : references) {
                if (entities.isEmpty()) continue;
                for (T entity : entities) {
                    if (canSee(entity)) {
                        if (newValue) {
                            addition.accept(entity);
                        } else {
                            removal.accept(entity);
                        }
                    }
                }
            }
        }
    }

    private <T extends Entity> void updateReferencesRule(List<List<T>> references,
                                                         Predicate<T> preliminaryTest,
                                                         Predicate<T> oldPredicate, Predicate<T> newPredicate,
                                                         Consumer<T> addition, Consumer<T> removal) {
        if (references == null) return;
        for (List<T> entities : references) {
            if (entities.isEmpty()) continue;
            for (T entity : entities) {
                if (!preliminaryTest.test(entity)) continue;
                final boolean upd = newPredicate.test(entity);
                if (oldPredicate.test(entity) != upd) {
                    if (upd && addition != null) addition.accept(entity);
                    if (!upd && removal != null) removal.accept(entity);
                }
            }
        }
    }

    private boolean isVisibleBy(Player player) {
        // Check if the player is currently visible by this
        if (!isPotentialAutoViewable(player)) return false;
        return player.isAutoViewer() && autoViewablePredicate.test(player);
    }

    private boolean canSee(Entity entity) {
        if (entity == this.entity ||
                entity instanceof Player player && setContain(manualViewers, player)) return false;
        return autoViewerPredicate.test(entity);
    }

    private boolean isPotentialAutoViewable(Player player) {
        return player != entity && player.isAutoViewer() &&
                !setContain(exceptionViewersMap, player) &&
                !setContain(manualViewers, player);
    }

    private boolean isPotentialAutoViewer(Entity entity) {
        return entity != this.entity && entity.isAutoViewable() &&
                (!(entity instanceof Player player) ||
                        !setContain(exceptionViewersMap, player) &&
                                !setContain(manualViewers, player));
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
                            if (isVisibleBy(player)) size++;
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
                            if (isVisibleBy(player)) return false;
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
                            return isVisibleBy(player);
                    }
                }
            }
            return false;
        }

        @Override
        public void forEach(Consumer<? super Player> action) {
            synchronized (mutex) {
                // Manual viewers
                final Set<Player> manual = ViewEngine.this.manualViewers;
                if (!manual.isEmpty()) manual.forEach(action);
                // Auto
                final List<List<Player>> auto = ViewEngine.this.autoViewableReferences;
                if (auto != null) {
                    for (List<Player> players : auto) {
                        if (players.isEmpty()) continue;
                        for (Player player : players) {
                            if (isVisibleBy(player)) action.accept(player);
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
                    if (autoIterator ? isVisibleBy(player) : player != entity) return player;
                }
                return null;
            }
        }
    }

    private static boolean setContain(Set<?> set, Player player) {
        return !set.isEmpty() && set.contains(player);
    }
}
