package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static net.minestom.server.api.TestUtils.assertEqualsSNBT;
import static org.junit.jupiter.api.Assertions.*;

public class TagStructureTest {

    private static final Tag<Entry> STRUCTURE_TAG = Tag.Structure("entry", new TagSerializer<>() {
        private static final Tag<String> VALUE_TAG = Tag.String("value");

        @Override
        public @Nullable Entry read(@NotNull TagReadable reader) {
            final String value = reader.getTag(VALUE_TAG);
            return value != null ? new Entry(value) : null;
        }

        @Override
        public void write(@NotNull TagWritable writer, @Nullable Entry value) {
            if (value != null) {
                writer.setTag(VALUE_TAG, value.value);
            } else {
                writer.removeTag(VALUE_TAG);
            }
        }
    });

    private static final Tag<Entry> STRUCTURE_TAG2 = Tag.Structure("entry", new TagSerializer<>() {
        private static final Tag<String> VALUE_TAG = Tag.String("value2");

        @Override
        public @Nullable Entry read(@NotNull TagReadable reader) {
            final String value = reader.getTag(VALUE_TAG);
            return value != null ? new Entry(value) : null;
        }

        @Override
        public void write(@NotNull TagWritable writer, @Nullable Entry value) {
            if (value != null) {
                writer.setTag(VALUE_TAG, value.value);
            } else {
                writer.removeTag(VALUE_TAG);
            }
        }
    });

    private record Entry(String value) {
    }

    @Test
    public void basic() {
        var handler = TagHandler.newHandler();
        assertNull(handler.getTag(STRUCTURE_TAG));
        assertFalse(handler.hasTag(STRUCTURE_TAG));

        var entry = new Entry("hello");
        handler.setTag(STRUCTURE_TAG, entry);
        assertTrue(handler.hasTag(STRUCTURE_TAG));
        assertEquals(entry, handler.getTag(STRUCTURE_TAG));

        handler.removeTag(STRUCTURE_TAG);
        assertFalse(handler.hasTag(STRUCTURE_TAG));
        assertNull(handler.getTag(STRUCTURE_TAG));
    }

    @Test
    public void snbt() {
        var handler = TagHandler.newHandler();
        var entry = new Entry("hello");
        handler.setTag(STRUCTURE_TAG, entry);
        assertEqualsSNBT("""
                {
                  "entry": {
                    "value":"hello"
                  }
                }
                """, handler.asCompound());

        handler.removeTag(STRUCTURE_TAG);
        assertEqualsSNBT("{}", handler.asCompound());
    }

    @Test
    public void overrideBasic() {
        var handler = TagHandler.newHandler();
        assertNull(handler.getTag(STRUCTURE_TAG));
        assertFalse(handler.hasTag(STRUCTURE_TAG));

        var entry1 = new Entry("hello");
        var entry2 = new Entry("hello2");

        // Add first entry
        {
            handler.setTag(STRUCTURE_TAG, entry1);
            assertTrue(handler.hasTag(STRUCTURE_TAG));
            assertEquals(entry1, handler.getTag(STRUCTURE_TAG));
        }
        // Add second entry
        {
            handler.setTag(STRUCTURE_TAG2, entry2);
            assertTrue(handler.hasTag(STRUCTURE_TAG2));
            assertEquals(entry2, handler.getTag(STRUCTURE_TAG2));
            // Assert first
            assertFalse(handler.hasTag(STRUCTURE_TAG));
            assertNull(handler.getTag(STRUCTURE_TAG));
        }
    }

    @Test
    public void overrideNbt() {
        var handler = TagHandler.newHandler();
        var entry1 = new Entry("hello");
        var entry2 = new Entry("hello2");
        // Add first entry
        {
            handler.setTag(STRUCTURE_TAG, entry1);
            assertEqualsSNBT("""
                    {
                      "entry": {
                        "value":"hello"
                      }
                    }
                    """, handler.asCompound());
        }
        // Add second entry
        {
            handler.setTag(STRUCTURE_TAG2, entry2);
            assertEqualsSNBT("""
                    {
                      "entry": {
                        "value2": "hello2"
                      }
                    }
                    """, handler.asCompound());
        }
    }
}
