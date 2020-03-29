package fr.themode.minestom.entity.vehicle;

public class PlayerVehicleInformation {

    private float sideways;
    private float forward;
    private boolean jump;
    private boolean unmount;

    public float getSideways() {
        return sideways;
    }

    public float getForward() {
        return forward;
    }

    public boolean shouldJump() {
        return jump;
    }

    public boolean shouldUnmount() {
        return unmount;
    }

    public void refresh(float sideways, float forward, boolean jump, boolean unmount) {
        this.sideways = sideways;
        this.forward = forward;
        this.jump = jump;
        this.unmount = unmount;
    }
}
