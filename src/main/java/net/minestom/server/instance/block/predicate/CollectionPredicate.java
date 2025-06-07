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

/**
 * A generic predicate to match against a collection of items.
 * <p>
 * If any fields are null, they are ignored in {@link #test}. If all fields are null, {@link #test} returns {@code true}.
 * @param contains A set of sub-predicates which all must return true for this CollectionPredicate to return true
 * @param counts A set of sub-predicates which all must match a certain number of times for this CollectionPredicate to return true
 * @param size An acceptable range for the collection's size
 * @param <T> Type of item in the collection
 * @param <P> A Predicate that matches against items of type {@code T}
 */
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

    /**
     * A predicate that requires that all of its sub-predicates match at least once.
     */
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

    /**
     * A predicate that counts the number of matching sub-predicates
     * and tests whether it's in the <code>count</code> range.
     */
    public record Count<T, P extends Predicate<T>>(
            @NotNull List<Entry<T, P>> entries) implements Predicate<Collection<T>> {

        public Count {
            entries = List.copyOf(entries);
        }

        public record Entry<T, P extends Predicate<T>>(@NotNull P predicate,
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

    public static @NotNull <T, P extends Predicate<T>> CollectionPredicate.Builder<T, P> builder() {
        return new CollectionPredicate.Builder<>();
    }

    public static class Builder<T, P extends Predicate<T>> {

        private final List<P> containsList = new ArrayList<>();
        private final List<Count.Entry<T, P>> countList = new ArrayList<>();
        private Range.Int size;

        private Builder() {
        }

        /**
         * Specifies that <code>predicate</code> must match at least once.
         */
        public Builder<T, P> mustContain(P predicate) {
            containsList.add(predicate);
            return this;
        }

        /**
         * Specifies that the number of times that <code>predicate</code> matches must fall in the <code>count</code> range.
         */
        public Builder<T, P> mustMatchCount(P predicate, Range.Int count) {
            countList.add(new Count.Entry<>(predicate, count));
            return this;
        }

        /**
         * Specifies that the collection's size must be inside the <code>size</code> range.
         */
        public Builder<T, P> matchSize(Range.Int size) {
            this.size = size;
            return this;
        }

        public CollectionPredicate<T, P> build() {
            return new CollectionPredicate<>(new Contains<>(containsList), new Count<>(countList), size);
        }
    }
}
