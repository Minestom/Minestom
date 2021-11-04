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
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
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
    private Point lastTrackingPoint;

    // Decide if this entity should be viewable to X players
    public final Option<Player> viewableOption;
    // Decide if this entity should view X entities
    public final Option<Entity> viewerOption;

    private final Set<Player> set = new SetImpl();
    private final Object mutex = this;

    public ViewEngine(@Nullable Entity entity,
                      Consumer<Player> autoViewableAddition, Consumer<Player> autoViewableRemoval,
                      Consumer<Entity> autoViewerAddition, Consumer<Entity> autoViewerRemoval) {
        this.entity = entity;
        this.viewableOption = new Option<>(Entity::autoViewEntities, autoViewableAddition, autoViewableRemoval);
        this.viewerOption = new Option<>(Entity::isAutoViewable, autoViewerAddition, autoViewerRemoval);
    }

    public ViewEngine() {
        this(null, null, null, null, null);
    }

    public void updateTracker(@NotNull Point point, @Nullable EntityTracker tracker) {
        synchronized (mutex) {
            this.tracker = tracker;
            this.lastTrackingPoint = point;
            if (tracker != null) {
                this.viewableOption.references = tracker.references(point, EntityTracker.Target.PLAYERS);
                this.viewerOption.references = tracker.references(point, EntityTracker.Target.ENTITIES);
            } else {
                this.viewableOption.references = null;
                this.viewerOption.references = null;
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
            return viewableOption.isAuto() && manualViewers.isEmpty();
        }
    }

    public void handleAutoViewAddition(Entity entity) {
        handleAutoView(entity, viewerOption.addition, viewableOption.addition);
    }

    public void handleAutoViewRemoval(Entity entity) {
        handleAutoView(entity, viewerOption.removal, viewableOption.removal);
    }

    private void handleAutoView(Entity entity, Consumer<Entity> viewer, Consumer<Player> viewable) {
        if (this.entity == entity)
            return; // Ensure that self isn't added or removed as viewer
        if (entity.getVehicle() != null)
            return; // Passengers are handled by the vehicle, inheriting its viewing settings
        if (this.entity instanceof Player && viewerOption.isAuto() && entity.isAutoViewable()) {
            viewer.accept(entity); // Send packet to this player
        }
        if (entity instanceof Player player && player.autoViewEntities() && viewableOption.isAuto()) {
            viewable.accept(player); // Send packet to the range-visible player
        }
    }

    private boolean validAutoViewer(Player player) {
        return entity == null || viewableOption.isRegistered(player);
    }

    public Set<Player> asSet() {
        return set;
    }

    public final class Option<T extends Entity> {
        @SuppressWarnings("rawtypes")
        private static final AtomicIntegerFieldUpdater<Option> UPDATER = AtomicIntegerFieldUpdater.newUpdater(Option.class, "auto");
        // The condition that must be met for this option to be considered auto.
        private final Predicate<T> loopPredicate;
        // The consumers to be called when an entity is added/removed.
        public final Consumer<T> addition, removal;
        // Contains all the entity ids that are viewable by this option.
        public final SparseBitSet bitSet = new SparseBitSet();
        // 1 if auto, 0 if manual
        private volatile int auto = 1;
        // References from the entity trackers.
        private List<List<T>> references;
        // The custom rule used to determine if an entity is viewable.
        private Predicate<T> predicate = entity -> true;

        public Option(Predicate<T> loopPredicate,
                      Consumer<T> addition, Consumer<T> removal) {
            this.loopPredicate = loopPredicate;
            this.addition = addition;
            this.removal = removal;
        }

        public boolean isAuto() {
            return auto == 1;
        }

        public boolean predicate(T entity) {
            return predicate.test(entity);
        }

        public boolean isRegistered(T entity) {
            return bitSet.get(entity.getEntityId());
        }

        public void register(T entity) {
            this.bitSet.set(entity.getEntityId());
        }

        public void unregister(T entity) {
            this.bitSet.clear(entity.getEntityId());
        }

        public void updateAuto(boolean autoViewable) {
            final boolean previous = UPDATER.getAndSet(this, autoViewable ? 1 : 0) == 1;
            if (previous != autoViewable) {
                synchronized (mutex) {
                    if (autoViewable) update(references, loopPredicate, addition);
                    else update(references, this::isRegistered, removal);
                }
            }
        }

        public void updateRule(Predicate<T> predicate) {
            synchronized (mutex) {
                this.predicate = predicate;
                updateRule();
            }
        }

        public void updateRule() {
            synchronized (mutex) {
                update(references, loopPredicate, entity -> {
                    final boolean result = predicate.test(entity);
                    if (result != isRegistered(entity)) {
                        if (result) addition.accept(entity);
                        else removal.accept(entity);
                    }
                });
            }
        }

        private void update(List<List<T>> references,
                            Predicate<T> visibilityPredicate,
                            Consumer<T> action) {
            if (tracker == null || references == null) return;
            tracker.synchronize(lastTrackingPoint, () -> {
                for (List<T> entities : references) {
                    if (entities.isEmpty()) continue;
                    for (T entity : entities) {
                        if (entity == ViewEngine.this.entity || !visibilityPredicate.test(entity)) continue;
                        if (entity instanceof Player player && manualViewers.contains(player)) continue;
                        if (entity.getVehicle() != null) continue;
                        action.accept(entity);
                    }
                }
            });
        }
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
                int size = manualViewers.size();
                if (entity != null) return size + viewableOption.bitSet.size();
                // Non-entity fallback
                final List<List<Player>> auto = ViewEngine.this.viewableOption.references;
                if (auto != null) {
                    for (List<Player> players : auto) {
                        if (players.isEmpty()) continue;
                        for (Player player : players) {
                            if (validAutoViewer(player)) size++;
                        }
                    }
                }
                return size;
            }
        }

        @Override
        public boolean isEmpty() {
            synchronized (mutex) {
                if (!manualViewers.isEmpty()) return false;
                if (entity != null) return viewableOption.bitSet.isEmpty();
                // Non-entity fallback
                final List<List<Player>> auto = ViewEngine.this.viewableOption.references;
                if (auto != null) {
                    for (List<Player> players : auto) {
                        if (players.isEmpty()) continue;
                        for (Player player : players) {
                            if (validAutoViewer(player)) return false;
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
                if (manualViewers.contains(player)) return true;
                if (entity != null) return viewableOption.isRegistered(player);
                // Non-entity fallback
                final List<List<Player>> auto = ViewEngine.this.viewableOption.references;
                if (auto != null) {
                    for (List<Player> players : auto) {
                        if (players.isEmpty()) continue;
                        if (players.contains(player) && validAutoViewer(player)) return true;
                    }
                }
                return false;
            }
        }

        @Override
        public void forEach(Consumer<? super Player> action) {
            synchronized (mutex) {
                if (!manualViewers.isEmpty()) manualViewers.forEach(action);
                // Auto
                final List<List<Player>> auto = ViewEngine.this.viewableOption.references;
                if (auto != null && viewableOption.isAuto()) {
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
                final var references = viewableOption.references;
                if (references == null || !viewableOption.isAuto()) return null;
                for (int i = index + 1; i < references.size(); i++) {
                    final List<Player> players = references.get(i);
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
