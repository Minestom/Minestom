package net.minestom.server.utils;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Defines which players are able to see this element.
 */
@ApiStatus.Internal
public final class ViewEngine {
    private final Entity entity;
    private final int range;
    private final Set<Player> manualViewers = new HashSet<>();

    private Instance instance;
    private EntityTracker tracker;
    private Point lastPoint;

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
        this.range = entity != null ? MinecraftServer.getEntityViewDistance() : MinecraftServer.getChunkViewDistance();
        this.viewableOption = new Option<>(EntityTracker.Target.PLAYERS, Entity::autoViewEntities, autoViewableAddition, autoViewableRemoval);
        this.viewerOption = new Option<>(EntityTracker.Target.ENTITIES, Entity::isAutoViewable, autoViewerAddition, autoViewerRemoval);
    }

    public ViewEngine(@Nullable Entity entity) {
        this(entity, null, null, null, null);
    }

    public ViewEngine() {
        this(null);
    }

    public void updateTracker(@NotNull Instance instance, @NotNull Point point, @Nullable EntityTracker tracker) {
        synchronized (mutex) {
            this.instance = instance;
            this.lastPoint = point;
            this.tracker = tracker;
        }
    }

    public boolean manualAdd(@NotNull Player player) {
        if (player == this.entity) return false;
        synchronized (mutex) {
            return manualViewers.add(player);
        }
    }

    public boolean manualRemove(@NotNull Player player) {
        if (player == this.entity) return false;
        synchronized (mutex) {
            return manualViewers.remove(player);
        }
    }

    public void forManuals(@NotNull Consumer<Player> consumer) {
        synchronized (mutex) {
            this.manualViewers.forEach(consumer);
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
        if (entity.getVehicle() != null)
            return; // Passengers are handled by the vehicle, inheriting its viewing settings
        if (this.entity instanceof Player && viewerOption.isAuto() && entity.isAutoViewable()) {
            if (viewer != null) viewer.accept(entity); // Send packet to this player
        }
        if (entity instanceof Player player && player.autoViewEntities() && viewableOption.isAuto()) {
            if (viewable != null) viewable.accept(player); // Send packet to the range-visible player
        }
    }

    private boolean validAutoViewer(Player player) {
        return entity == null || viewableOption.isRegistered(player);
    }

    public Object mutex() {
        return mutex;
    }

    public Set<Player> asSet() {
        return set;
    }

    public final class Option<T extends Entity> {
        @SuppressWarnings("rawtypes")
        private static final AtomicIntegerFieldUpdater<Option> UPDATER = AtomicIntegerFieldUpdater.newUpdater(Option.class, "auto");
        // Entities that should be tracked from this option
        private final EntityTracker.Target<T> target;
        // The condition that must be met for this option to be considered auto.
        private final Predicate<T> loopPredicate;
        // The consumers to be called when an entity is added/removed.
        public final Consumer<T> addition, removal;
        // Contains all the auto-entity ids that are viewable by this option.
        public final IntSet bitSet = new IntOpenHashSet();
        // 1 if auto, 0 if manual
        private volatile int auto = 1;
        // The custom rule used to determine if an entity is viewable.
        private Predicate<T> predicate = entity -> true;

        public Option(EntityTracker.Target<T> target, Predicate<T> loopPredicate,
                      Consumer<T> addition, Consumer<T> removal) {
            this.target = target;
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
            return bitSet.contains(entity.getEntityId());
        }

        public void register(T entity) {
            this.bitSet.add(entity.getEntityId());
        }

        public void unregister(T entity) {
            this.bitSet.remove(entity.getEntityId());
        }

        public void updateAuto(boolean autoViewable) {
            final boolean previous = UPDATER.getAndSet(this, autoViewable ? 1 : 0) == 1;
            if (previous != autoViewable) {
                synchronized (mutex) {
                    if (autoViewable) update(loopPredicate, addition);
                    else update(this::isRegistered, removal);
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
                update(loopPredicate, entity -> {
                    final boolean result = predicate.test(entity);
                    if (result != isRegistered(entity)) {
                        if (result) addition.accept(entity);
                        else removal.accept(entity);
                    }
                });
            }
        }

        private void update(Predicate<T> visibilityPredicate,
                            Consumer<T> action) {
            references().forEach(entity -> {
                if (entity == ViewEngine.this.entity || !visibilityPredicate.test(entity)) return;
                if (entity instanceof Player player && manualViewers.contains(player)) return;
                if (entity.getVehicle() != null) return;
                action.accept(entity);
            });
        }

        private Stream<T> references() {
            if (tracker == null) return Stream.empty();
            var references = tracker.references(lastPoint, range, target);
            var result = references.stream().flatMap(Collection::stream);
            if (instance != null && instance instanceof InstanceContainer container) {
                // References from shared instances must be added to the result.
                var shared = container.getSharedInstances();
                if (!shared.isEmpty()) {
                    var tmp = shared.stream().<List<T>>mapMulti((inst, consumer) -> {
                        var ref = inst.getEntityTracker().references(lastPoint, range, target);
                        ref.forEach(consumer);
                    }).flatMap(Collection::stream);
                    result = Stream.concat(result, tmp);
                }
            }
            return result;
        }
    }

    final class SetImpl extends AbstractSet<Player> {
        private static final Object[] EMPTY = new Object[0];

        @Override
        public @NotNull Iterator<Player> iterator() {
            synchronized (mutex) {
                return Arrays.asList(toArray(Player[]::new)).iterator();
            }
        }

        @Override
        public int size() {
            synchronized (mutex) {
                int size = manualViewers.size();
                if (entity != null) return size + viewableOption.bitSet.size();
                // Non-entity fallback
                size += viewableOption.references().filter(ViewEngine.this::validAutoViewer).count();
                return size;
            }
        }

        @Override
        public boolean isEmpty() {
            synchronized (mutex) {
                if (!manualViewers.isEmpty()) return false;
                if (entity != null) return viewableOption.bitSet.isEmpty();
                // Non-entity fallback
                return viewableOption.references().noneMatch(ViewEngine.this::validAutoViewer);
            }
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Player player)) return false;
            synchronized (mutex) {
                if (manualViewers.contains(player)) return true;
                if (entity != null) return viewableOption.isRegistered(player);
                // Non-entity fallback
                return viewableOption.references().anyMatch(ViewEngine.this::validAutoViewer);
            }
        }

        @Override
        public void forEach(Consumer<? super Player> action) {
            synchronized (mutex) {
                if (!manualViewers.isEmpty()) manualViewers.forEach(action);
                if (entity != null) {
                    viewableOption.bitSet.forEach((int id) ->
                            action.accept((Player) Entity.getEntity(id)));
                    return;
                }
                // Non-entity fallback
                viewableOption.references().filter(ViewEngine.this::validAutoViewer).forEach(action);
            }
        }

        @Override
        public @NotNull Object @NotNull [] toArray() {
            synchronized (mutex) {
                final int size = size();
                if (size == 0) return EMPTY;
                Object[] array = new Object[size];
                AtomicInteger index = new AtomicInteger();
                forEach(player -> array[index.getAndIncrement()] = player);
                assert index.get() == size;
                return array;
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> @NotNull T @NotNull [] toArray(@NotNull T @NotNull [] a) {
            synchronized (mutex) {
                final int size = size();
                T[] array = a.length >= size ? a :
                        (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);

                AtomicInteger index = new AtomicInteger();
                forEach(player -> array[index.getAndIncrement()] = (T) player);
                assert index.get() == size;
                return array;
            }
        }
    }
}
