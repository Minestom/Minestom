package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static net.minestom.server.api.TestUtils.assertEqualsSNBT;
import static org.junit.jupiter.api.Assertions.*;

public class TagViewTest {

    private static final Tag<Entry> VIEW_TAG = Tag.View(new TagSerializer<>() {
        private static final Tag<String> VALUE_TAG = Tag.String("value");

        @Override
        public @Nullable Entry read(@NotNull TagReadable reader) {
            final String value = reader.getTag(VALUE_TAG);
            return value != null ? new Entry(value) : null;
        }

        @Override
        public void write(@NotNull TagWritable writer, @NotNull Entry value) {
            writer.setTag(VALUE_TAG, value.value);
        }
    });

    private record Entry(String value) {
    }

    @Test
    public void basic() {
        var handler = TagHandler.newHandler();
        assertNull(handler.getTag(VIEW_TAG));
        assertFalse(handler.hasTag(VIEW_TAG));

        var entry = new Entry("hello");
        handler.setTag(VIEW_TAG, entry);
        assertTrue(handler.hasTag(VIEW_TAG));
        assertEquals(entry, handler.getTag(VIEW_TAG));

        handler.removeTag(VIEW_TAG);
        assertFalse(handler.hasTag(VIEW_TAG));
        assertNull(handler.getTag(VIEW_TAG));
    }

    @Test
    public void snbt() {
        var handler = TagHandler.newHandler();
        var entry = new Entry("hello");
        handler.setTag(VIEW_TAG, entry);
        assertEqualsSNBT("""
                {
                  "value":"hello"
                }
                """, handler.asCompound());

        handler.removeTag(VIEW_TAG);
        assertEqualsSNBT("{}", handler.asCompound());
    }

    @Test
    public void snbtOverride() {
        var handler = TagHandler.newHandler();
        var entry = new Entry("hello");
        handler.setTag(VIEW_TAG, entry);
        assertEqualsSNBT("""
                {
                  "value":"hello"
                }
                """, handler.asCompound());

        handler.setTag(Tag.Integer("value"), 5);
        assertEqualsSNBT("""
                {
                  "value":5,
                }
                """, handler.asCompound());
    }

}
