package net.minestom.demo.block.placement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class FencePlacementRule extends BlockPlacementRule {

    public FencePlacementRule(Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        var instance = placementState.instance();
        var position = placementState.placePosition();

        return calculateConnections(instance, position);
    }

    @Override
    public @NotNull Block blockUpdate(@NotNull UpdateState updateState) {
        var instance = updateState.instance();
        var position = updateState.blockPosition();

        return calculateConnections(instance, position);
    }

    @NotNull
    private Block calculateConnections(Block.Getter instance, Point position) {
        Map<String, String> connections = new HashMap<>(
            Map.of(
                "north", instance.getBlock(position.add(0, 0, -1)).isSolid() ? "true" : "false",
                "south", instance.getBlock(position.add(0, 0, 1)).isSolid() ? "true" : "false",
                "west", instance.getBlock(position.add(-1, 0, 0)).isSolid() ? "true" : "false",
                "east", instance.getBlock(position.add(1, 0, 0)).isSolid() ? "true" : "false"
            )
        );

        return block.withProperties(
                connections
        );
    }

    @Override
    public @NotNull Vec[] updateShape() {
        // Fences have a specific shape update logic, they connect to adjacent fence blocks
        return new Vec[]{
                new Vec(1, 0, 0), // east
                new Vec(-1, 0, 0), // west
                new Vec(0, 0, 1), // south
                new Vec(0, 0, -1) // north
        };
    }

    @Override
    public boolean considerUpdate(@NotNull Vec offset, @NotNull Block block) {
        // Fences should only consider updates from solid blocks that are next to them
        System.out.println(offset);
        System.out.println(block);
        return (offset.equals(new Vec(1, 0, 0)) || // east
               offset.equals(new Vec(-1, 0, 0)) || // west
               offset.equals(new Vec(0, 0, 1)) || // south
               offset.equals(new Vec(0, 0, -1))) && // north
                block.isSolid() || block.isAir(); // ensure the block is solid
    }
}
