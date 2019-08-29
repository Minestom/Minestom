package fr.themode.minestom.entity.demo;

import fr.themode.minestom.Main;
import fr.themode.minestom.entity.EntityCreature;
import fr.themode.minestom.entity.EntityType;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.utils.Position;
import net.tofweb.starlite.*;

public class ChickenCreature extends EntityCreature {

    private Path path;
    private int counter;
    private Position target;
    private long lastTeleport;
    private long wait = 500;

    public ChickenCreature() {
        super(EntityType.CHICKEN);
    }

    @Override
    public void update() {
        super.update();
        float speed = 0.125f;

        /*if (hasPassenger()) {
            Entity passenger = getPassengers().iterator().next();
            if (passenger instanceof Player) {
                Player player = (Player) passenger;
                float sideways = player.getVehicleSideways();
                float forward = player.getVehicleForward();
                if (sideways == 0f && forward == 0f)
                    return;

                float yaw = player.getYaw();
                yaw %= 360;
                if (forward > 0) {
                    forward = yaw * forward;
                } else {
                    forward = yaw - forward * 180;
                }
                sideways = yaw - sideways * 90;
                yaw = (forward + sideways) / 2 % 360;
                System.out.println("test: " + forward + " : " + sideways);
                double radian = Math.toRadians(yaw + 90);
                double cos = Math.cos(radian);
                double sin = Math.sin(radian);
                //System.out.println(sideways + " : " + forward);
                //System.out.println(cos + " : " + sin);
                double x = cos;
                double z = sin;
                move(x * speed, 0, z * speed);
            }
        }*/

        if (path == null) {
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
        }
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
