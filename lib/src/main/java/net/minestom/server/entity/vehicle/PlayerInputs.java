package net.minestom.server.entity.vehicle;

public class PlayerInputs {

    private boolean forward;
    private boolean backward;
    private boolean left;
    private boolean right;
    private boolean jump;
    private boolean shift;
    private boolean sprint;

    public boolean forward() {
        return forward;
    }

    public boolean backward() {
        return backward;
    }

    public boolean left() {
        return left;
    }

    public boolean right() {
        return right;
    }

    public boolean jump() {
        return jump;
    }

    public boolean shift() {
        return shift;
    }

    public boolean sprint() {
        return sprint;
    }

    public void refresh(boolean forward, boolean backward, boolean left, boolean right, boolean jump, boolean shift, boolean sprint) {
        this.forward = forward;
        this.backward = backward;
        this.left = left;
        this.right = right;
        this.jump = jump;
        this.shift = shift;
        this.sprint = sprint;
    }
}
