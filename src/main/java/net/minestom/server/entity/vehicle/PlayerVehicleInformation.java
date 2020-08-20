package net.minestom.server.entity.vehicle;

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

    /**
     * Refresh internal data
     *
     * @param sideways the new sideways value
     * @param forward  the new forward value
     * @param jump     the new jump value
     * @param unmount  the new unmount value
     */
    public void refresh(float sideways, float forward, boolean jump, boolean unmount) {
        this.sideways = sideways;
        this.forward = forward;
        this.jump = jump;
        this.unmount = unmount;
    }
}
