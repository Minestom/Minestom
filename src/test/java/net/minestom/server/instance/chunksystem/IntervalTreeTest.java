package net.minestom.server.instance.chunksystem;

import net.minestom.server.instance.chunksystem.IntervalTree.Node;
import net.minestom.server.utils.Range;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class IntervalTreeTest {
    private static IntervalTree<Integer> degenerateTree;
    private static IntervalTree<Integer> degenerateTreeCopy;
    private static IntervalTree<Integer> largeTree;
    private static IntervalTree<Integer> largeTreeCopy;
    private static ArrayList<Range.Int> largeTreeValidEntries;

    @BeforeAll
    static void setupSingletonTree() {

    }

    @BeforeAll
    static void setupDegenerateTree() {
        degenerateTree = new IntervalTree<>();
        for (var i = 0; i < 10000; i++) {
            int finalI = i;
            var val = degenerateTree.insertOrGet(i, i + 50, () -> finalI);
            assertEquals(i, val);
        }
        degenerateTreeCopy = degenerateTree.copy();
    }

    @BeforeAll
    static void setupLargeTree() {
        largeTree = new IntervalTree<>();
        largeTreeValidEntries = new ArrayList<>();
        var random = new Random(105976);
        var len = 100_000;
        for (var i = 0; i < len; i++) {
            var num = random.nextInt(len * 2);
            var state = largeTree.state();
            largeTree.insertOrGet(num, num + 50, () -> num);
            if (largeTree.modifiedSince(state)) {
                if (largeTreeValidEntries.size() < 1000) largeTreeValidEntries.add(new Range.Int(num, num + 50));
            }
        }
        largeTreeCopy = largeTree.copy();
    }

    @Test
    void validateSmallTreeCopy() {
        var smallTree = new IntervalTree<Integer>();
        smallTree.insertOrGet(0, 10, () -> 3);
        var st2 = smallTree.copy();
        assertEquals(smallTree, st2);
    }

    @Test
    void validateLargeTreeCopySameAsLargeTree() {
        assertEquals(largeTree, largeTreeCopy, "Large tree copy must equal large tree");
    }

    @Test
    void validateDegenerateTreeCopySameAsDegenerateTree() {
        assertEquals(degenerateTree, degenerateTreeCopy, "Degenerate tree copy must be equal to degenerate tree");
    }

    @Test
    void validateDataLargeTree() {
        validateData(largeTree);
    }

    @Test
    void validateDataLargeTreeCopy() {
        validateData(largeTreeCopy);
    }

    @Test
    void validateDataDegenerateTree() {
        validateData(degenerateTree);
    }

    @Test
    void validateDataDegenerateTreeCopy() {
        validateData(degenerateTreeCopy);
    }

    @Test
    void validateLinksLargeTree() {
        validateLinks(largeTree);
    }

    @Test
    void validateLinksLargeTreeCopy() {
        validateLinks(largeTreeCopy);
    }

    @Test
    void validateLinksDegenerateTree() {
        validateLinks(degenerateTree);
    }

    @Test
    void validateLinksDegenerateTreeCopy() {
        validateLinks(degenerateTreeCopy);
    }

    @Test
    void validateHeightLargeTree() {
        validateHeight(largeTree);
    }

    @Test
    void validateHeightLargeTreeCopy() {
        validateHeight(largeTreeCopy);
    }

    @Test
    void validateHeightDegenerateTree() {
        validateHeight(degenerateTree);
    }

    @Test
    void validateHeightDegenerateTreeCopy() {
        validateHeight(degenerateTreeCopy);
    }

    @Test
    void validateMaxEndLargeTree() {
        validateMaxEnd(largeTree);
    }

    @Test
    void validateMaxEndLargeTreeCopy() {
        validateMaxEnd(largeTreeCopy);
    }

    @Test
    void validateMaxEndDegenerateTree() {
        validateMaxEnd(degenerateTree);
    }

    @Test
    void validateMaxEndDegenerateTreeCopy() {
        validateMaxEnd(degenerateTreeCopy);
    }

    @Test
    void validateLargeTreeCorrectRemoval() {
        var copy = largeTree.copy();
        var prevSize = largeTree.size();
        for (var start : largeTreeValidEntries) {
            assertTrue(copy.delete(start.min(), start.max()));
        }
        assertEquals(prevSize - largeTreeValidEntries.size(), copy.size());
    }

    @Test
    void validateGetLargeTree() {
        for (var range : largeTreeValidEntries) {
            var num = largeTree.get(range.min(), range.max());
            assertEquals(range.min(), num);
        }
    }
    
    /*
    
    record ClaimData(int priority)
    
    int minX, int minY, int maxX, int maxY -> List<ClaimData>
    
     */

    @Test
    void validateSizeLargeTree() {
        validateSize(largeTree);
    }

    @Test
    void validateSizeLargeTreeCopy() {
        validateSize(largeTreeCopy);
    }

    @Test
    void validateSizeDegenerateTree() {
        validateSize(degenerateTree);
    }

    @Test
    void validateSizeDegenerateTreeCopy() {
        validateSize(degenerateTreeCopy);
    }

    @Test
    void validateInsertOrGet() {
        var tree = new IntervalTree<Integer>();
        tree.insertOrGet(0, 10, () -> 5);
        tree.insertOrGet(-5, 10, () -> 4);
        assertEquals(3, tree.insertOrGet(0, 50, () -> 3));
        assertEquals(3, tree.insertOrGet(0, 50, () -> 9));
    }

    @Test
    void testSelection() {
        for (var i = 80; i < 9950; i += 37) {
            var result = degenerateTree.searchNodes(i);
            assertEquals(51, result.size());
            for (var node : result) {
                assertEquals(50, node.end.lastKey() - node.start);
                assertTrue(node.start <= i);
                assertTrue(node.end.lastKey() >= i);
            }
        }
    }

    private static void validateSize(IntervalTree<?> tree) {
        assertEquals(computeSize(tree), tree.size());
    }

    private static int computeSize(IntervalTree<?> tree) {
        return computeSize(tree.getRoot());
    }

    private static int computeSize(Node<?> node) {
        if (node == null) return 0;
        return computeSize(node.left) + 1 + computeSize(node.right);
    }

    private static void validateHeight(IntervalTree<?> tree) {
        var height = tree.height();
        var size = tree.size();
        var expectedMaxHeight = 2 * Math.log(size + 1) / Math.log(2);
        assertTrue(height <= expectedMaxHeight, "Height " + height + " must be less than or equal to expected max height " + (int) Math.ceil(expectedMaxHeight));
    }

    private static void validateMaxEnd(IntervalTree<?> tree) {
        validateMaxEnd(tree.getRoot());
    }

    private static int validateMaxEnd(Node<?> node) {
        if (node == null) return Integer.MIN_VALUE;
        var left = validateMaxEnd(node.left);
        var right = validateMaxEnd(node.right);
        var maxEnd = Math.max(node.end.lastKey(), Math.max(left, right));
        assertEquals(node.maxEnd, maxEnd, "Node maxEnd incorrect");
        return maxEnd;
    }

    private static void validateLinks(IntervalTree<?> tree) {
        var root = tree.getRoot();
        if (root != null) {
            assertNull(root.parent, "Root parent must be null");
        }
        validateLinks(root);
    }

    private static void validateLinks(Node<?> node) {
        if (node == null) return;
        if (node.right != null) {
            assertSame(node, node.right.parent, "right.parent==this");
            validateLinks(node.right);
        }
        if (node.left != null) {
            assertSame(node, node.left.parent, "left.parent==this");
            validateLinks(node.left);
        }
    }

    private static void validateData(IntervalTree<?> tree) {
        validateData(tree.getRoot(), Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    private static void validateData(Node<?> node, int min, int max) {
        if (node == null) return;
        assertTrue(node.start >= min, "Node start must be in range");
        assertTrue(node.start <= max, "Node end must be in range");

        validateData(node.left, min, node.start - 1);
        validateData(node.right, node.start + 1, max);
    }
}
