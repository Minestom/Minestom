package net.minestom.server.property;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerPropertyTest {

    private final List<String> assigned = new ArrayList<>();
    private int counter;

    @AfterEach
    void clearAssignedProperties() {
        for (String name : assigned) System.clearProperty(name);
        assigned.clear();
    }

    @Test
    void defaultsWhenSystemPropertyUnset() {
        var name = unset();
        var property = ServerPropertyImpl.create(name, 20, Integer::parseInt);
        assertEquals(name, property.name());
        assertEquals(20, property.defaultValue());
        assertEquals(20, property.get());
        assertEquals(name + "=20", property.toString());
    }

    @Test
    void readsSystemProperty() {
        assertEquals(40, ServerPropertyImpl.create(valued("40"), 20, Integer::parseInt).get());
        assertEquals(40L, ServerPropertyImpl.create(valued("40"), 20L, Long::parseLong).get());
        assertEquals(0.5f, ServerPropertyImpl.create(valued("0.5"), 1f, Float::parseFloat).get());
        assertTrue(ServerPropertyImpl.create(valued("true"), false, Boolean::parseBoolean).get());
        assertEquals("custom", ServerPropertyImpl.create(valued("custom"), "default", Function.identity()).get());
    }

    @Test
    void unparseableSystemPropertyThrows() {
        var name = valued("not-a-number");
        assertThrows(IllegalArgumentException.class, () -> ServerPropertyImpl.create(name, 20, Integer::parseInt));
    }

    @Test
    void outOfRangeSystemPropertyThrows() {
        var name = valued("0");
        assertThrows(IllegalArgumentException.class, () -> ServerPropertyImpl.create(name, 32, Integer::parseInt, range(1, 64)));
    }

    @Test
    void outOfRangeDefaultThrows() {
        var name = unset();
        assertThrows(IllegalArgumentException.class, () -> ServerPropertyImpl.create(name, 512, Integer::parseInt, range(1, 64)));
    }

    /**
     * An immutable property must stay a record, so that reads through a constant reference fold to
     * the value it resolved.
     */
    @Test
    void immutableIsARecordAndRejectsWrites() {
        var property = ServerPropertyImpl.create(switched(unset(), false), 20, Integer::parseInt);
        assertTrue(property.getClass().isRecord());
        assertFalse(property.writable());
        var thrown = assertThrows(IllegalStateException.class, () -> property.set(40));
        assertTrue(thrown.getMessage().contains(property.name()));
        assertEquals(20, property.get());
    }

    @Test
    void mutableAcceptsWritesAfterRead() {
        var property = ServerPropertyImpl.create(switched(unset(), true), 20, Integer::parseInt);
        assertFalse(property.getClass().isRecord());
        assertEquals(20, property.get());
        property.set(40);
        assertTrue(property.writable());
        assertEquals(40, property.get());
    }

    /**
     * A property that does not ask for anything follows the global default, which is off.
     */
    @Test
    void mutabilityFallsBackToTheGlobalDefault() {
        var property = ServerPropertyImpl.create(unset(), 20, Integer::parseInt);
        assertFalse(property.writable());
        assertTrue(property.getClass().isRecord());
    }

    @Test
    void setValidatesRange() {
        var property = ServerPropertyImpl.create(switched(unset(), true), 32, Integer::parseInt, range(1, 64));
        assertThrows(IllegalArgumentException.class, () -> property.set(0));
        assertEquals(32, property.get());
    }

    @Test
    void serverPropertyUsesTheGivenParser() {
        var property = ServerPropertyImpl.create(valued("PT5S"), Duration.ZERO, Duration::parse);
        assertEquals(Duration.ofSeconds(5), property.get());
    }

    @Test
    void serverPropertyRunsTheValidator() {
        final Consumer<String> rejectEmpty = value -> {
            if (value.isEmpty()) throw new IllegalArgumentException("empty");
        };
        var property = ServerPropertyImpl.create(switched(unset(), true), "abc", Function.identity(), rejectEmpty);
        assertThrows(IllegalArgumentException.class, () -> property.set(""));
        assertEquals("abc", property.get());
        assertThrows(IllegalArgumentException.class,
                () -> ServerPropertyImpl.create(unset(), "", Function.identity(), rejectEmpty));
    }

    private static Consumer<Integer> range(int minValue, int maxValue) {
        return value -> {
            if (value < minValue || value > maxValue) throw new IllegalArgumentException("out of range");
        };
    }

    private String valued(String value) {
        return assign(unset(), "", value);
    }

    private String switched(String name, boolean mutable) {
        return assign(name, ServerPropertyImpl.MUTABLE_SUFFIX, String.valueOf(mutable));
    }

    private String assign(String name, String suffix, String value) {
        System.setProperty(name + suffix, value);
        assigned.add(name + suffix);
        return name;
    }

    private String unset() {
        return "minestom.test.property." + counter++;
    }
}
