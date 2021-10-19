package net.minestom.server.utils;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

@ApiStatus.Internal
public final class ViewEngine {
    private final Entity entity;
    private final Set<Player> manualViewers = new HashSet<>();
    private List<List<Player>> autoViewable;

    private final Set<Player> set = new SetImpl();
    private final Object mutex = this;

    public ViewEngine(Entity entity) {
        this.entity = entity;
    }

    public synchronized void updateReferences(List<List<Player>> references) {
        this.autoViewable = references;
    }

    public boolean attemptAdd(Player player) {
        synchronized (mutex) {
            return manualViewers.add(player);
        }
    }

    public boolean attemptRemove(Player player) {
        synchronized (mutex) {
            return manualViewers.remove(player);
        }
    }

    public boolean hasPredictableViewers() {
        synchronized (mutex) {
            return entity.isAutoViewable() && manualViewers.isEmpty();
        }
    }

    public boolean ensureAutoViewer(Entity entity) {
        synchronized (mutex) {
            return manualViewers.isEmpty() || manualViewers.contains(entity);
        }
    }

    public Set<Player> asSet() {
        return set;
    }

    final class SetImpl extends AbstractSet<Player> {
        @Override
        public Iterator<Player> iterator() {
            synchronized (mutex) {
                return new It();
            }
        }

        @Override
        public int size() {
            synchronized (mutex) {
                int size = manualViewers.size();
                final List<List<Player>> auto = autoViewable;
                if (auto != null) {
                    for (List<Player> players : auto) size += players.size();
                }
                return size;
            }
        }

        final class It implements Iterator<Player> {
            private Iterator<Player> current = ViewEngine.this.manualViewers.iterator();
            private int index;
            private Player next;

            @Override
            public boolean hasNext() {
                synchronized (mutex) {
                    if (current.hasNext()) return true;
                    this.current = nextIterator();
                    if (current == null || !current.hasNext()) return false;
                    if (next != null) return true;
                    this.next = current.next();
                    return next != entity;
                }
            }

            @Override
            public Player next() {
                synchronized (mutex) {
                    final Player value = next;
                    if (value == null) return current.next();
                    next = null;
                    return value;
                }
            }

            private Iterator<Player> nextIterator() {
                final List<List<Player>> auto = autoViewable;
                while (true) {
                    final int updated = index++;
                    if (updated >= auto.size()) return null;
                    final List<Player> players = auto.get(updated);
                    if (players.isEmpty()) continue;
                    return players.iterator();
                }
            }
        }
    }
}
