package net.minestom.server.entity;

import java.util.*;

final class EntityView {
    private final Entity entity;
    private final Set<Player> manualViewers = new HashSet<>();
    private volatile List<List<Player>> autoViewable;

    private final Set<Player> set = new SetImpl();

    private final Object mutex = this;

    EntityView(Entity entity) {
        this.entity = entity;
    }

    void updateReferences(List<List<Player>> references) {
        this.autoViewable = references;
    }

    boolean attemptAdd(Player player) {
        synchronized (mutex) {
            return manualViewers.add(player);
        }
    }

    boolean attemptRemove(Player player) {
        synchronized (mutex) {
            return manualViewers.remove(player);
        }
    }

    boolean hasPredictableViewers() {
        synchronized (mutex) {
            return entity.isAutoViewable() && manualViewers.isEmpty();
        }
    }

    boolean ensureAutoViewer(Entity entity) {
        synchronized (mutex) {
            return manualViewers.isEmpty() || manualViewers.contains(entity);
        }
    }

    Set<Player> asSet() {
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
            private Iterator<Player> current = EntityView.this.manualViewers.iterator();
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
