package net.minestom.server.tag;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.tag.TagDatabase.Condition;
import net.minestom.server.tag.TagDatabase.Operation;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class TagDatabaseTest {

    @Test
    public void insert() {
        TagDatabase db = TagDatabase.database();
        var compound = NBT.Compound(Map.of("key", NBT.Int(1)));
        db.newHandler().updateContent(compound);
    }

    @Test
    public void insertNested() {
        TagDatabase db = TagDatabase.database();
        var compound = NBT.Compound(Map.of("key",
                NBT.Compound(Map.of("value", NBT.Int(1)))));
        db.newHandler().updateContent(compound);
    }

    @Test
    public void empty() {
        TagDatabase db = TagDatabase.database();
        var select = db.select(Condition.eq(Tag.String("key"), "value"));
        var result = select.collect();
        assertTrue(result.isEmpty());
    }

    @Test
    public void findFilterEq() {
        TagDatabase db = TagDatabase.database();
        var tag = Tag.String("key");

        {
            db.newHandler().setTag(tag, "value");
            var collect = db.select(Condition.eq(tag, "value")).collect();
            assertEquals(1, collect.size());
        }

        {
            db.newHandler().setTag(tag, "value");
            var collect = db.select(Condition.eq(tag, "value")).collect();
            assertEquals(2, collect.size());
        }

        {
            db.newHandler().setTag(tag, "value2");
            var collect = db.select(Condition.eq(tag, "value")).collect();
            assertEquals(2, collect.size());
        }
    }

    @Test
    public void findFilterCompoundEq() {
        TagDatabase db = TagDatabase.database();
        var child = NBT.Compound(Map.of("something", NBT.String("something")));
        var compound = NBT.Compound(Map.of("key", NBT.String("value2"),
                "other", child));

        db.newHandler().updateContent(compound);

        var result = db.select(Condition.eq(Tag.NBT("other"), child)).collect();
        assertEquals(1, result.size());
        assertEquals(compound, result.get(0).asCompound());
    }

    @Test
    public void findTagMismatch() {
        TagDatabase db = TagDatabase.database();
        var tagInteger = Tag.Integer("key");
        var tagDouble = Tag.Double("key");

        db.newHandler().updateContent(NBT.Compound(Map.of("key", NBT.Int(1))));
        db.newHandler().updateContent(NBT.Compound(Map.of("key", NBT.Double(1))));

        var queryInteger = db.select(Condition.eq(tagInteger, 1));
        assertEquals(1, queryInteger.collect().size());

        var queryDouble = db.select(Condition.eq(tagDouble, 1D));
        assertEquals(1, queryDouble.collect().size());
    }

    @Test
    public void findArray() {
        TagDatabase db = TagDatabase.database();
        var tag = Tag.NBT("key");
        var nbt = NBT.IntArray(1, 2, 3);
        var compound = NBT.Compound(Map.of("key", nbt));

        db.newHandler().updateContent(compound);

        var query = db.select(Condition.eq(tag, nbt));
        var result = query.collect();
        assertEquals(1, result.size());
        assertEquals(compound, result.get(0).asCompound());
    }

    @Test
    public void valueChange() {
        TagDatabase db = TagDatabase.database();
        var tag = Tag.Integer("key");

        var handler = db.newHandler();
        handler.setTag(tag, 1);
        handler.setTag(tag, 5);

        var result1 = db.select(Condition.eq(tag, 1)).collect();
        var result5 = db.select(Condition.eq(tag, 5)).collect();

        assertEquals(0, result1.size());
        assertEquals(1, result5.size());
        assertEquals(handler.asCompound(), result5.get(0).asCompound());
    }

    @Test
    public void findNestedTag() {
        TagDatabase db = TagDatabase.database();
        var handler = db.newHandler();

        var tag = Tag.String("key");
        var tag2 = Tag.String("key2").path("path");
        var tag3 = Tag.String("key3").path("path", "path2");
        var tag4 = Tag.String("key4").path("path", "path2");
        var tag5 = Tag.String("key4").path("path", "path2", "path3", "path4", "path5");

        handler.setTag(tag, "value");
        handler.setTag(tag2, "value2");
        handler.setTag(tag3, "value3");
        handler.setTag(tag4, "value4");
        handler.setTag(tag5, "value5");

        var copy = handler.copy();

        // Check query based on nested tag
        assertListEqualsIgnoreOrder(List.of(copy), db.select(Condition.eq(tag, "value")).collect());
        assertListEqualsIgnoreOrder(List.of(copy), db.select(Condition.eq(tag2, "value2")).collect());
        assertListEqualsIgnoreOrder(List.of(copy), db.select(Condition.eq(tag3, "value3")).collect());
        assertListEqualsIgnoreOrder(List.of(copy), db.select(Condition.eq(tag4, "value4")).collect());
        assertListEqualsIgnoreOrder(List.of(copy), db.select(Condition.eq(tag5, "value5")).collect());
    }

    @Test
    public void findFirst() {
        TagDatabase db = TagDatabase.database();
        var tag = Tag.String("key");
        var tag2 = Tag.String("key2");
        var handler = db.newHandler();
        handler.setTag(tag, "value");
        handler.setTag(tag2, "value2");
        var copy = handler.copy();

        var result = db.findFirst(tag, "value").orElseThrow();
        assertEquals(copy.asCompound(), result.asCompound());
    }

    @Test
    public void replaceConstant() {
        TagDatabase db = TagDatabase.database();
        var tag = Tag.Integer("number");
        var compound = NBT.Compound(Map.of("number", NBT.Int(5)));

        db.newHandler().updateContent(compound);
        db.selectAll().operate(Operation.set(tag, 10));

        var result = db.selectAll().collect();
        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getTag(tag));
    }

    @Test
    public void replaceNull() {
        TagDatabase db = TagDatabase.database();
        var tag = Tag.Integer("number");
        var compound = NBT.Compound(Map.of("number", NBT.Int(5)));

        db.newHandler().updateContent(compound);
        db.selectAll().operate(Operation.set(tag, null));

        assertFalse(db.selectAll().collect().isEmpty());
        assertTrue(db.select(Condition.eq(tag, 5)).collect().isEmpty());

    }

    @Test
    public void replaceOperator() {
        TagDatabase db = TagDatabase.database();
        var tag = Tag.Integer("number");

        db.newHandler().setTag(tag, 5);
        db.selectAll().collect().forEach(tagHandler -> tagHandler.updateTag(tag, integer -> integer * 2));

        var result = db.selectAll().collect();
        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getTag(tag));
    }

    @Test
    public void delete() {
        TagDatabase db = TagDatabase.database();
        var tag = Tag.Integer("number");
        var compound = NBT.Compound(Map.of("number", NBT.Int(5)));
        var condition = Condition.eq(tag, 5);

        db.newHandler().updateContent(compound);
        db.select(condition).deleteAll();

        var result = db.select(condition).collect();
        assertTrue(result.isEmpty());
    }

    @Test
    public void intSort() {
        TagDatabase db = TagDatabase.database();
        var tag = Tag.Integer("number");

        var handler2 = db.newHandler();
        var handler3 = db.newHandler();
        var handler1 = db.newHandler();

        handler1.updateContent(NBT.Compound(Map.of("number", NBT.Int(1))));
        handler2.updateContent(NBT.Compound(Map.of("number", NBT.Int(2))));
        handler3.updateContent(NBT.Compound(Map.of("number", NBT.Int(3))));

        var ascending = db.selectAll().collect(Map.of(tag, TagDatabase.SortOrder.ASCENDING), -1);
        assertEquals(List.of(handler1, handler2, handler3), ascending);

        var descending = db.selectAll().collect(Map.of(tag, TagDatabase.SortOrder.DESCENDING), -1);
        assertEquals(List.of(handler3, handler2, handler1), descending);
    }

    @Test
    public void nestedSort() {
        TagDatabase db = TagDatabase.database();
        var tag = Tag.Integer("number").path("path", "path2");

        var handler = db.newHandler();
        var handler2 = db.newHandler();
        var handler3 = db.newHandler();
        var handler4 = db.newHandler();

        handler.setTag(tag, 1);
        handler2.setTag(tag, 2);
        handler3.setTag(tag, 3);
        handler4.setTag(tag, 4);

        var ascending = db.selectAll().collect(Map.of(tag, TagDatabase.SortOrder.ASCENDING), -1);
        assertEquals(List.of(handler, handler2, handler3, handler4), ascending);

        var descending = db.selectAll().collect(Map.of(tag, TagDatabase.SortOrder.DESCENDING), -1);
        assertEquals(List.of(handler4, handler3, handler2, handler), descending);
    }

    @Test
    public void tableDownsize() {
        TagDatabase db = TagDatabase.database();
        var tag = Tag.Integer("number");
        var condition = Condition.eq(tag, 1);
        var selectQuery = db.select(condition);

        var handler = db.newHandler();
        handler.setTag(tag, 1);

        assertEquals(1, selectQuery.collect().size());

        handler.removeTag(tag);
        assertEquals(0, selectQuery.collect().size());
    }

    @Test
    public void entityQuery() {
        var pos = Tag.Structure("pos", Pos.class);

        TagDatabase db = TagDatabase.database();
        var entity1 = db.newHandler();
        var entity2 = db.newHandler();

        // Set positions
        entity1.setTag(pos, new Pos(1, 55, 2));
        entity2.setTag(pos, new Pos(4, 55, 6));

        // Query entities within a radius of 5 blocks from (0, 55, 0)
        var condition = Condition.and(
                Condition.range(Tag.Double("x").path("pos"), -5d, 5d),
                Condition.range(Tag.Double("z").path("pos"), -5d, 5d)
        );
        var entities = db.select(condition).collect();
    }

    @Test
    public void trackRoot() {
        var tag = Tag.Integer("value");

        TagDatabase db = TagDatabase.database();
        var entity = db.newHandler();

        entity.setTag(tag, 1);

        AtomicReference<Integer> ref = new AtomicReference<>(null);
        db.track(tag, (tagHandler, value) -> {
            assertNull(ref.get());
            ref.set(value);
        });

        entity.setTag(tag, 2);
        assertEquals(2, ref.get());

        ref.set(null);
        entity.setTag(tag, 3);
        assertEquals(3, ref.get());
    }

    @Test
    public void trackStruct() {
        var posTag = Tag.Structure("value", Pos.class);

        TagDatabase db = TagDatabase.database();
        var entity = db.newHandler();

        entity.setTag(posTag, new Pos(1, 1, 1));

        AtomicReference<Pos> ref = new AtomicReference<>(null);
        db.track(posTag, (tagHandler, value) -> {
            assertNull(ref.get());
            ref.set(value);
        });

        entity.setTag(posTag, new Pos(2, 2, 2));
        assertEquals(new Pos(2, 2, 2), ref.get());

        ref.set(null);
        entity.setTag(posTag, new Pos(3, 3, 3));
        assertEquals(new Pos(3, 3, 3), ref.get());
    }

    @Test
    public void trackUp() {
        var posTag = Tag.Structure("value", Pos.class);
        var xTag = Tag.Double("x").path("value");

        TagDatabase db = TagDatabase.database();
        var entity = db.newHandler();

        entity.setTag(posTag, new Pos(1, 1, 1));

        AtomicReference<Double> ref = new AtomicReference<>(null);
        db.track(xTag, (tagHandler, value) -> {
            assertNull(ref.get());
            ref.set(value);
        });

        entity.setTag(posTag, new Pos(2, 2, 2));
        assertEquals(2d, ref.get());

        ref.set(null);
        entity.setTag(posTag, new Pos(3, 3, 3));
        assertEquals(3, ref.get());
    }

    @Test
    public void trackDown() {
        var posTag = Tag.Structure("value", Pos.class);
        var xTag = Tag.Double("x").path("value");

        TagDatabase db = TagDatabase.database();
        var entity = db.newHandler();

        entity.setTag(posTag, new Pos(1, 1, 1));

        AtomicReference<Pos> ref = new AtomicReference<>(null);
        db.track(posTag, (tagHandler, value) -> {
            assertNull(ref.get());
            ref.set(value);
        });

        entity.setTag(xTag, 2d);
        assertEquals(new Pos(2, 1, 1), ref.get());

        ref.set(null);
        entity.setTag(xTag, 3d);
        assertEquals(new Pos(3, 1, 1), ref.get());
    }

    public static void assertListEqualsIgnoreOrder(List<TagHandler> expected, List<TagHandler> actual) {
        var expectedCompound = expected.stream().map(TagHandler::asCompound).toList();
        var actualCompound = actual.stream().map(TagHandler::asCompound).toList();
        assertEquals(new HashSet<>(expectedCompound), new HashSet<>(actualCompound));
    }
}
