package net.minestom.server.utils;

import com.zaxxer.sparsebits.SparseBitSet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.EntityTracker;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Defines which players are able to see this element.
 */
@ApiStatus.Internal
public final class ViewEngine {
    private final Entity entity;
    private final ObjectArraySet<Player> manualViewers = new ObjectArraySet<>();

    private EntityTracker tracker;

    // Decide if this entity should be viewable to X players
    private final AtomicBoolean autoViewable = new AtomicBoolean(true);
    private final SparseBitSet autoViewableBitSet = new SparseBitSet();
    private List<List<Player>> autoViewableReferences;
    private final Consumer<Player> autoViewableAddition, autoViewableRemoval;
    // Decide if this entity should view X entities
    private final AtomicBoolean autoViewer = new AtomicBoolean(true);
    private final SparseBitSet autoViewerBitSet = new SparseBitSet();
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

    public void updateTracker(@NotNull Point point, @Nullable EntityTracker tracker) {
        synchronized (mutex) {
            this.tracker = tracker;
            if (tracker != null) {
                this.autoViewableReferences = tracker.references(point, EntityTracker.Target.PLAYERS);
                this.autoViewerReferences = tracker.references(point, EntityTracker.Target.ENTITIES);
            } else {
                this.autoViewableReferences = null;
                this.autoViewerReferences = null;
            }
        }
    }

    public boolean manualAdd(@NotNull Player player) {
        if (player == this.entity) return false;
        synchronized (mutex) {
            return !manualViewers.add(player);
        }
    }

    public boolean manualRemove(@NotNull Player player) {
        if (player == this.entity) return false;
        synchronized (mutex) {
            return !manualViewers.remove(player);
        }
    }

    public boolean hasPredictableViewers() {
        // Verify if this entity's viewers can be predicted from surrounding entities
        synchronized (mutex) {
            return isAutoViewable() && manualViewers.isEmpty();
        }
    }

    public void registerViewable(Player player) {
        this.autoViewableBitSet.set(player.getEntityId());
    }

    public void unregisterViewable(Player player) {
        this.autoViewableBitSet.clear(player.getEntityId());
    }

    public void registerViewer(Entity entity) {
        this.autoViewerBitSet.set(entity.getEntityId());
    }

    public void unregisterViewer(Entity entity) {
        this.autoViewerBitSet.clear(entity.getEntityId());
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
        if (this.entity instanceof Player && isAutoViewer() && entity.isAutoViewable()) {
            viewer.accept(entity); // Send packet to this player
        }
        if (entity instanceof Player player && player.autoViewEntities() && isAutoViewable()) {
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
            synchronized (mutex) {
                if (tracker == null || autoViewableReferences == null) return;
                Predicate<Player> predicate = autoViewable ? Entity::autoViewEntities : player -> autoViewableBitSet.get(player.getEntityId());
                Consumer<Player> action = autoViewable ? autoViewableAddition : autoViewableRemoval;
                update(autoViewableReferences, predicate, action);
            }
        }
    }

    public void setAutoViewer(boolean autoViewer) {
        final boolean previous = this.autoViewer.getAndSet(autoViewer);
        if (previous != autoViewer) {
            // View state changed, either add or remove all surrounding entities
            synchronized (mutex) {
                if (tracker == null || autoViewerReferences == null) return;
                Predicate<Entity> predicate = autoViewer ? Entity::isAutoViewable : ent -> autoViewerBitSet.get(ent.getEntityId());
                Consumer<Entity> action = autoViewer ? autoViewerAddition : autoViewerRemoval;
                update(autoViewerReferences, predicate, action);
            }
        }
    }

    private <T extends Entity> void update(List<List<T>> references,
                                           Predicate<T> visibilityPredicate,
                                           Consumer<T> action) {
        this.tracker.synchronize(() -> {
            for (List<T> entities : references) {
                if (entities.isEmpty()) continue;
                for (T entity : entities) {
                    if (entity == this.entity || !visibilityPredicate.test(entity)) continue;
                    if (entity instanceof Player player && manualViewers.contains(player)) continue;
                    if (entity.getVehicle() != null) continue;
                    action.accept(entity);
                }
            }
        });
    }

    private boolean validAutoViewer(Player player) {
        return entity == null || autoViewableBitSet.get(player.getEntityId());
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
                return manualViewers.size() + autoViewableBitSet.size();
            }
        }

        @Override
        public boolean isEmpty() {
            synchronized (mutex) {
                return manualViewers.isEmpty() && autoViewableBitSet.isEmpty();
            }
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Player player)) return false;
            synchronized (mutex) {
                return manualViewers.contains(player) || autoViewableBitSet.get(player.getEntityId());
            }
        }

        @Override
        public void forEach(Consumer<? super Player> action) {
            synchronized (mutex) {
                // Manual viewers
                if (!manualViewers.isEmpty()) manualViewers.forEach(action);
                // Auto
                final List<List<Player>> auto = ViewEngine.this.autoViewableReferences;
                if (auto != null && isAutoViewable()) {
                    for (List<Player> players : auto) {
                        if (players.isEmpty()) continue;
                        for (Player player : players) {
                            if (validAutoViewer(player)) action.accept(player);
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
                if (autoViewableReferences == null || !isAutoViewable()) return null;
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
                    if (autoIterator ? validAutoViewer(player) : player != entity)
                        return player;
                }
                return null;
            }
        }
    }
}
