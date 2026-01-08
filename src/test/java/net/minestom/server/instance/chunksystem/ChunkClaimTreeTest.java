package net.minestom.server.instance.chunksystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChunkClaimTreeTest {
    private static final ChunkClaimTree.CompleteEntry player1S = new ChunkClaimTree.CompleteEntry(10 - 32, -200 - 32, 10 + 32, -200 + 32, new ChunkClaimTree.Entry(50, ChunkClaim.Shape.SQUARE));
    private static final ChunkClaimTree.CompleteEntry player1C = new ChunkClaimTree.CompleteEntry(10 - 32, -200 - 32, 10 + 32, -200 + 32, new ChunkClaimTree.Entry(50, ChunkClaim.Shape.CIRCLE));
    private static final ChunkClaimTree.CompleteEntry player1D = new ChunkClaimTree.CompleteEntry(10 - 32, -200 - 32, 10 + 32, -200 + 32, new ChunkClaimTree.Entry(50, ChunkClaim.Shape.DIAMOND));
    private static final ChunkClaimTree.CompleteEntry player2 = new ChunkClaimTree.CompleteEntry(11 - 32, -199 - 32, 11 + 32, -199 + 32, new ChunkClaimTree.Entry(50, ChunkClaim.Shape.CIRCLE));
    private static final ChunkClaimTree.CompleteEntry player3 = new ChunkClaimTree.CompleteEntry(30 - 32, -200 - 32, 30 + 32, -200 + 32, new ChunkClaimTree.Entry(50, ChunkClaim.Shape.CIRCLE));
    private static final ChunkClaimTree.CompleteEntry player4 = new ChunkClaimTree.CompleteEntry(50 - 16, -200 - 16, 50 + 16, -200 + 16, new ChunkClaimTree.Entry(50, ChunkClaim.Shape.CIRCLE));
    private static final ChunkClaimTree.CompleteEntry player5 = new ChunkClaimTree.CompleteEntry(10 - 32, 100 - 32, 10 + 32, 100 + 32, new ChunkClaimTree.Entry(50, ChunkClaim.Shape.CIRCLE));
    private ChunkClaimTree tree;

    @BeforeEach
    void setup() {
        tree = new ChunkClaimTree();
        tree.insert(player1S);
        tree.insert(player1C);
        tree.insert(player1D);
        tree.insert(player2);
        tree.insert(player3);
        tree.insert(player4);
        tree.insert(player5);
    }

    @Test
    void validateFindPlayersShape() {
        var entries = tree.findEntries(10 + 17, -200 + 17);

        assertEquals(4, entries.size());
        assertTrue(entries.contains(player1S));
        assertTrue(entries.contains(player1C));
        assertTrue(entries.contains(player2));
        assertTrue(entries.contains(player3));
    }

    @Test
    void validateFindPlayersShape2() {
        var entries = tree.findEntries(10 - 28, -200 + 28);

        assertEquals(1, entries.size());
        assertTrue(entries.contains(player1S));
    }

    @Test
    void validateFindPlayersShape3() {
        var entries = tree.findEntries(10 + 28, -200 + 28);

        assertEquals(2, entries.size());
        assertTrue(entries.contains(player1S));
        assertTrue(entries.contains(player3));
    }

    @Test
    void validateRemovedFindPlayersShape() {
        tree.delete(player1S);
        var entries = tree.findEntries(10 + 17, -200 + 17);

        assertEquals(3, entries.size());
        assertTrue(entries.contains(player1C));
        assertTrue(entries.contains(player2));
        assertTrue(entries.contains(player3));
    }
    
    @Test
    void testRemoveAll() {
        tree.delete(player1S);
        tree.delete(player1C);
        tree.delete(player1D);
        tree.delete(player2);
        tree.delete(player3);
        tree.delete(player4);
        tree.delete(player5);
    }

    @Test
    void validateDuplicateEntries() {
        tree.insert(player1C);
        tree.insert(player1C);
        var entries = tree.findEntries(10 + 17, -200 + 17);

        assertEquals(6, entries.size());
        entries.remove(player1C);
        entries.remove(player1C);
        assertTrue(entries.contains(player1S));
        assertTrue(entries.contains(player1C));
        assertTrue(entries.contains(player2));
        assertTrue(entries.contains(player3));
    }

    @Test
    void validateFindPlayersBasic() {
        var entries = tree.findEntries(10, -200);

        assertEquals(5, entries.size());
        assertTrue(entries.contains(player1S));
        assertTrue(entries.contains(player1C));
        assertTrue(entries.contains(player1D));
        assertTrue(entries.contains(player2));
        assertTrue(entries.contains(player3));
    }

    @Test
    void validateFindPlayersBasic2() {
        var entries = tree.findEntries(40, -200);

        assertEquals(6, entries.size());
        assertTrue(entries.contains(player1S));
        assertTrue(entries.contains(player1C));
        assertTrue(entries.contains(player1D));
        assertTrue(entries.contains(player2));
        assertTrue(entries.contains(player3));
        assertTrue(entries.contains(player4));
    }
}
