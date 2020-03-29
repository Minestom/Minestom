package fr.themode.demo.entity;

import fr.themode.minestom.entity.Entity;
import fr.themode.minestom.entity.EntityCreature;
import fr.themode.minestom.entity.EntityType;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.entity.pathfinding.EntityPathFinder;
import fr.themode.minestom.entity.vehicle.PlayerVehicleInformation;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.Position;
import fr.themode.minestom.utils.Vector;

import java.util.LinkedList;

public class ChickenCreature extends EntityCreature {

    private EntityPathFinder pathFinder = new EntityPathFinder(this);
    private LinkedList<BlockPosition> blockPositions;
    private Position targetPosition;

    public ChickenCreature(Position defaultPosition) {
        super(EntityType.CHICKEN, defaultPosition);
        setBoundingBox(0.4f, 0.7f, 0.4f);
    }

    public ChickenCreature() {
        this(new Position());
    }

    @Override
    public void spawn() {
        //Player player = MinecraftServer.getConnectionManager().getPlayer("TheMode911");
        //moveTo(player.getPosition().add(2, 0, 2));
    }

    @Override
    public void update() {
        super.update();
        float speed = 0.075f;

        if (hasPassenger()) {
            Entity passenger = getPassengers().iterator().next();
            if (passenger instanceof Player) {
                Player player = (Player) passenger;
                PlayerVehicleInformation vehicleInformation = player.getVehicleInformation();
                float sideways = vehicleInformation.getSideways();
                float forward = vehicleInformation.getForward();

                boolean jump = vehicleInformation.shouldJump();
                boolean unmount = vehicleInformation.shouldUnmount();

                if (jump && isOnGround()) {
                    setVelocity(new Vector(0, 6, 0), 500);
                }

                boolean updateView = forward > 0;
                if (sideways == 0f && forward == 0f)
                    return;

                float yaw = player.getPosition().getYaw();
                yaw %= 360;

                sideways = yaw + (updateView ? -sideways * 90 : sideways * 90);

                if (forward > 0) {
                    forward = yaw * forward;
                } else {
                    forward = yaw + forward * 360;
                }
                yaw = (forward + sideways) / 2 % 360;
                double radian = Math.toRadians(yaw + 90);
                double cos = Math.cos(radian);
                double sin = Math.sin(radian);
                float x = (float) cos * speed;
                float z = (float) sin * speed;

                /*BlockPosition blockPosition = getPosition().toBlockPosition();
                BlockPosition belowPosition = blockPosition.clone().add(0, -1, 0);
                BlockPosition upPosition = blockPosition.clone().add(0, 1, 0);
                boolean airCurrent = getInstance().getBlockId(blockPosition) == 0;
                boolean airBelow = getInstance().getBlockId(belowPosition) == 0;
                boolean airUp = getInstance().getBlockId(upPosition) == 0;
                boolean shouldJump = false;
                int boundingBoxY = (int) Math.ceil(getBoundingBox().getY());
                for (int i = 0; i < boundingBoxY; i++) {

                }*/

                //System.out.println("test: "+player.isVehicleJump());
                //System.out.println(getInstance().getBlockId(getPosition().toBlockPosition()));

                move(x, 0, z, updateView);
            }
        } else {
            //move(randomX * speed, 0, randomZ * speed, true);
        }

        if (blockPositions != null) {
            if (targetPosition != null) {
                float distance = getPosition().getDistance(targetPosition);
                if (distance < 0.2f) {
                    setNextPathPosition();
                    System.out.println("END TARGET");
                } else {
                    moveTowards(targetPosition, speed);
                    System.out.println("MOVE TOWARD " + targetPosition);
                }
            }
        }
    }

    private void setNextPathPosition() {
        BlockPosition blockPosition = blockPositions.pollFirst();

        if (blockPosition == null) {
            this.blockPositions = null;
            this.targetPosition = null;
            return;
        }

        this.targetPosition = blockPosition.toPosition();
    }

    private void moveTo(Position position) {
        this.blockPositions = pathFinder.getPath(position);
        setNextPathPosition();
    }
}
