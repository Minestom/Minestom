package net.minestom.server.utils;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class WeightedList<T> implements Iterable<T> {
    public static <T> NetworkBuffer.Type<WeightedList<T>> networkType(NetworkBuffer.Type<T> valueType) {
        return Entry.networkType(valueType).list().transform(WeightedList::new, WeightedList::entries);
    }
    public static <T> Codec<WeightedList<T>> codec(StructCodec<T> valueCodec) {
        return Entry.codec(valueCodec).list().transform(WeightedList::new, WeightedList::entries);
    }
    public static <T> Codec<WeightedList<T>> codec(Codec<T> valueCodec) {
        StructCodec<T> wrapper = StructCodec.struct("data", valueCodec, t -> t, t -> t);
        return Entry.codec(wrapper).list().transform(WeightedList::new, WeightedList::entries);
    }

    @SafeVarargs
    public static <T> WeightedList<T> of(Entry<T>... entries) {
        return new WeightedList<>(List.of(entries));
    }

    private final List<Entry<T>> entries;
    private final int totalWeight;

    public WeightedList(List<Entry<T>> entries) {
        this.entries = List.copyOf(entries);

        int total = 0;
        for (Entry<T> entry : this.entries)
            total += entry.weight();
        this.totalWeight = total;
    }

    public List<Entry<T>> entries() {
        return entries;
    }

    public @Nullable T pick(Random random) {
        int pick = random.nextInt(totalWeight);
        for (Entry<T> entry : entries) {
            pick -= entry.weight();
            if (pick < 0) return entry.value();
        }
        return null;
    }

    public T pickOrThrow(Random random) {
        return Objects.requireNonNull(pick(random), "Weighted list was empty");
    }

    @Override
    public Iterator<T> iterator() {
        final var delegate = entries.iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return delegate.hasNext();
            }

            @Override
            public T next() {
                return delegate.next().value();
            }
        };
    }

    public record Entry<T>(T value, int weight) {
        public static <T> NetworkBuffer.Type<Entry<T>> networkType(NetworkBuffer.Type<T> valueType) {
            return NetworkBufferTemplate.template(
                    valueType, Entry::value,
                    NetworkBuffer.VAR_INT, Entry::weight,
                    Entry::new);
        }
        public static <T> StructCodec<Entry<T>> codec(StructCodec<T> valueCodec) {
            return StructCodec.struct(
                    StructCodec.INLINE, valueCodec, Entry::value,
                    "weight", Codec.INT, Entry::weight,
                    Entry::new);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WeightedList<?> that)) return false;
        return totalWeight == that.totalWeight && entries.equals(that.entries);
    }

    @Override
    public int hashCode() {
        int result = entries.hashCode();
        result = 31 * result + totalWeight;
        return result;
    }
}
