package net.minestom.server.inventory.click;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.entity.GameMode;
import org.junit.jupiter.api.Test;

import static net.minestom.server.inventory.click.ClickUtils.*;
import static net.minestom.server.network.packet.client.play.ClientClickWindowPacket.ClickType.*;

public class ClickPreprocessorTest {

    @Test
    public void testPickupType() {
        assertProcessed(new Click.Info.LeftDropCursor(), clickPacket(PICKUP, 1, 0, -999));
        assertProcessed(new Click.Info.RightDropCursor(), clickPacket(PICKUP, 1, 1, -999));
        assertProcessed(new Click.Info.MiddleDropCursor(), clickPacket(CLONE, 1, 2, -999));

        assertProcessed(new Click.Info.Left(0), clickPacket(PICKUP, 1, 0, 0));
        assertProcessed(new Click.Info.Left(SIZE), clickPacket(PICKUP, 1, 0, 5));
        assertProcessed(null, clickPacket(PICKUP, 1, 0, 99));

        assertProcessed(new Click.Info.Right(0), clickPacket(PICKUP, 1, 1, 0));
        assertProcessed(new Click.Info.Right(SIZE), clickPacket(PICKUP, 1, 1, 5));
        assertProcessed(null, clickPacket(PICKUP, 1, 1, 99));

        assertProcessed(null, clickPacket(PICKUP, 1, -1, 0));
        assertProcessed(null, clickPacket(PICKUP, 1, 2, 0));
    }

    @Test
    public void testQuickMoveType() {
        assertProcessed(new Click.Info.LeftShift(0), clickPacket(QUICK_MOVE, 1, 0, 0));
        assertProcessed(new Click.Info.LeftShift(SIZE), clickPacket(QUICK_MOVE, 1, 0, 5));
        assertProcessed(null, clickPacket(QUICK_MOVE, 1, 0, -1));
    }

    @Test
    public void testSwapType() {
        assertProcessed(null, clickPacket(SWAP, 1, 0, -1));
        assertProcessed(new Click.Info.HotbarSwap(0, 2), clickPacket(SWAP, 1, 0, 2));
        assertProcessed(new Click.Info.HotbarSwap(8, 2), clickPacket(SWAP, 1, 8, 2));
        assertProcessed(new Click.Info.OffhandSwap(2), clickPacket(SWAP, 1, 40, 2));

        assertProcessed(null, clickPacket(SWAP, 1, 9, 2));
        assertProcessed(null, clickPacket(SWAP, 1, 39, 2));
    }

    @Test
    public void testCloneType() {
        var player = createPlayer();
        player.setGameMode(GameMode.CREATIVE);

        assertProcessed(null, clickPacket(CLONE, 1, 0, 0));
        assertProcessed(player, new Click.Info.Middle(0), clickPacket(CLONE, 1, 0, 0));
        assertProcessed(player, null, clickPacket(CLONE, 1, 0, -1));
    }

    @Test
    public void testThrowType() {
        assertProcessed(new Click.Info.DropSlot(0, true), clickPacket(THROW, 1, 1, 0));

        assertProcessed(new Click.Info.DropSlot(0, false), clickPacket(THROW, 1, 0, 0));
        assertProcessed(new Click.Info.DropSlot(0, true), clickPacket(THROW, 1, 1, 0));

        assertProcessed(new Click.Info.DropSlot(1, false), clickPacket(THROW, 1, 0, 1));
        assertProcessed(new Click.Info.DropSlot(1, true), clickPacket(THROW, 1, 1, 1));
    }

    @Test
    public void testQuickCraft() {
        var processor = createPreprocessor();
        var player = createPlayer();

        assertProcessed(player, null, clickPacket(QUICK_CRAFT, 1, 8, 0));
        assertProcessed(player, null, clickPacket(QUICK_CRAFT, 1, 9, 0));
        assertProcessed(player, null, clickPacket(QUICK_CRAFT, 1, 10, 0));

        player.setGameMode(GameMode.CREATIVE);

        assertProcessed(processor, player, null, clickPacket(QUICK_CRAFT, 1, 0, 0));
        assertProcessed(processor, player, null, clickPacket(QUICK_CRAFT, 1, 1, 0));
        assertProcessed(processor, player, null, clickPacket(QUICK_CRAFT, 1, 1, 1));
        assertProcessed(processor, player, new Click.Info.LeftDrag(IntList.of(0, 1)), clickPacket(QUICK_CRAFT, 1, 2, 0));

        assertProcessed(processor, player, null, clickPacket(QUICK_CRAFT, 1, 4, 0));
        assertProcessed(processor, player, null, clickPacket(QUICK_CRAFT, 1, 5, 0));
        assertProcessed(processor, player, null, clickPacket(QUICK_CRAFT, 1, 5, 1));
        assertProcessed(processor, player, new Click.Info.RightDrag(IntList.of(0, 1)), clickPacket(QUICK_CRAFT, 1, 6, 0));

        assertProcessed(processor, player, null, clickPacket(QUICK_CRAFT, 1, 8, 0));
        assertProcessed(processor, player, null, clickPacket(QUICK_CRAFT, 1, 9, 0));
        assertProcessed(processor, player, null, clickPacket(QUICK_CRAFT, 1, 9, 1));
        assertProcessed(processor, player, new Click.Info.MiddleDrag(IntList.of(0, 1)), clickPacket(QUICK_CRAFT, 1, 10, 0));
    }

}
