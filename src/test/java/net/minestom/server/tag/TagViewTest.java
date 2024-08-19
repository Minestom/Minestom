package net.minestom.server.tag;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static net.minestom.testing.TestUtils.assertEqualsSNBT;
import static org.junit.jupiter.api.Assertions.*;

class TagViewTest {

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
    void basic() {
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
    void snbt() {
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
    void snbtOverride() {
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

    @Test
    void empty() {
        var handler = TagHandler.newHandler();
        var tag = Tag.View(new TagSerializer<Entry>() {
            @Override
            public @Nullable Entry read(@NotNull TagReadable reader) {
                // Empty
                return null;
            }

            @Override
            public void write(@NotNull TagWritable writer, @NotNull Entry value) {
                // Empty
            }
        });
        assertNull(handler.getTag(tag));
        assertFalse(handler.hasTag(tag));

        var entry = new Entry("hello");
        handler.setTag(tag, entry);
        assertNull(handler.getTag(tag));
        assertFalse(handler.hasTag(tag));
        assertEqualsSNBT("{}", handler.asCompound());

        handler.removeTag(tag);
        assertFalse(handler.hasTag(tag));
        assertNull(handler.getTag(VIEW_TAG));
        assertEqualsSNBT("{}", handler.asCompound());
    }

    @Test
    void path() {
        var handler = TagHandler.newHandler();
        var tag = VIEW_TAG.path("path");
        assertNull(handler.getTag(tag));
        assertFalse(handler.hasTag(tag));

        var entry = new Entry("hello");
        handler.setTag(tag, entry);
        assertTrue(handler.hasTag(tag));
        assertEquals(entry, handler.getTag(tag));

        handler.removeTag(tag);
        assertFalse(handler.hasTag(tag));
        assertNull(handler.getTag(tag));
    }

    @Test
    void pathSnbt() {
        var handler = TagHandler.newHandler();
        var tag = VIEW_TAG.path("path");
        var entry = new Entry("hello");
        handler.setTag(tag, entry);
        assertEqualsSNBT("""
                {
                  "path":{
                    "value":"hello"
                  }
                }
                """, handler.asCompound());

        handler.removeTag(tag);
        assertEqualsSNBT("{}", handler.asCompound());
    }

    @Test
    void compoundSerializer() {
        var tag = Tag.View(TagSerializer.COMPOUND);
        var handler = TagHandler.newHandler();
        handler.setTag(tag, CompoundBinaryTag.builder().putString("value", "hello").build());
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

        handler.setTag(tag, CompoundBinaryTag.empty());
        assertEqualsSNBT("{}", handler.asCompound());

        handler.setTag(tag, null);
        assertEqualsSNBT("{}", handler.asCompound());
    }
}
