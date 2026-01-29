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
public final class PlayerInputEvent implements PlayerInstanceEvent {

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
        super();
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    // Movement keys

    /**
     * @return true if the player is currently holding the forward key (typically the 'W' key).
     */
    public boolean isHoldingForwardKey() {
        return this.player.inputs().forward();
    }

    /**
     * @return true if the player has just pressed the forward key (typically the 'W' key).
     */
    public boolean hasPressedForwardKey() {
        return !this.oldForward && this.player.inputs().forward();
    }

    /**
     * @return true if the player has just released the forward key (typically the 'W' key).
     */
    public boolean hasReleasedForwardKey() {
        return this.oldForward && !this.player.inputs().forward();
    }

    /**
     * @return true if the player is currently holding the backward key (typically the 'S' key).
     */
    public boolean isHoldingBackwardKey() {
        return this.player.inputs().backward();
    }

    /**
     * @return true if the player has just pressed the backward key (typically the 'S' key).
     */
    public boolean hasPressedBackwardKey() {
        return !this.oldBackward && this.player.inputs().backward();
    }

    /**
     * @return true if the player has just released the backward key (typically the 'S' key).
     */
    public boolean hasReleasedBackwardKey() {
        return this.oldBackward && !this.player.inputs().backward();
    }

    /**
     * @return true if the player is currently holding the left key (typically the 'A' key).
     */
    public boolean isHoldingLeftKey() {
        return this.player.inputs().left();
    }

    /**
     * @return true if the player has just pressed the left key (typically the 'A' key).
     */
    public boolean hasPressedLeftKey() {
        return !this.oldLeft && this.player.inputs().left();
    }

    /**
     * @return true if the player has just released the left key (typically the 'A' key).
     */
    public boolean hasReleasedLeftKey() {
        return this.oldLeft && !this.player.inputs().left();
    }

    /**
     * @return true if the player is currently holding the right key (typically the 'D' key).
     */
    public boolean isHoldingRightKey() {
        return this.player.inputs().right();
    }

    /**
     * @return true if the player has just pressed the right key (typically the 'D' key).
     */
    public boolean hasPressedRightKey() {
        return !this.oldRight && this.player.inputs().right();
    }

    /**
     * @return true if the player has just released the right key (typically the 'D' key).
     */
    public boolean hasReleasedRightKey() {
        return this.oldRight && !this.player.inputs().right();
    }

    // Action Keys

    /**
     * @return true if the player is currently holding the jump key (typically the spacebar).
     * @apiNote If the player has auto-jump enabled, for 1 tick this will return true even if the player is not actually holding the jump key but may continue if they start holding it themselves.
     */
    public boolean isHoldingJumpKey() {
        return this.player.inputs().jump();
    }

    /**
     * @return true if the player has just pressed the jump key (typically the spacebar).
     * @apiNote If the player has auto-jump enabled, for 1 tick this will return true even if the player did not actually press the jump key.
     */
    public boolean hasPressedJumpKey() {
        return !this.oldJump && this.player.inputs().jump();
    }

    /**
     * @return true if the player has just released the jump key (typically the spacebar).
     * @apiNote If the player has auto-jump enabled, for 1 tick after auto-jump triggers if the player does not start holding the key themselves this will return true.
     */
    public boolean hasReleasedJumpKey() {
        return this.oldJump && !this.player.inputs().jump();
    }

    /**
     * @return true if the player is currently holding the shift key (typically the left shift key).
     */
    public boolean isHoldingShiftKey() {
        return this.player.inputs().shift();
    }

    /**
     * @return true if the player has just pressed the shift key (typically the left shift key).
     */
    public boolean hasPressedShiftKey() {
        return !this.oldShift && this.player.inputs().shift();
    }

    /**
     * @return true if the player has just released the shift key (typically the left shift key).
     */
    public boolean hasReleasedShiftKey() {
        return this.oldShift && !this.player.inputs().shift();
    }

    /**
     * @return true if the player is currently holding the sprint key (typically the left control key).
     * @apiNote This method only reports the state of the sprint key itself, not other ways to start sprinting such as double-tapping the forward key.
     */
    public boolean isHoldingSprintKey() {
        return this.player.inputs().sprint();
    }

    /**
     * @return true if the player has just pressed the sprint key (typically the left control key).
     * @apiNote This method only reports the state of the sprint key itself, not other ways to start sprinting such as double-tapping the forward key.
     */
    public boolean hasPressedSprintKey() {
        return !this.oldSprint && this.player.inputs().sprint();
    }

    /**
     * @return true if the player has just released the sprint key (typically the left control key).
     * @apiNote This method only reports the state of the sprint key itself, not other ways to start sprinting such as double-tapping the forward key.
     */
    public boolean hasReleasedSprintKey() {
        return this.oldSprint && !this.player.inputs().sprint();
    }

}
