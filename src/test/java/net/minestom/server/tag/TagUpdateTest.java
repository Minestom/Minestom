package net.minestom.server.tag;

import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import static net.minestom.testing.TestUtils.assertEqualsSNBT;
import static org.junit.jupiter.api.Assertions.*;

class TagUpdateTest {

    @Test
    void update() {
        var tag = Tag.Integer("coin");
        var handler = TagHandler.newHandler();
        handler.updateTag(tag, integer -> {
            assertNull(integer);
            return 5;
        });
        assertEquals(5, handler.getTag(tag));
        handler.updateTag(tag, integer -> {
            assertEquals(5, integer);
            return 10;
        });
        assertEquals(10, handler.getTag(tag));
    }

    @Test
    void updateDefault() {
        var tag = Tag.Integer("coin").defaultValue(25);
        var handler = TagHandler.newHandler();
        handler.updateTag(tag, integer -> {
            assertEquals(25, integer);
            return 5;
        });
        assertEquals(5, handler.getTag(tag));
        handler.updateTag(tag, integer -> {
            assertEquals(5, integer);
            return 10;
        });
        assertEquals(10, handler.getTag(tag));
    }

    @Test
    void updateRemoval() {
        var tag = Tag.Integer("coin");
        var handler = TagHandler.newHandler();
        handler.setTag(tag, 5);
        handler.updateTag(tag, integer -> {
            assertEquals(5, integer);
            return null;
        });
        assertNull(handler.getTag(tag));
        assertEqualsSNBT("{}", handler.asCompound());
    }

    @Test
    void updateRemovalPath() {
        var tag = Tag.Integer("coin").path("path");
        var handler = TagHandler.newHandler();
        handler.setTag(tag, 5);
        handler.updateTag(tag, integer -> {
            assertEquals(5, integer);
            return null;
        });
        assertNull(handler.getTag(tag));
        assertEqualsSNBT("{}", handler.asCompound());
    }

    @Test
    void updateAndGet() {
        var tag = Tag.Integer("coin");
        var handler = TagHandler.newHandler();
        var result = handler.updateAndGetTag(tag, integer -> {
            assertNull(integer);
            return 5;
        });
        assertEquals(5, result);
        result = handler.updateAndGetTag(tag, integer -> {
            assertEquals(5, integer);
            return 10;
        });
        assertEquals(10, result);
    }

    @Test
    void getAndUpdate() {
        var tag = Tag.Integer("coin");
        var handler = TagHandler.newHandler();
        var result = handler.getAndUpdateTag(tag, integer -> {
            assertNull(integer);
            return 5;
        });
        assertNull(result);
        result = handler.getAndUpdateTag(tag, integer -> {
            assertEquals(5, integer);
            return 10;
        });
        assertEquals(5, result);
    }

    @Test
    void updateHiddenSimilarity() {
        var tag1 = Tag.Integer("coin");
        var tag2 = Tag.Integer("coin").map(i -> i + 1, i -> i - 1);
        var handler = TagHandler.newHandler();
        handler.setTag(tag1, 5);
        assertDoesNotThrow(() -> handler.updateTag(tag2, value -> 5));
        assertEquals(4, handler.getTag(tag1));
        assertEquals(5, handler.getTag(tag2));
    }

    @Test
    void updateStructureConversion() {
        record Test(int coin) {
        }

        var tag1 = Tag.Integer("coin").path("path");
        var tag2 = Tag.Structure("path", Test.class);
        var handler = TagHandler.newHandler();
        handler.setTag(tag1, 5);
        assertEquals(5, handler.getTag(tag1));
        assertEquals(new Test(5), handler.getTag(tag2));

        assertDoesNotThrow(() -> handler.updateTag(tag2, value -> new Test(value.coin + 1)));
        assertEquals(6, handler.getTag(tag1));
        assertEquals(new Test(6), handler.getTag(tag2));

        handler.updateTag(tag2, value -> null);
        assertNull(handler.getTag(tag1));
        assertNull(handler.getTag(tag2));
    }

    @Test
    void updateStructureConversionPath() {
        record Test(int coin) {
        }

        var tag1 = Tag.Integer("coin").path("path", "path2");
        var tag2 = Tag.Structure("path2", Test.class).path("path");
        var handler = TagHandler.newHandler();
        handler.setTag(tag1, 5);
        assertEquals(5, handler.getTag(tag1));
        assertEquals(new Test(5), handler.getTag(tag2));

        assertDoesNotThrow(() -> handler.updateTag(tag2, value -> new Test(value.coin + 1)));
        assertEquals(6, handler.getTag(tag1));
        assertEquals(new Test(6), handler.getTag(tag2));

        handler.updateTag(tag2, value -> null);
        assertNull(handler.getTag(tag1));
        assertNull(handler.getTag(tag2));
    }

    @Test
    void updateStructureConversionPathDouble() {
        record Test(int coin) {
        }
        record Structure(Test test) {
        }

        var tag1 = Tag.Integer("coin").path("path", "test");
        var tag2 = Tag.Structure("path", Structure.class);

        var handler = TagHandler.newHandler();
        handler.setTag(tag1, 5);
        assertEquals(5, handler.getTag(tag1));
        assertEquals(new Structure(new Test(5)), handler.getTag(tag2));

        assertDoesNotThrow(() -> handler.updateTag(tag2, value -> new Structure(new Test(value.test.coin + 1))));
        assertEquals(6, handler.getTag(tag1));
        assertEquals(new Structure(new Test(6)), handler.getTag(tag2));

        handler.updateTag(tag2, value -> null);
        assertNull(handler.getTag(tag1));
        assertNull(handler.getTag(tag2));
    }

    @Test
    void updateViewConversion() {
        record Test(int coin) {
        }

        var tag1 = Tag.Integer("coin");
        var tag2 = Tag.View(Test.class);
        var handler = TagHandler.newHandler();
        handler.setTag(tag1, 5);
        assertDoesNotThrow(() -> handler.updateTag(tag2, value -> new Test(value.coin + 1)));
        assertEquals(6, handler.getTag(tag1));
        assertEquals(new Test(6), handler.getTag(tag2));

        handler.updateTag(tag2, value -> null);
        assertNull(handler.getTag(tag1));
        assertNull(handler.getTag(tag2));
    }

    @Test
    void updateIncompatible() {
        var tagI = Tag.Integer("coin");
        var tagD = Tag.Double("coin");
        var handler = TagHandler.newHandler();
        handler.setTag(tagI, 5);
        assertThrows(ClassCastException.class, () -> handler.updateTag(tagD, value -> 5d));
    }

    @Test
    void updateInner() {
        var tag = Tag.Structure("vec", Vec.class);
        var tagX = Tag.Double("x").path("vec");
        var handler = TagHandler.newHandler();
        handler.setTag(tag, new Vec(5, 10, 15));
        handler.updateTag(tagX, x -> {
            assertEquals(5, x);
            return 7d;
        });
        assertEquals(7d, handler.getTag(tagX));
        assertEquals(new Vec(7, 10, 15), handler.getTag(tag));
    }
}
