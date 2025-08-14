package net.minestom.testing;


import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

public interface Collector<T> {
    List<T> collect();

    default <P extends T> void assertSingle(Class<P> type, Consumer<P> consumer) {
        List<T> elements = collect();
        assertEquals(1, elements.size(), "Expected 1 element, got " + elements);
        var element = elements.getFirst();
        assertInstanceOf(type, element, "Expected type " + type.getSimpleName() + ", got " + element.getClass().getSimpleName());
        //noinspection unchecked
        consumer.accept((P) element);
    }

    default void assertSingle(Consumer<T> consumer) {
        List<T> elements = collect();
        assertEquals(1, elements.size(), "Expected 1 element, got " + elements);
        consumer.accept(elements.getFirst());
    }

    default void assertCount(int count) {
        List<T> elements = collect();
        assertEquals(count, elements.size(), "Expected " + count + " element(s), got " + elements.size() + ": " + elements);
    }

    default void assertSingle() {
        assertCount(1);
    }

    default void assertEmpty() {
        assertCount(0);
    }

    /**
     * Asserts that at least one element matches the given predicate.
     */
    default void assertAnyMatch(Predicate<T> predicate) {
        List<T> elements = collect();
        assertTrue(elements.stream().anyMatch(predicate),
                "No elements matched the predicate. Elements: " + elements);
    }

    /**
     * Asserts that no elements match the given predicate.
     */
    default void assertNoneMatch(Predicate<T> predicate) {
        List<T> elements = collect();
        assertFalse(elements.stream().anyMatch(predicate),
                "Found elements that matched the predicate: " + elements.stream().filter(predicate).toList());
    }

    /**
     * Asserts that all elements match the given predicate.
     */
    default void assertAllMatch(Predicate<T> predicate) {
        List<T> elements = collect();
        assertTrue(elements.stream().allMatch(predicate),
                "Not all elements matched the predicate. Elements: " + elements);
    }
}
