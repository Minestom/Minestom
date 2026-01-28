package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;

/**
 * Called when a player's input state changes.
 * This is raw input data and does not take into account any game mechanics.
 * <br>
 * For example, this event may say a player has their jump key held down
 * even if they are in a situation where they can not actually jump.
 */
public class PlayerInputEvent implements PlayerInstanceEvent {

    private final Player player;

    private final boolean oldForward;
    private final boolean oldBackward;
    private final boolean oldLeft;
    private final boolean oldRight;
    private final boolean oldJump;
    private final boolean oldShift;
    private final boolean oldSprint;

    public PlayerInputEvent(Player player, boolean oldForward, boolean oldBackward, boolean oldLeft, boolean oldRight, boolean oldJump, boolean oldShift, boolean oldSprint) {
        this.player = player;
        this.oldForward = oldForward;
        this.oldBackward = oldBackward;
        this.oldLeft = oldLeft;
        this.oldRight = oldRight;
        this.oldJump = oldJump;
        this.oldShift = oldShift;
        this.oldSprint = oldSprint;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    // Movement keys

    public boolean isHoldingForwardKey() {
        return this.player.inputs().forward();
    }

    public boolean hasPressedForwardKey() {
        return !this.oldForward && this.player.inputs().forward();
    }

    public boolean hasReleasedForwardKey() {
        return this.oldForward && !this.player.inputs().forward();
    }

    public boolean isHoldingBackwardKey() {
        return this.player.inputs().backward();
    }

    public boolean hasPressedBackwardKey() {
        return !this.oldBackward && this.player.inputs().backward();
    }

    public boolean hasReleasedBackwardKey() {
        return this.oldBackward && !this.player.inputs().backward();
    }

    public boolean isHoldingLeftKey() {
        return this.player.inputs().left();
    }

    public boolean hasPressedLeftKey() {
        return !this.oldLeft && this.player.inputs().left();
    }

    public boolean hasReleasedLeftKey() {
        return this.oldLeft && !this.player.inputs().left();
    }

    public boolean isHoldingRightKey() {
        return this.player.inputs().right();
    }

    public boolean hasPressedRightKey() {
        return !this.oldRight && this.player.inputs().right();
    }

    public boolean hasReleasedRightKey() {
        return this.oldRight && !this.player.inputs().right();
    }

    // Action Keys

    public boolean isHoldingJumpKey() {
        return this.player.inputs().jump();
    }

    public boolean hasPressedJumpKey() {
        return !this.oldJump && this.player.inputs().jump();
    }

    public boolean hasReleasedJumpKey() {
        return this.oldJump && !this.player.inputs().jump();
    }

    public boolean isHoldingShiftKey() {
        return this.player.inputs().shift();
    }

    public boolean hasPressedShiftKey() {
        return !this.oldShift && this.player.inputs().shift();
    }

    public boolean hasReleasedShiftKey() {
        return this.oldShift && !this.player.inputs().shift();
    }

    public boolean isHoldingSprintKey() {
        return this.player.inputs().sprint();
    }

    public boolean hasPressedSprintKey() {
        return !this.oldSprint && this.player.inputs().sprint();
    }

    public boolean hasReleasedSprintKey() {
        return this.oldSprint && !this.player.inputs().sprint();
    }

}
