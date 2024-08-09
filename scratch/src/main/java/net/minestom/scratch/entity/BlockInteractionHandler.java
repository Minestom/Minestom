package net.minestom.scratch.entity;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.AcknowledgeBlockChangePacket;
import net.minestom.server.utils.Direction;

import java.util.List;
import java.util.function.Consumer;

public final class BlockInteractionHandler {
    private final int entityId;
    private final Consumer<ServerPacket.Play> selfConsumer;
    private final Consumer<ServerPacket.Play> localBroadcastConsumer;

    public BlockInteractionHandler(int entityId, Consumer<ServerPacket.Play> selfConsumer, Consumer<ServerPacket.Play> localBroadcastConsumer) {
        this.entityId = entityId;
        this.selfConsumer = selfConsumer;
        this.localBroadcastConsumer = localBroadcastConsumer;
    }

    public List<Action> consume(ClientPlayerDiggingPacket packet, boolean creative) {
        final ClientPlayerDiggingPacket.Status status = packet.status();
        final Point blockPosition = packet.blockPosition();
        selfConsumer.accept(new AcknowledgeBlockChangePacket(packet.sequence()));
        if (status == ClientPlayerDiggingPacket.Status.STARTED_DIGGING) {
            if (creative) {
                return List.of(new Action.BreakBlock(blockPosition));
            }
        } else if (status == ClientPlayerDiggingPacket.Status.FINISHED_DIGGING) {
            return List.of(new Action.BreakBlock(blockPosition));
        }
        return List.of();
    }

    public List<Action> consume(ClientPlayerBlockPlacementPacket packet, ItemStack handItem) {
        final boolean handBlock = handItem.material().registry().block() != null;
        final Point blockPosition = packet.blockPosition();
        if (handBlock) {
            final BlockFace blockFace = packet.blockFace();
            final Direction direction = blockFace.toDirection();
            final Point finalPos = blockPosition.add(direction.normalX(), direction.normalY(), direction.normalZ());
            return List.of(new Action.PlaceBlock(finalPos, packet.hand()));
        } else {
            return List.of(new Action.InteractBlock(blockPosition));
        }
    }

    public sealed interface Action {
        record BreakBlock(Point blockPosition) implements Action {
        }

        record PlaceBlock(Point blockPosition, PlayerHand hand) implements Action {
        }

        record InteractBlock(Point blockPosition) implements Action {
        }
    }
}
