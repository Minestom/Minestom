package fr.themode.minestom.entity.demo;

import fr.themode.minestom.entity.EntityCreature;

public class ChickenCreature extends EntityCreature {

    public ChickenCreature() {
        super(8);
    }

    @Override
    public void update() {
        float speed = 0.05f;

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

        move(0, 0, speed);
    }
}
