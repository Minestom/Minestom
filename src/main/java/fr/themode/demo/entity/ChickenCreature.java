package fr.themode.demo.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.type.EntityChicken;
import net.minestom.server.entity.vehicle.PlayerVehicleInformation;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;

public class ChickenCreature extends EntityChicken {

    public ChickenCreature(Position defaultPosition) {
        super(defaultPosition);
    }

    @Override
    public void spawn() {

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
                    setVelocity(new Vector(0, 6, 0));
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
            //move(0.5f * speed, 0, 0.5f * speed, true);
        }

        //Player player = MinecraftServer.getConnectionManager().getPlayer("TheMode911");
        //moveTo(player.getPosition().clone());
    }
}
