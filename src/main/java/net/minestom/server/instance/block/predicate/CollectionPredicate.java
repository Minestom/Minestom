package net.minestom.server.instance.block.predicate;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.utils.Range;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public record CollectionPredicate<T, P extends Predicate<T>>(@Nullable Contains<T, P> contains,
                                                             @Nullable Count<T, P> counts,
                                                             @Nullable Range.Int size) implements Predicate<Collection<T>> {

    public static <T, P extends Predicate<T>> @NotNull Codec<CollectionPredicate<T, P>> createCodec(Codec<P> codec) {
        return StructCodec.struct(
                "contains", Contains.createCodec(codec).optional(), CollectionPredicate::contains,
                "count", Count.createCodec(codec).optional(), CollectionPredicate::counts,
                "size", DataComponentPredicates.INT_RANGE_CODEC.optional(), CollectionPredicate::size,
                CollectionPredicate::new
        );
    }

    @Override
    public boolean test(@NotNull Collection<T> collection) {
        return (contains == null || contains.test(collection)) &&
                (counts == null || counts.test(collection)) &&
                (size == null || size.inRange(collection.size()));
    }

    public record Contains<T, P extends Predicate<T>>(@NotNull List<P> predicates) implements Predicate<Collection<T>> {

        public Contains {
            predicates = List.copyOf(predicates);
        }

        public static <T, P extends Predicate<T>> @NotNull Codec<Contains<T, P>> createCodec(Codec<P> codec) {
            return codec.listOrSingle().transform(Contains::new, Contains::predicates);
        }

        @Override
        public boolean test(@NotNull Collection<T> collection) {
            List<Predicate<T>> predicates = new ArrayList<>(this.predicates);
            if (predicates.isEmpty()) {
                return true;
            }
            for (T t : collection) {
                predicates.removeIf(p -> p.test(t));
                if (predicates.isEmpty()) {
                    return true;
                }
            }
            return false;
        }
    }

    public record Count<T, P extends Predicate<T>>(
            @NotNull List<Entry<T, P>> entries) implements Predicate<Collection<T>> {

        public Count {
            entries = List.copyOf(entries);
        }

        record Entry<T, P extends Predicate<T>>(@NotNull P predicate,
                                                @NotNull Range.Int count) implements Predicate<Collection<T>> {
            public static <T, P extends Predicate<T>> @NotNull Codec<Entry<T, P>> createCodec(Codec<P> codec) {
                return StructCodec.struct(
                        "test", codec, Entry::predicate,
                        "count", DataComponentPredicates.INT_RANGE_CODEC, Entry::count,
                        Entry::new
                );
            }

            @Override
            public boolean test(@NotNull Collection<T> collection) {
                return this.count.inRange((int) collection.stream().filter(this.predicate).count());
            }
        }

        public static <T, P extends Predicate<T>> @NotNull Codec<Count<T, P>> createCodec(Codec<P> codec) {
            return Entry.createCodec(codec).listOrSingle().transform(Count::new, Count::entries);
        }

        @Override
        public boolean test(@NotNull Collection<T> collection) {
            for (Entry<T, P> entry : entries) {
                if (!entry.test(collection)) {
                    return false;
                }
            }
            return true;
        }
    }
}
