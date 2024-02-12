package net.minestom.server.tag;

import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTCompoundLike;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

final class TagDatabaseImpl implements TagDatabase {
    private final ReentrantLock lock = new ReentrantLock();
    private final List<Entry> entries = new ArrayList<>();
    private final Map<String, List<Pair<Tag, BiConsumer<TagHandler, Object>>>> tracked = new HashMap<>();

    @Override
    public @NotNull TagHandler newHandler() {
        final Entry entry = new Entry();
        this.lock.lock();
        this.entries.add(entry);
        this.lock.unlock();
        return entry;
    }

    @Override
    public @NotNull Selection select(@NotNull Condition condition) {
        return new SelectionImpl(condition);
    }

    @Override
    public @NotNull Selection selectAll() {
        return new SelectionImpl(null);
    }

    @Override
    public <T> void track(Tag<T> tag, BiConsumer<TagHandler, T> consumer) {
        Pair pair = Pair.of(tag, (BiConsumer<TagHandler, Object>) consumer);
        lock.lock();
        {
            StringBuilder builder = new StringBuilder();
            if (tag.path != null) {
                for (Tag.PathEntry s : tag.path) {
                    builder.append(s.name());
                    tracked.computeIfAbsent(builder.toString(), tmp -> new ArrayList<>()).add(pair);
                    builder.append('.');
                }
            }
            builder.append(tag.getKey());
            tracked.computeIfAbsent(builder.toString(), tmp -> new ArrayList<>()).add(pair);
        }
        lock.unlock();
    }

    private static String getTagPath(Tag<?> tag) {
        // Create string like test.test.test from tag path + name
        StringBuilder builder = new StringBuilder();
        if (tag.path != null) {
            for (Tag.PathEntry s : tag.path) {
                builder.append(s.name()).append('.');
            }
        }
        builder.append(tag.getKey());
        return builder.toString();
    }

    record ConditionAnd(Condition left, Condition right) implements Condition.And {
    }

    record ConditionEq<T>(Tag<T> tag, T value) implements Condition.Eq<T> {
    }

    record ConditionRange<T extends Number>(Tag<T> tag, T min, T max) implements Condition.Range<T> {
    }

    record OperationSet<T>(Tag<T> tag, T value) implements Operation.Set<T> {
    }

    record Sorter(Tag<?> tag, SortOrder sortOrder) implements TagDatabase.Sorter {
    }

    final class SelectionImpl implements Selection {
        private final Condition condition;

        SelectionImpl(Condition condition) {
            this.condition = condition;
        }

        @Override
        public void operate(@NotNull List<@NotNull Operation> operations) {
            List<TagHandler> collect = collect();
            for (TagHandler entry : collect) {
                for (Operation operation : operations) {
                    if (operation instanceof Operation.Set set) {
                        entry.setTag(set.tag(), set.value());
                    } else {
                        throw new RuntimeException("Unsupported: " + operation);
                    }
                }
            }
        }

        @Override
        public @NotNull List<@NotNull TagHandler> collect(Map<Tag<?>, SortOrder> sorters, int limit) {
            List<TagHandler> result = new ArrayList<>();
            // Insert valid entries
            lock.lock();
            for (Entry entry : TagDatabaseImpl.this.entries) {
                if (condition == null || validate(entry, condition)) {
                    result.add(entry);
                    if (limit > -1 && result.size() == limit) break;
                }
            }
            lock.unlock();
            // Sort entries
            if (!sorters.isEmpty()) {
                Comparator<TagHandler> comparator = null;
                for (var entry : sorters.entrySet()) {
                    final Tag<?> tag = entry.getKey();
                    final SortOrder sorter = entry.getValue();
                    Comparator<TagHandler> test = Comparator.comparing(tagHandler ->
                            (Comparable) tagHandler.getTag(tag));
                    if (sorter == SortOrder.DESCENDING) {
                        test = test.reversed();
                    }
                    comparator = comparator != null ?
                            comparator.thenComparing(test) : test;
                }
                result.sort(comparator);
            }
            return List.copyOf(result);
        }

        @Override
        public void deleteAll() {
            lock.lock();
            TagDatabaseImpl.this.entries.removeIf(entry -> validate(entry, condition));
            lock.unlock();
        }

        private boolean validate(Entry entry, Condition condition) {
            if (condition instanceof Condition.Eq<?> eq) {
                final Object value = entry.getTag(eq.tag());
                return Objects.equals(value, eq.value());
            } else {
                throw new RuntimeException("Unsupported: " + condition);
            }
        }
    }

    final class Entry implements TagHandler {
        private final TagHandler handler = TagHandler.newHandler();

        @Override
        public @NotNull TagReadable readableCopy() {
            return handler.readableCopy();
        }

        @Override
        public @NotNull TagHandler copy() {
            return handler.copy();
        }

        @Override
        public void updateContent(@NotNull NBTCompoundLike compound) {
            this.handler.updateContent(compound);
            // TODO update?
        }

        @Override
        public @NotNull NBTCompound asCompound() {
            return handler.asCompound();
        }

        @Override
        public <T> void updateTag(@NotNull Tag<T> tag, @NotNull UnaryOperator<@UnknownNullability T> value) {
            this.handler.updateTag(tag, value);
            handleUpdate(tag);
        }

        @Override
        public <T> @UnknownNullability T updateAndGetTag(@NotNull Tag<T> tag, @NotNull UnaryOperator<@UnknownNullability T> value) {
            final T result = handler.updateAndGetTag(tag, value);
            handleUpdate(tag);
            return result;
        }

        @Override
        public <T> @UnknownNullability T getAndUpdateTag(@NotNull Tag<T> tag, @NotNull UnaryOperator<@UnknownNullability T> value) {
            final T result = handler.getAndUpdateTag(tag, value);
            handleUpdate(tag);
            return result;
        }

        @Override
        public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
            return handler.getTag(tag);
        }

        @Override
        public <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
            this.handler.setTag(tag, value);
            handleUpdate(tag);
        }

        private void handleUpdate(Tag<?> writeTag) {
            final String tagPath = getTagPath(writeTag);
            final String[] split = tagPath.split("\\.");
            StringBuilder builder = new StringBuilder();
            for (String s : split) {
                builder.append(s);
                final List<Pair<Tag, BiConsumer<TagHandler, Object>>> pairs = tracked.get(builder.toString());
                if (pairs != null) {
                    for(var pair : pairs){
                        final Tag tag = pair.left();
                        final Object value = handler.getTag(tag);
                        pair.right().accept(this, value);
                    }
                }
                builder.append('.');
            }
        }
    }
}
