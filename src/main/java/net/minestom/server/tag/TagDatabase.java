package net.minestom.server.tag;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

@ApiStatus.Experimental
public sealed interface TagDatabase permits TagDatabaseImpl {
    static @NotNull TagDatabase database() {
        return new TagDatabaseImpl();
    }

    @NotNull TagHandler newHandler();

    @NotNull Selection select(@NotNull Condition condition);

    @NotNull Selection selectAll();

    <T> void track(Tag<T> tag, BiConsumer<TagHandler, T> consumer);

    default <T> @NotNull Optional<TagHandler> findFirst(@NotNull Tag<T> tag, @NotNull T value) {
        final Selection selection = select(Condition.eq(tag, value));
        final List<TagHandler> collect = selection.collect();
        return collect.isEmpty() ? Optional.empty() : Optional.of(collect.get(0));
    }

    sealed interface Selection permits TagDatabaseImpl.SelectionImpl {
        void operate(@NotNull List<@NotNull Operation> operations);

        default void operate(@NotNull Operation @NotNull ... operations) {
            operate(List.of(operations));
        }

        @NotNull List<@NotNull TagHandler> collect(Map<Tag<?>, SortOrder> sorters, int limit);

        default @NotNull List<@NotNull TagHandler> collect() {
            return collect(Map.of(), -1);
        }

        void deleteAll();
    }

    sealed interface Condition permits Condition.And, Condition.Eq, Condition.Range {
        static @NotNull Condition and(@NotNull Condition left, @NotNull Condition right) {
            return new TagDatabaseImpl.ConditionAnd(left, right);
        }

        static <T> @NotNull Condition eq(@NotNull Tag<T> tag, @NotNull T value) {
            return new TagDatabaseImpl.ConditionEq<>(tag, value);
        }

        static <T extends Number> @NotNull Condition range(@NotNull Tag<T> tag, @NotNull T min, @NotNull T max) {
            return new TagDatabaseImpl.ConditionRange<>(tag, min, max);
        }

        sealed interface And extends Condition permits TagDatabaseImpl.ConditionAnd {
            @NotNull Condition left();

            @NotNull Condition right();
        }

        sealed interface Eq<T> extends Condition permits TagDatabaseImpl.ConditionEq {
            @NotNull Tag<T> tag();

            @NotNull T value();
        }

        sealed interface Range<T extends Number> extends Condition permits TagDatabaseImpl.ConditionRange {
            @NotNull Tag<T> tag();

            @NotNull T min();

            @NotNull T max();
        }
    }

    sealed interface Operation permits Operation.Set {
        static <T> Operation set(@NotNull Tag<T> tag, @Nullable T value) {
            return new TagDatabaseImpl.OperationSet<>(tag, value);
        }

        sealed interface Set<T> extends Operation permits TagDatabaseImpl.OperationSet {
            @NotNull Tag<T> tag();

            @Nullable T value();
        }
    }

    sealed interface Sorter permits TagDatabaseImpl.Sorter {
        @NotNull Tag<?> tag();

        @NotNull SortOrder sortOrder();
    }

    enum SortOrder {
        ASCENDING,
        DESCENDING
    }
}
