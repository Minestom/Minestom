package net.minestom.server.extras.blockplacement;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerBlockUpdateNeighborEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public class BlockPlaceMechanicChestType {

    public static void onPlace(Block block, PlayerBlockPlaceEvent event) {
        event.setBlock(updateFromNeighbors(event.getBlock(), event.getInstance(), event.getBlockPosition()));
    }

    public static void onNeighbor(Block block, PlayerBlockUpdateNeighborEvent event) {
        event.setBlock(updateFromNeighbors(event.getBlock(), event.getInstance(), event.getBlockPosition()));
    }

    private static @NotNull Block updateFromNeighbors(Block state, Instance instance, Point position) {
        String oldState = state.getProperty("type");
        boolean isSingle = "single".equals(oldState);
        boolean isLeft = "left".equals(oldState);
        boolean isRight = "right".equals(oldState);

        state = state.withProperty("type", "single");

        String facing = state.getProperty("facing");

        Point left;
        Point right;

        switch(facing) {
            case "east": {
                left = new Vec(0, 0, 1);
                right = new Vec(0, 0, -1);
                break;
            }
            case "west": {
                left = new Vec(0, 0, -1);
                right = new Vec(0, 0, 1);
                break;
            }
            case "north": {
                left = new Vec(1, 0, 0);
                right = new Vec(-1, 0, 0);
                break;
            }
            case "south": {
                left = new Vec(-1, 0, 0);
                right = new Vec(1, 0, 0);
                break;
            }
            default: {
                return state;
            }
        }

        Block leftBlock = instance.getBlock(
                position.blockX() + left.blockX(),
                position.blockY() + left.blockY(),
                position.blockZ() + left.blockZ()
        );

        if(leftBlock.compare(state)) {
            if(isSingle || isLeft) {
                String leftType = leftBlock.getProperty("type");

                if("single".equals(leftType) || "right".equals(leftType)) {
                    state = state.withProperty("type", "left");
                    return state;
                }
            }
        } else if(isLeft) {
            state = state.withProperty("type", "single");
            return state;
        }

        Block rightBlock = instance.getBlock(
                position.blockX() + right.blockX(),
                position.blockY() + right.blockY(),
                position.blockZ() + right.blockZ()
        );

        if(rightBlock.compare(state)) {
            if(isSingle || isRight) {
                String rightLeft = rightBlock.getProperty("type");

                if("single".equals(rightLeft) || "left".equals(rightLeft)) {
                    state = state.withProperty("type", "right");
                    return state;
                }
            }
        } else if(isRight) {
            state = state.withProperty("type", "single");
            return state;
        }

        return state;
    }

}
