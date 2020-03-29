package fr.themode.minestom.entity.pathfinding;

import fr.themode.minestom.entity.Entity;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.Position;
import net.tofweb.starlite.*;

import java.util.LinkedList;

public class EntityPathFinder {

    private Entity entity;

    public EntityPathFinder(Entity entity) {
        this.entity = entity;
    }

    public LinkedList<BlockPosition> getPath2(Position target) {
        BlockPosition entityPosition = entity.getPosition().toBlockPosition();
        BlockPosition targetPosition = target.toBlockPosition();

        LinkedList<BlockPosition> blockPositions = new LinkedList<>();

        CellSpace cellSpace = new CellSpace();
        cellSpace.setGoalCell(targetPosition.getX(), targetPosition.getY(), targetPosition.getZ());
        cellSpace.setStartCell(entityPosition.getX(), entityPosition.getY(), entityPosition.getZ());


        CostBlockManager costBlockManager = new CostBlockManager(cellSpace);
        // TODO add blocked cells


        Pathfinder pathfinder = new Pathfinder(costBlockManager);

        Path path = pathfinder.findPath();

        for (Cell cell : path) {
            blockPositions.add(new BlockPosition(cell.getX(), cell.getY(), cell.getZ()));
        }

        return blockPositions;
    }

    public LinkedList<BlockPosition> getPath(Position target) {
        LinkedList<BlockPosition> blockPositions = new LinkedList<>();

        JPS jps = new JPS(entity.getInstance(), entity.getPosition(), target);

        for (Position position : jps.getPath()) {
            blockPositions.add(position.toBlockPosition());
        }

        System.out.println("test: " + blockPositions.size());
        return blockPositions;
    }

}
