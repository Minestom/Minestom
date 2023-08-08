package net.minestom.server.listener;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.item.ItemUpdateStateEvent;
import net.minestom.server.event.player.PlayerCancelDiggingEvent;
import net.minestom.server.event.player.PlayerFinishDiggingEvent;
import net.minestom.server.event.player.PlayerStartDiggingEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket;
import net.minestom.server.network.packet.server.play.AcknowledgeBlockChangePacket;
import net.minestom.server.network.packet.server.play.BlockEntityDataPacket;
import net.minestom.server.utils.block.BlockUtils;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public final class PlayerDiggingListener {

    public static void playerDiggingListener(ClientPlayerDiggingPacket packet, Player player) {
        final ClientPlayerDiggingPacket.Status status = packet.status();
        final Point blockPosition = packet.blockPosition();
        final Instance instance = player.getInstance();
        if (instance == null) return;

        DiggingResult diggingResult = null;
        if (status == ClientPlayerDiggingPacket.Status.STARTED_DIGGING) {
            if (!instance.isChunkLoaded(blockPosition)) return;
            diggingResult = startDigging(player, instance, blockPosition, packet.blockFace());
        } else if (status == ClientPlayerDiggingPacket.Status.CANCELLED_DIGGING) {
            if (!instance.isChunkLoaded(blockPosition)) return;
            diggingResult = cancelDigging(player, instance, blockPosition);
        } else if (status == ClientPlayerDiggingPacket.Status.FINISHED_DIGGING) {
            if (!instance.isChunkLoaded(blockPosition)) return;
            diggingResult = finishDigging(player, instance, blockPosition, packet.blockFace());
        } else if (status == ClientPlayerDiggingPacket.Status.DROP_ITEM_STACK) {
            player.getInventory().handleClick(player, new Click.Info.DropSlot(player.getHeldSlot(), true));
        } else if (status == ClientPlayerDiggingPacket.Status.DROP_ITEM) {
            player.getInventory().handleClick(player, new Click.Info.DropSlot(player.getHeldSlot(), false));
        } else if (status == ClientPlayerDiggingPacket.Status.UPDATE_ITEM_STATE) {
            updateItemState(player);
        } else if (status == ClientPlayerDiggingPacket.Status.SWAP_ITEM_HAND) {
            player.getInventory().handleClick(player, new Click.Info.OffhandSwap(player.getHeldSlot()));
        }
        // Acknowledge start/cancel/finish digging status
        if (diggingResult != null) {
            player.sendPacket(new AcknowledgeBlockChangePacket(packet.sequence()));
            if (!diggingResult.success()) {
                // Refresh block on player screen in case it had special data (like a sign)
                var registry = diggingResult.block().registry();
                if (registry.isBlockEntity()) {
                    final NBTCompound data = BlockUtils.extractClientNbt(diggingResult.block());
                    player.sendPacketToViewersAndSelf(new BlockEntityDataPacket(blockPosition, registry.blockEntityId(), data));
                }
            }
        }
    }

    private static DiggingResult startDigging(Player player, Instance instance, Point blockPosition, BlockFace blockFace) {
        final Block block = instance.getBlock(blockPosition);
        final GameMode gameMode = player.getGameMode();

        // Prevent spectators and check players in adventure mode
        if (shouldPreventBreaking(player, block)) {
            return new DiggingResult(block, false);
        }

        if (gameMode == GameMode.CREATIVE) {
            return breakBlock(instance, player, blockPosition, block, blockFace);
        }

        // Survival digging
        // FIXME: verify mineable tag and enchantment
        final boolean instantBreak = player.isInstantBreak() || block.registry().hardness() == 0;
        if (!instantBreak) {
            PlayerStartDiggingEvent playerStartDiggingEvent = new PlayerStartDiggingEvent(player, block, blockPosition, blockFace);
            EventDispatcher.call(playerStartDiggingEvent);
            return new DiggingResult(block, !playerStartDiggingEvent.isCancelled());
        }
        // Client only send a single STARTED_DIGGING when insta-break is enabled
        return breakBlock(instance, player, blockPosition, block, blockFace);
    }

    private static DiggingResult cancelDigging(Player player, Instance instance, Point blockPosition) {
        final Block block = instance.getBlock(blockPosition);
        PlayerCancelDiggingEvent playerCancelDiggingEvent = new PlayerCancelDiggingEvent(player, block, blockPosition);
        EventDispatcher.call(playerCancelDiggingEvent);
        return new DiggingResult(block, true);
    }

    private static DiggingResult finishDigging(Player player, Instance instance, Point blockPosition, BlockFace blockFace) {
        final Block block = instance.getBlock(blockPosition);

        if (shouldPreventBreaking(player, block)) {
            return new DiggingResult(block, false);
        }

        PlayerFinishDiggingEvent playerFinishDiggingEvent = new PlayerFinishDiggingEvent(player, block, blockPosition);
        EventDispatcher.call(playerFinishDiggingEvent);

        return breakBlock(instance, player, blockPosition, playerFinishDiggingEvent.getBlock(), blockFace);
    }

    private static boolean shouldPreventBreaking(@NotNull Player player, Block block) {
        if (player.getGameMode() == GameMode.SPECTATOR) {
            // Spectators can't break blocks
            return true;
        } else if (player.getGameMode() == GameMode.ADVENTURE) {
            // Check if the item can break the block with the current item
            final ItemStack itemInMainHand = player.getItemInMainHand();
            if (!itemInMainHand.meta().canDestroy(block)) {
                return true;
            }
        }
        return false;
    }

    private static void updateItemState(Player player) {
        LivingEntityMeta meta = player.getLivingEntityMeta();
        if (meta == null || !meta.isHandActive()) return;
        Player.Hand hand = meta.getActiveHand();

        player.refreshEating(null);
        player.triggerStatus((byte) 9);

        ItemUpdateStateEvent itemUpdateStateEvent = player.callItemUpdateStateEvent(hand);
        if (itemUpdateStateEvent == null) {
            player.refreshActiveHand(true, false, false);
        } else {
            final boolean isOffHand = itemUpdateStateEvent.getHand() == Player.Hand.OFF;
            player.refreshActiveHand(itemUpdateStateEvent.hasHandAnimation(),
                    isOffHand, itemUpdateStateEvent.isRiptideSpinAttack());
        }
    }

    private static DiggingResult breakBlock(Instance instance,
                                            Player player,
                                            Point blockPosition, Block previousBlock, BlockFace blockFace) {
        // Unverified block break, client is fully responsible
        final boolean success = instance.breakBlock(player, blockPosition, blockFace);
        final Block updatedBlock = instance.getBlock(blockPosition);
        if (!success) {
            if (previousBlock.isSolid()) {
                final Pos playerPosition = player.getPosition();
                // Teleport the player back if he broke a solid block just below him
                if (playerPosition.sub(0, 1, 0).samePoint(blockPosition)) {
                    player.teleport(playerPosition);
                }
            }
        }
        return new DiggingResult(updatedBlock, success);
    }

    private record DiggingResult(Block block, boolean success) {
    }
}
