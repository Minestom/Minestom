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

    public void registerViewable(Player player) {
        this.viewableOption.bitSet.set(player.getEntityId());
    }

    public void unregisterViewable(Player player) {
        this.viewableOption.bitSet.clear(player.getEntityId());
    }

    public void registerViewer(Entity entity) {
        this.viewerOption.bitSet.set(entity.getEntityId());
    }

    public void unregisterViewer(Entity entity) {
        this.viewerOption.bitSet.clear(entity.getEntityId());
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
        return entity == null || viewableOption.bitSet.get(player.getEntityId());
    }

    public Set<Player> asSet() {
        return set;
    }

    public final class Option<T extends Entity> {
        private final Predicate<T> loopPredicate;
        private final AtomicBoolean auto = new AtomicBoolean(true);
        private final SparseBitSet bitSet = new SparseBitSet();
        private List<List<T>> references;
        public final Consumer<T> addition, removal;
        private Predicate<T> predicate = entity -> true;

        public Option(Predicate<T> loopPredicate,
                      Consumer<T> addition, Consumer<T> removal) {
            this.loopPredicate = loopPredicate;
            this.addition = addition;
            this.removal = removal;
        }

        public boolean isAuto() {
            return auto.get();
        }

        public boolean predicate(T entity) {
            return predicate.test(entity);
        }

        public void updateAuto(boolean autoViewable) {
            final boolean previous = auto.getAndSet(autoViewable);
            if (previous != autoViewable) {
                // View state changed, either add or remove itself from surrounding players
                synchronized (mutex) {
                    Predicate<T> predicate = autoViewable ? loopPredicate : player -> bitSet.get(player.getEntityId());
                    Consumer<T> action = autoViewable ? addition : removal;
                    update(references, predicate, action);
                }
            }
        }

        public void updateRule(Predicate<T> predicate) {
            synchronized (mutex) {
                this.predicate = predicate;
                update(references, loopPredicate, entity -> {
                    boolean result = predicate.test(entity);
                    boolean contains = bitSet.get(entity.getEntityId());
                    if (result && !contains) {
                        addition.accept(entity);
                    } else if (!result && contains) {
                        removal.accept(entity);
                    }
                });
            }
        }

        private void update(List<List<T>> references,
                            Predicate<T> visibilityPredicate,
                            Consumer<T> action) {
            if (tracker == null || references == null) return;
            tracker.synchronize(() -> {
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
                return manualViewers.size() + viewableOption.bitSet.size();
            }
        }

        @Override
        public boolean isEmpty() {
            synchronized (mutex) {
                return manualViewers.isEmpty() && viewableOption.bitSet.isEmpty();
            }
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Player player)) return false;
            synchronized (mutex) {
                return manualViewers.contains(player) || viewableOption.bitSet.get(player.getEntityId());
            }
        }

        @Override
        public void forEach(Consumer<? super Player> action) {
            synchronized (mutex) {
                // Manual viewers
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
