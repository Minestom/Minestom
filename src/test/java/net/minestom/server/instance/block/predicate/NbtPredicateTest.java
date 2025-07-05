package net.minestom.server.instance.block.predicate;

import net.kyori.adventure.nbt.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NbtPredicateTest {

    @Test
    void testEmptyStandard() {
        // An empty NBT predicate should return true against any NBT compound
        CompoundBinaryTag standard = CompoundBinaryTag.builder().build();
        CompoundBinaryTag tag1 = CompoundBinaryTag.builder().build();
        CompoundBinaryTag tag2 = CompoundBinaryTag.builder().put("property", IntBinaryTag.intBinaryTag(123)).build();
        CompoundBinaryTag tag3 = CompoundBinaryTag.builder().put("array", ListBinaryTag.builder().add(IntBinaryTag.intBinaryTag(123)).build()).build();

        assertTrue(new NbtPredicate(standard).test(tag1));
        assertTrue(new NbtPredicate(standard).test(tag2));
        assertTrue(new NbtPredicate(standard).test(tag3));

        // If the standard is null, always return true.
        assertTrue(new NbtPredicate(null).test(null));
        assertTrue(new NbtPredicate(null).test(tag2));

        // If the subject is null, always return false (unless the standard is also null).
        assertFalse(new NbtPredicate(standard).test(null));
        assertFalse(new NbtPredicate(tag3).test(null));
    }

    @Test
    void testPrimitive() {
        // Primitive values are expected to match exactly between the standard and the subject
        assertTrue(NbtPredicate.compareNBT(StringBinaryTag.stringBinaryTag("string"), StringBinaryTag.stringBinaryTag("string")));
        assertFalse(NbtPredicate.compareNBT(StringBinaryTag.stringBinaryTag("string"), StringBinaryTag.stringBinaryTag("otherString")));

        assertFalse(NbtPredicate.compareNBT(IntBinaryTag.intBinaryTag(123), StringBinaryTag.stringBinaryTag("123"))); // Tags of different types can never be equal
    }

    @Test
    void testExactMatch() {
        // When a non-array, non-compound tag is specified in an NBT predicate, its value must exactly match
        CompoundBinaryTag standard = CompoundBinaryTag.builder().put("tag", StringBinaryTag.stringBinaryTag("test")).build();

        CompoundBinaryTag empty = CompoundBinaryTag.builder().build();
        CompoundBinaryTag comparison = CompoundBinaryTag.builder().put("tag", StringBinaryTag.stringBinaryTag("test")).build();
        CompoundBinaryTag extra = CompoundBinaryTag.builder()
                .put("tag", StringBinaryTag.stringBinaryTag("test"))
                .put("extra", IntBinaryTag.intBinaryTag(123)).build();
        CompoundBinaryTag extraOnly = CompoundBinaryTag.builder()
                .put("extra", IntBinaryTag.intBinaryTag(123)).build();

        assertFalse(new NbtPredicate(standard).test(empty));
        assertTrue(new NbtPredicate(standard).test(comparison));

        // Adding extra tags is allowed as long as the input has all the tags in the standard
        assertTrue(new NbtPredicate(standard).test(extra));
        assertFalse(new NbtPredicate(standard).test(extraOnly));
    }

    @Test
    void testListComparison() {
        // When a list tag is specified in an NBT predicate, the predicate's input must have a matching item for every item in the "standard" list
        ListBinaryTag list = ListBinaryTag.builder()
                .add(StringBinaryTag.stringBinaryTag("item1"))
                .add(StringBinaryTag.stringBinaryTag("item2"))
                .add(StringBinaryTag.stringBinaryTag("item3"))
                .build();

        CompoundBinaryTag standard = CompoundBinaryTag.builder().put("tag", list).build();

        CompoundBinaryTag partialMatch = CompoundBinaryTag.builder().put("tag", list.remove(0, null)).build();

        CompoundBinaryTag noMatch = CompoundBinaryTag.builder().put("tag", ListBinaryTag.builder()
                .add(StringBinaryTag.stringBinaryTag("item4"))
                .add(StringBinaryTag.stringBinaryTag("item5"))
                .add(StringBinaryTag.stringBinaryTag("item6"))
                .build()
        ).build();

        CompoundBinaryTag reordered = CompoundBinaryTag.builder().put("tag", ListBinaryTag.builder()
                .add(StringBinaryTag.stringBinaryTag("item3"))
                .add(StringBinaryTag.stringBinaryTag("item1"))
                .add(StringBinaryTag.stringBinaryTag("item2"))
                .build()
        ).build();

        CompoundBinaryTag extraItems = CompoundBinaryTag.builder().put("tag", list.add(StringBinaryTag.stringBinaryTag("item4"))).build();

        CompoundBinaryTag empty = CompoundBinaryTag.builder().build();

        assertTrue(new NbtPredicate(standard).test(standard));
        assertTrue(new NbtPredicate(standard).test(reordered)); // The order of list items doesn't matter
        assertFalse(new NbtPredicate(standard).test(noMatch));
        assertFalse(new NbtPredicate(standard).test(empty));

        // Extra items are allowed as long as all items in the standard are contained within the predicate's input
        assertTrue(new NbtPredicate(standard).test(extraItems));
        assertFalse(new NbtPredicate(standard).test(partialMatch));
    }

    @Test
    void testListComparisonWithDifferingSize() {
        // If the standard has a length greater than the length of the subject, always return false, even if it would match otherwise.
        ListBinaryTag list = ListBinaryTag.builder()
                .add(StringBinaryTag.stringBinaryTag("item1"))
                .add(StringBinaryTag.stringBinaryTag("item2"))
                .add(StringBinaryTag.stringBinaryTag("item3"))
                .build();

        CompoundBinaryTag standard = CompoundBinaryTag.builder().put("tag", list.add(StringBinaryTag.stringBinaryTag("item3"))).build();
        CompoundBinaryTag subject = CompoundBinaryTag.builder().put("tag", list).build();

        assertFalse(new NbtPredicate(standard).test(subject));
        assertTrue(new NbtPredicate(subject).test(standard)); // The standard can be smaller than the subject; just not the reverse
    }

    @Test
    void testEmptyList() {
        // If the standard contains an empty list, then the subject must also contain an empty list
        // (otherwise, as long as the subject contains more items than the standard, length doesn't matter)

        CompoundBinaryTag standard = CompoundBinaryTag.builder().put("tag", ListBinaryTag.empty()).build();
        CompoundBinaryTag subject = CompoundBinaryTag.builder().put("tag", ListBinaryTag.builder().add(IntBinaryTag.intBinaryTag(1)).build()).build();

        assertFalse(new NbtPredicate(standard).test(subject));
    }

    @Test
    void testNumberArrayComparison() {
        // We must consider the order and size of byte/int/long arrays when comparing, unlike the way we compare lists

        CompoundBinaryTag standard = CompoundBinaryTag.builder().put("tag", IntArrayBinaryTag.intArrayBinaryTag(1, 2, 3)).build();
        CompoundBinaryTag reordered = CompoundBinaryTag.builder().put("tag", IntArrayBinaryTag.intArrayBinaryTag(3, 1, 2)).build();
        CompoundBinaryTag extraItems = CompoundBinaryTag.builder().put("tag", IntArrayBinaryTag.intArrayBinaryTag(1, 2, 3, 4)).build();

        assertFalse(new NbtPredicate(standard).test(reordered));
        assertFalse(new NbtPredicate(standard).test(extraItems));
    }
}
