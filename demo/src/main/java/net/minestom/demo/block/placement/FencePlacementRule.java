package net.minestom.demo.block.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockChange;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FencePlacementRule extends BlockPlacementRule {

    public FencePlacementRule(Block block) {
        super(block);
    }

    @Override
    public @NotNull Block blockPlace(@NotNull BlockChange blockChange) {
        var instance = blockChange.instance();
        var position = blockChange.blockPosition();

        return calculateConnections(instance, position);
    }

    @Override
    public @NotNull Block blockUpdate(@NotNull BlockChange updateState) {
        var instance = updateState.instance();
        var position = updateState.blockPosition();

        return calculateConnections(instance, position);
    }

    @NotNull
    private Block calculateConnections(Block.Getter instance, Point position) {
        Map<String, String> connections = new HashMap<>();

        if (!(instance instanceof Instance realInstance)) return this.block;

        realInstance.loadChunk(position.add(0, 0, -1)).thenAccept(chunk -> connections.put("north", instance.getBlock(position.add(0, 0, -1)).isSolid() ? "true" : "false"));
        realInstance.loadChunk(position.add(0, 0, 1)).thenAccept(chunk -> connections.put("south", instance.getBlock(position.add(0, 0, 1)).isSolid() ? "true" : "false"));
        realInstance.loadChunk(position.add(-1, 0, 0)).thenAccept(chunk -> connections.put("west", instance.getBlock(position.add(-1, 0, 0)).isSolid() ? "true" : "false"));
        realInstance.loadChunk(position.add(1, 0, 0)).thenAccept(chunk -> connections.put("east", instance.getBlock(position.add(1, 0, 0)).isSolid() ? "true" : "false"));
        realInstance.loadChunk(position);

        return block.withProperties(
                connections
        );
    }

    @Override
    public @NotNull List<Vec> updateShape() {
        return List.of(
                Direction.NORTH.vec(),
                Direction.SOUTH.vec(),
                Direction.EAST.vec(),
                Direction.WEST.vec()
        );
    }

    @Override
    public boolean considerUpdate(@NotNull Vec offset, @NotNull Block block) {
        // Fences should only consider updates from solid blocks that are next to them
        return super.considerUpdate(offset, block) && block.isSolid() || block.isAir(); // ensure the block is solid
    }
}