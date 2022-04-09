package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static net.minestom.server.api.TestUtils.assertEqualsSNBT;
import static org.junit.jupiter.api.Assertions.*;

public class TagPathTest {

    @Test
    public void basic() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Integer("number");
        var path = tag.path("display");
        handler.setTag(path, 5);
        assertEquals(5, handler.getTag(path));
        assertNull(handler.getTag(tag));

        handler.setTag(path, 6);
        assertEquals(6, handler.getTag(path));
        assertNull(handler.getTag(tag));

        handler.removeTag(path);
        assertNull(handler.getTag(path));
        assertNull(handler.getTag(tag));
    }

    @Test
    public void invalidPath() {
        assertThrows(IllegalArgumentException.class, () -> Tag.Integer("number").path(""));
        assertThrows(IllegalArgumentException.class, () -> Tag.Integer("number").path("path", null));
    }

    @Test
    public void emptyRemoval() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Integer("number").path("display");
        handler.removeTag(tag);
        assertNull(handler.getTag(tag));
        assertEqualsSNBT("{}", handler.asCompound());
    }

    @Test
    public void snbt() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Integer("number").path("display");
        handler.setTag(tag, 5);
        assertEqualsSNBT("""
                {
                  "display": {
                    "number":5
                  }
                }
                """, handler.asCompound());

        handler.removeTag(tag);
        assertEqualsSNBT("{}", handler.asCompound());
    }

    @Test
    public void doubleSnbt() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Integer("number").path("display");
        var tag1 = Tag.String("string").path("display");
        handler.setTag(tag, 5);
        handler.setTag(tag1, "test");

        assertEqualsSNBT("""
                {
                  "display": {
                    "string":"test",
                    "number":5
                  }
                }
                """, handler.asCompound());

        handler.removeTag(tag);
        assertEqualsSNBT("""
                {
                  "display": {
                    "string":"test"
                  }
                }
                """, handler.asCompound());

        handler.removeTag(tag1);
        assertEqualsSNBT("{}", handler.asCompound());
    }

    @Test
    public void differentPath() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Integer("number");
        var path = tag.path("display");
        handler.setTag(tag, 5);
        assertEqualsSNBT("""
                {
                  "number":5
                }
                """, handler.asCompound());

        handler.setTag(path, 5);
        assertEqualsSNBT("""
                {
                  "number":5,
                  "display": {
                    "number":5
                  }
                }
                """, handler.asCompound());

        handler.removeTag(tag);
        assertEqualsSNBT("""
                {
                  "display": {
                    "number":5
                  }
                }
                """, handler.asCompound());
    }

    @Test
    public void overrideSnbt() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Integer("key");
        var tag1 = Tag.Integer("value").path("key");
        handler.setTag(tag, 5);
        assertEqualsSNBT("""
                {
                  "key":5
                }
                """, handler.asCompound());

        assertThrows(IllegalStateException.class, () -> handler.setTag(tag1, 5));
    }

    @Test
    public void forgetPath() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Integer("key");
        var path = Tag.Integer("value").path("key");
        handler.setTag(path, 5);
        assertNull(handler.getTag(tag));
    }

    @Test
    public void chaining() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Integer("key");
        var path = Tag.Integer("key").path("first", "second");
        handler.setTag(path, 5);
        assertEqualsSNBT("""
                {
                  "first": {
                    "second": {
                      "key":5
                    }
                  }
                }
                """, handler.asCompound());

        assertEquals(5, handler.getTag(path));
        assertNull(handler.getTag(tag));

        handler.removeTag(path);
        assertEqualsSNBT("{}", handler.asCompound());
    }

    @Test
    public void chainingDouble() {
        var handler = TagHandler.newHandler();
        var path = Tag.Integer("key").path("first", "second");
        var path1 = Tag.Integer("key").path("first");
        handler.setTag(path, 5);
        assertEqualsSNBT("""
                {
                  "first": {
                    "second": {
                      "key":5
                    }
                  }
                }
                """, handler.asCompound());
        assertEquals(5, handler.getTag(path));

        handler.setTag(path1, 5);
        assertEqualsSNBT("""
                {
                  "first": {
                    "key":5,
                    "second": {
                      "key":5
                    }
                  }
                }
                """, handler.asCompound());
        assertEquals(5, handler.getTag(path));
        assertEquals(5, handler.getTag(path1));

        handler.removeTag(path);
        assertEqualsSNBT("""
                {
                  "first": {
                    "key":5
                  }
                }
                """, handler.asCompound());

        handler.removeTag(path1);
        assertEqualsSNBT("{}", handler.asCompound());
    }

    @Test
    public void structureObstruction() {
        record Entry(int value) {
        }

        var handler = TagHandler.newHandler();
        var tag = Tag.Integer("value");
        var struct = Tag.Structure("struct", new TagSerializer<Entry>() {
            private static final Tag<Integer> VALUE_TAG = Tag.Integer("value");

            @Override
            public @Nullable Entry read(@NotNull TagReadable reader) {
                final Integer value = reader.getTag(VALUE_TAG);
                return value != null ? new Entry(value) : null;
            }

            @Override
            public void write(@NotNull TagWritable writer, @NotNull Entry value) {
                writer.setTag(VALUE_TAG, value.value);
            }
        });

        handler.setTag(struct, new Entry(5));
        assertEqualsSNBT("""
                {
                  "struct": {
                    "value":5
                  }
                }
                """, handler.asCompound());
        assertEquals(5, handler.getTag(tag.path("struct")));

        handler.setTag(tag, 5);
        assertEqualsSNBT("""
                {
                  value:5,
                  "struct": {
                    "value":5
                  }
                }
                """, handler.asCompound());

        // Cannot enter a structure from a path tag
        assertThrows(IllegalStateException.class, () -> handler.setTag(tag.path("struct"), 5));
    }

    @Test
    public void tagObstruction() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Integer("key");
        var path = Tag.Integer("value").path("key", "second");
        handler.setTag(tag, 5);
        assertEqualsSNBT("""
                {
                  "key":5
                }
                """, handler.asCompound());
        assertThrows(IllegalStateException.class, () -> handler.setTag(path, 5));
    }
}
