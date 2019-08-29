package fr.themode.minestom.entity.demo;

import fr.themode.minestom.entity.Entity;
import fr.themode.minestom.entity.EntityCreature;
import fr.themode.minestom.entity.EntityType;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.utils.Position;
import fr.themode.minestom.utils.Vector;
import net.tofweb.starlite.CellSpace;
import net.tofweb.starlite.CostBlockManager;
import net.tofweb.starlite.Path;
import net.tofweb.starlite.Pathfinder;

public class ChickenCreature extends EntityCreature {

    private Path path;
    private int counter;
    private Position target;
    private long lastTeleport;
    private long wait = 500;

    public ChickenCreature() {
        super(EntityType.CHICKEN);
        setBoundingBox(0.4f, 0.7f, 0.4f);
    }

    @Override
    public void update() {
        super.update();
        float speed = 0.25f;

        if (hasPassenger()) {
            Entity passenger = getPassengers().iterator().next();
            if (passenger instanceof Player) {
                Player player = (Player) passenger;
                float sideways = player.getVehicleSideways();
                float forward = player.getVehicleForward();

                boolean jump = player.isVehicleJump();
                boolean unmount = player.isVehicleUnmount();

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
        }

        /*if (path == null) {
            System.out.println("FIND PATH");
            Player player = Main.getConnectionManager().getPlayer("Raulnil");
            if (player != null) {
                refreshPath(player);
                this.target = null;
            }
        }

        if (target == null) {
            Cell cell = path.pollFirst();
            if (cell != null) {
                this.target = new Position(cell.getX(), cell.getY(), cell.getZ());
                System.out.println("NEW TARGET");
            } else {
                path = null;
            }
        }

        if (path != null && target != null) {
            if (path.isEmpty()) {
                System.out.println("FINISHED PATH");
                path = null;
            } else {
                float distance = getPosition().getDistance(target);
                //System.out.println("DISTANCE: "+distance);
                if (distance <= 1) {
                    System.out.println("RESET TARGET");
                    target = null;
                } else {
                    //System.out.println("WALK");
                    moveTowards(target, speed);
                }
            }
        }*/
    }

    @Override
    public void spawn() {
        // setVelocity(new Vector(0, 1, 0), 3000);
    }

    private void refreshPath(Player target) {
        long time = System.currentTimeMillis();
        Position position = getPosition();
        Position targetPosition = target.getPosition();
        CellSpace space = new CellSpace();
        space.setGoalCell((int) targetPosition.getX(), (int) targetPosition.getY(), (int) targetPosition.getZ());
        space.setStartCell((int) position.getX(), (int) position.getY(), (int) position.getZ());

        CostBlockManager blockManager = new CostBlockManager(space);
        blockManager.blockCell(space.makeNewCell(6, 6, 3));
        blockManager.blockCell(space.makeNewCell(6, 5, 4));

        Pathfinder pathfinder = new Pathfinder(blockManager);

        this.path = pathfinder.findPath();

        System.out.println("PATH FINDING: " + (System.currentTimeMillis() - time) + " ms");
    }
}
