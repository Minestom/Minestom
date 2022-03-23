package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static net.minestom.server.api.TestUtils.assertEqualsIgnoreSpace;
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
    public void snbt() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Integer("number").path("display");
        handler.setTag(tag, 5);
        assertEqualsIgnoreSpace("""
                {
                  "display": {
                    "number": 5
                  }
                }
                """, handler.asCompound().toSNBT());

        handler.removeTag(tag);
        assertEqualsIgnoreSpace("{}", handler.asCompound().toSNBT());
    }

    @Test
    public void doubleSnbt() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Integer("number").path("display");
        var tag1 = Tag.String("string").path("display");
        handler.setTag(tag, 5);
        handler.setTag(tag1, "test");

        assertEquals(NBT.Compound(Map.of("display", NBT.Compound(Map.of("string", NBT.String("test"),
                "number", NBT.Int(5))))), handler.asCompound());

        handler.removeTag(tag);
        assertEqualsIgnoreSpace("""
                {
                  "display": {
                    "string": "test"
                  }
                }
                """, handler.asCompound().toSNBT());

        handler.removeTag(tag1);
        assertEqualsIgnoreSpace("{}", handler.asCompound().toSNBT());
    }

    @Test
    public void differentPath() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Integer("number");
        var path = tag.path("display");
        handler.setTag(tag, 5);
        assertEqualsIgnoreSpace("""
                {
                  "number": 5
                }
                """, handler.asCompound().toSNBT());

        handler.setTag(path, 5);
        assertEquals(NBT.Compound(Map.of("display", NBT.Compound(Map.of("number", NBT.Int(5))),
                "number", NBT.Int(5))), handler.asCompound());


        handler.removeTag(tag);
        assertEqualsIgnoreSpace("""
                {
                  "display": {
                    "number": 5
                  }
                }
                """, handler.asCompound().toSNBT());
    }

    @Test
    public void overrideSnbt() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Integer("key");
        var tag1 = Tag.Integer("value").path("key");
        handler.setTag(tag, 5);
        assertEqualsIgnoreSpace("""
                {
                  "key": 5
                }
                """, handler.asCompound().toSNBT());

        assertThrows(IllegalStateException.class, () -> handler.setTag(tag1, 5));
    }

    @Test
    public void chaining() {
        var handler = TagHandler.newHandler();
        var tag = Tag.Integer("key");
        var path = Tag.Integer("key").path("first", "second");
        handler.setTag(path, 5);
        assertEqualsIgnoreSpace("""
                {
                  "first": {
                    "second": {
                      "key": 5
                    }
                  }
                }
                """, handler.asCompound().toSNBT());

        assertEquals(5, handler.getTag(path));
        assertNull(handler.getTag(tag));

        handler.removeTag(path);
        assertEqualsIgnoreSpace("{}", handler.asCompound().toSNBT());
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
            public void write(@NotNull TagWritable writer, @Nullable Entry value) {
                writer.setTag(VALUE_TAG, value != null ? value.value : null);
            }
        });

        handler.setTag(struct, new Entry(5));
        assertEqualsIgnoreSpace("""
                {
                  "struct": {
                    "value": 5
                  }
                }
                """, handler.asCompound().toSNBT());

        handler.setTag(tag, 5);
        assertEquals(NBT.Compound(Map.of("value", NBT.Int(5),
                        "struct", NBT.Compound(Map.of("value", NBT.Int(5))))),
                handler.asCompound());

        // Cannot enter a structure from a path tag
        assertThrows(IllegalStateException.class, () -> handler.setTag(tag.path("struct"), 5));
    }
}
