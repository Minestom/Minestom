package net.minestom.testing;


import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public interface Collector<T> {
    List<T> collect();

    default <P extends T> void assertSingle(Class<P> type, Consumer<P> consumer) {
        List<T> elements = collect();
        assertEquals(1, elements.size(), "Expected 1 element, got " + elements);
        var element = elements.get(0);
        assertInstanceOf(type, element, "Expected type " + type.getSimpleName() + ", got " + element.getClass().getSimpleName());
        consumer.accept((P) element);
    }

    default void assertSingle(Consumer<T> consumer) {
        List<T> elements = collect();
        assertEquals(1, elements.size(), "Expected 1 element, got " + elements);
        consumer.accept(elements.get(0));
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
}
