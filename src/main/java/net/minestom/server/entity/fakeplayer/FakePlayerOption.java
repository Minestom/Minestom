package net.minestom.server.entity.fakeplayer;

import net.minestom.server.network.ConnectionManager;

/**
 * Represents any options for a {@link FakePlayer}.
 */
public class FakePlayerOption {

    private boolean registered = false;
    private boolean inTabList = false;

    /**
     * Gets if the player is registered internally as a Player.
     *
     * @return true if the player is registered in {@link ConnectionManager}, false otherwise
     */
    public boolean isRegistered() {
        return registered;
    }

    /**
     * Sets the FakePlayer as registered or not.
     * <p>
     * WARNING: this can't be changed halfway.
     *
     * @param registered should the fake player be registered internally
     * @return this instance, allowing for chained method calls
     */
    public FakePlayerOption setRegistered(boolean registered) {
        this.registered = registered;
        return this;
    }

    /**
     * Gets if the player is visible in the tab-list or not.
     *
     * @return true if the player is in the tab-list, false otherwise
     */
    public boolean isInTabList() {
        return inTabList;
    }

    /**
     * Sets the player in the tab-list or not.
     * <p>
     * WARNING: this can't be changed halfway.
     *
     * @param inTabList should the player be in the tab-list
     * @return this instance, allowing for chained method calls
     */
    public FakePlayerOption setInTabList(boolean inTabList) {
        this.inTabList = inTabList;
        return this;
    }
}
