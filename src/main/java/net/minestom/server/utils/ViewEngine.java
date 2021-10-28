package net.minestom.server.utils;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
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
     * Represents manual viewers.
     * `True` means that the player shall always be seen (put using {@link Viewable#addViewer(Player)}).
     * `False` means that the player shall never be viewed (put using {@link Viewable#removeViewer(Player)}).
     */
    private final Object2BooleanOpenHashMap<Player> manualMap = new Object2BooleanOpenHashMap<>();

    // Decide if this entity should be viewable to X players
    private final AtomicBoolean autoViewable = new AtomicBoolean(true);
    private List<List<Player>> autoViewableReferences;
    private final Consumer<Player> autoViewableAddition, autoViewableRemoval;
    // Decide if this entity should view X entities
    private final AtomicBoolean autoViewer = new AtomicBoolean(true);
    private List<List<Entity>> autoViewerReferences;
    private final Consumer<Entity> autoViewerAddition, autoViewerRemoval;

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
            return !manualMap.put(player, true);
        }
    }

    public boolean manualRemove(@NotNull Player player) {
        synchronized (mutex) {
            // Add exception
            if (!manualMap.containsKey(player)) {
                manualMap.put(player, false);
                return true;
            }
            // Remove from manual view
            return !manualMap.put(player, true);
        }
    }

    public boolean hasPredictableViewers() {
        // Verify if this entity's viewers can be predicted from surrounding entities
        // This method should be considered as a hint instead of the objective truth
        // as the manual map is not iterated.
        return entity != null && isAutoViewable() && manualMap.isEmpty();
    }

    public void handleAutoViewAddition(Entity entity) {
        handleAutoView(entity, autoViewerAddition, autoViewableAddition);
    }

    public void handleAutoViewRemoval(Entity entity) {
        handleAutoView(entity, autoViewerRemoval, autoViewableRemoval);
    }

    private void handleAutoView(Entity entity, Consumer<Entity> viewer, Consumer<Player> viewable) {
        if (this.entity == entity)
            return; // Ensure that self isn't added or removed as viewer
        if (entity.getVehicle() != null)
            return; // Passengers are handled by the vehicle, inheriting its viewing settings
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

    public boolean isAutoViewer() {
        return autoViewer.get();
    }

    public void setAutoViewable(boolean autoViewable) {
        final boolean previous = this.autoViewable.getAndSet(autoViewable);
        if (previous != autoViewable) {
            // View state changed, either add or remove itself from surrounding players
            updateReferences(autoViewableReferences, autoViewable, Entity::isAutoViewer,
                    autoViewableAddition, autoViewableRemoval);
        }
    }

    public void setAutoViewer(boolean autoViewer) {
        final boolean previous = this.autoViewer.getAndSet(autoViewer);
        if (previous != autoViewer) {
            // View state changed, either add or remove all surrounding entities
            updateReferences(autoViewerReferences, autoViewer, Entity::isAutoViewable,
                    autoViewerAddition, autoViewerRemoval);
        }
    }

    private <T extends Entity> void updateReferences(List<List<T>> references, boolean newValue,
                                                     Predicate<T> visibilityPredicate,
                                                     Consumer<T> addition, Consumer<T> removal) {
        synchronized (mutex) {
            if (references == null) return;
            for (List<T> entities : references) {
                if (entities.isEmpty()) continue;
                for (T entity : entities) {
                    if (entity.getVehicle() != null ||
                            !ensureAuto(entity) ||
                            !visibilityPredicate.test(entity)) continue;
                    if (newValue) {
                        addition.accept(entity);
                    } else {
                        removal.accept(entity);
                    }
                }
            }
        }
    }

    private boolean isVisibleByAuto(Player player) {
        // Check if the close player is currently visible by this
        if (!ensureAuto(player)) return false;
        return player.isAutoViewer();
    }

    private boolean ensureAuto(Entity entity) {
        return entity != this.entity &&
                (!(entity instanceof Player player) || !manualMap.getBoolean(player));
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
                int size = 0;
                // Manual size
                for (boolean isManualViewer : manualMap.values()) {
                    if (isManualViewer) size++;
                }
                // Auto
                final List<List<Player>> auto = ViewEngine.this.autoViewableReferences;
                if (auto != null) {
                    for (List<Player> players : auto) {
                        if (players.isEmpty()) continue;
                        for (Player player : players) {
                            if (isVisibleByAuto(player)) size++;
                        }
                    }
                }
                return size;
            }
        }

        @Override
        public boolean isEmpty() {
            synchronized (mutex) {
                if (ViewEngine.this.manualMap.containsValue(true)) return false;
                final List<List<Player>> auto = ViewEngine.this.autoViewableReferences;
                if (auto != null) {
                    for (List<Player> players : auto) {
                        if (players.isEmpty()) continue;
                        for (Player player : players) {
                            if (isVisibleByAuto(player)) return false;
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
                if (ViewEngine.this.manualMap.getBoolean(player)) return true;
                // Auto
                final List<List<Player>> auto = ViewEngine.this.autoViewableReferences;
                if (auto != null) {
                    for (List<Player> players : auto) {
                        if (!players.isEmpty() && players.contains(player))
                            return isVisibleByAuto(player);
                    }
                }
            }
            return false;
        }

        @Override
        public void forEach(Consumer<? super Player> action) {
            synchronized (mutex) {
                // Manual viewers
                if (!manualMap.isEmpty()) {
                    for (var entry : manualMap.object2BooleanEntrySet()) {
                        if (entry.getBooleanValue()) action.accept(entry.getKey());
                    }
                }
                // Auto
                final List<List<Player>> auto = ViewEngine.this.autoViewableReferences;
                if (auto != null) {
                    for (List<Player> players : auto) {
                        if (players.isEmpty()) continue;
                        for (Player player : players) {
                            if (isVisibleByAuto(player)) action.accept(player);
                        }
                    }
                }
            }
        }

        final class It implements Iterator<Player> {
            private Iterator<Player> current = ViewEngine.this.manualMap.object2BooleanEntrySet().stream()
                    .filter(Object2BooleanMap.Entry::getBooleanValue).map(Map.Entry::getKey).iterator();
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
                    if (autoIterator ? isVisibleByAuto(player) : player != entity) return player;
                }
                return null;
            }
        }
    }
}
