package net.minestom.server.inventory.click;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.entity.GameMode;
import org.junit.jupiter.api.Test;

import static net.minestom.server.inventory.click.ClickUtils.*;
import static net.minestom.server.network.packet.client.play.ClientClickWindowPacket.ClickType.*;

public class ClickPreprocessorTest {

    @Test
    public void testPickupType() {
        assertProcessed(new ClickInfo.DropCursor(true), clickPacket(PICKUP, 1, 0, -999));
        assertProcessed(new ClickInfo.LeftClick(0), clickPacket(PICKUP, 1, 0, 0));
        assertProcessed(new ClickInfo.LeftClick(SIZE), clickPacket(PICKUP, 1, 0, 5));
        assertProcessed(null, clickPacket(PICKUP, 1, 0, 99));

        assertProcessed(new ClickInfo.DropCursor(false), clickPacket(PICKUP, 1, 1, -999));
        assertProcessed(new ClickInfo.RightClick(0), clickPacket(PICKUP, 1, 1, 0));
        assertProcessed(new ClickInfo.RightClick(SIZE), clickPacket(PICKUP, 1, 1, 5));
        assertProcessed(null, clickPacket(PICKUP, 1, 1, 99));

        assertProcessed(null, clickPacket(PICKUP, 1, -1, 0));
        assertProcessed(null, clickPacket(PICKUP, 1, 2, 0));
    }

    @Test
    public void testQuickMoveType() {
        assertProcessed(new ClickInfo.ShiftClick(0), clickPacket(QUICK_MOVE, 1, 0, 0));
        assertProcessed(new ClickInfo.ShiftClick(SIZE), clickPacket(QUICK_MOVE, 1, 0, 5));
        assertProcessed(null, clickPacket(QUICK_MOVE, 1, 0, -1));
    }

    @Test
    public void testSwapType() {
        assertProcessed(null, clickPacket(SWAP, 1, 0, -1));
        assertProcessed(new ClickInfo.HotbarSwap(0, 2), clickPacket(SWAP, 1, 0, 2));
        assertProcessed(new ClickInfo.HotbarSwap(8, 2), clickPacket(SWAP, 1, 8, 2));
        assertProcessed(new ClickInfo.OffhandSwap(2), clickPacket(SWAP, 1, 40, 2));

        assertProcessed(null, clickPacket(SWAP, 1, 9, 2));
        assertProcessed(null, clickPacket(SWAP, 1, 39, 2));
    }

    @Test
    public void testCloneType() {
        var player = createPlayer();
        player.setGameMode(GameMode.CREATIVE);

        assertProcessed(null, clickPacket(CLONE, 1, 0, 0));
        assertProcessed(player, new ClickInfo.CopyItem(0), clickPacket(CLONE, 1, 0, 0));
        assertProcessed(player, null, clickPacket(CLONE, 1, 0, -1));
    }

    @Test
    public void testThrowType() {
        assertProcessed(new ClickInfo.DropSlot(0, true), clickPacket(THROW, 1, 1, 0));

        assertProcessed(new ClickInfo.DropSlot(0, false), clickPacket(THROW, 1, 0, 0));
        assertProcessed(new ClickInfo.DropSlot(0, true), clickPacket(THROW, 1, 1, 0));

        assertProcessed(new ClickInfo.DropSlot(1, false), clickPacket(THROW, 1, 0, 1));
        assertProcessed(new ClickInfo.DropSlot(1, true), clickPacket(THROW, 1, 1, 1));
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
        assertProcessed(processor, player, new ClickInfo.DragClick(IntList.of(0, 1), true), clickPacket(QUICK_CRAFT, 1, 2, 0));

        assertProcessed(processor, player, null, clickPacket(QUICK_CRAFT, 1, 4, 0));
        assertProcessed(processor, player, null, clickPacket(QUICK_CRAFT, 1, 5, 0));
        assertProcessed(processor, player, null, clickPacket(QUICK_CRAFT, 1, 5, 1));
        assertProcessed(processor, player, new ClickInfo.DragClick(IntList.of(0, 1), false), clickPacket(QUICK_CRAFT, 1, 6, 0));

        assertProcessed(processor, player, null, clickPacket(QUICK_CRAFT, 1, 8, 0));
        assertProcessed(processor, player, null, clickPacket(QUICK_CRAFT, 1, 9, 0));
        assertProcessed(processor, player, null, clickPacket(QUICK_CRAFT, 1, 9, 1));
        assertProcessed(processor, player, new ClickInfo.CopyCursor(IntList.of(0, 1)), clickPacket(QUICK_CRAFT, 1, 10, 0));
    }

}
