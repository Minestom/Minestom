package net.minestom.server.tag;

import net.minestom.server.entity.PlayerSkin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static net.minestom.testing.TestUtils.assertEqualsSNBT;
import static org.junit.jupiter.api.Assertions.*;

class TagStructureTest {

    private static final Tag<Entry> STRUCTURE_TAG = Tag.Structure("entry", new TagSerializer<>() {
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

    private static final Tag<Entry> STRUCTURE_TAG2 = Tag.Structure("entry", new TagSerializer<>() {
        private static final Tag<String> VALUE_TAG = Tag.String("value2");

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
    void snbt() {
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
    void overrideBasic() {
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
    void overrideNbt() {
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

    @Test
    void pathOverride() {
        var handler = TagHandler.newHandler();
        Tag<UUID> uuidTag = Tag.UUID("Id").path("SkullOwner");
        Tag<PlayerSkin> skinTag = Tag.Structure("Properties", new TagSerializer<PlayerSkin>() {
            @Override
            public @Nullable PlayerSkin read(@NotNull TagReadable reader) {
                final String value = reader.getTag(Tag.String("Value"));
                final String signature = reader.getTag(Tag.String("Signature"));
                if (value == null || signature == null) return null;
                return new PlayerSkin(value, signature);
            }

            @Override
            public void write(@NotNull TagWritable writer, @NotNull PlayerSkin value) {
                writer.setTag(Tag.String("Value"), value.textures());
                writer.setTag(Tag.String("Signature"), value.signature());
            }
        }).path("SkullOwner");
        var uuid = UUID.fromString("a4a9f3e7-f8b5-4b8e-8b3d-b8b9f8b9f8b9");
        var skin = new PlayerSkin("textures", "signature");
        handler.setTag(uuidTag, uuid);
        handler.setTag(skinTag, skin);

        assertEquals(uuid, handler.getTag(uuidTag));
        assertEquals(skin, handler.getTag(skinTag));
        assertEqualsSNBT("""
                {
                   "SkullOwner":{
                      "Id":[I;-1532365849,-122336370,-1958889287,-122029895],
                      "Properties":{"Signature":"signature","Value":"textures"}
                   }
                }
                """, handler.asCompound());
    }
}
