package net.minestom.server.instance.chunksystem;

/**
 * <b>Order of the entries is important. Ordered from least important to most important.</b>
 */
enum UpdateType {
    /**
     * When a normal load update happens, which was not explicitly requested (origin of claim).
     * Basically when a claim tries to load a chunk because of the radius of the claim.
     */
    LOAD_PROPAGATE(false),
    /**
     * When an unload update occurs. This should always be handled before any implicit loads,
     * to make sure we actually unload chunks at some point. Otherwise, if there are many chunks
     * to load and the unload update has a low priority, it will never get handled.
     */
    UNLOAD_PROPAGATE(false),
    /**
     * Used for the origin chunk of a claim. All origin chunks are handled as prioritized and handled
     * before any implicit updates. This should make the entire system more snappy to requests.
     */
    ADD_CLAIM_EXPLICIT(true),
    /**
     * Used for the origin chunk of a claim. All origin chunks are handled as prioritized and handled
     * before any implicit updates. This should make the entire system more snappy to requests.
     */
    REMOVE_CLAIM_EXPLICIT(true);

    private final boolean explicit;

    UpdateType(boolean explicit) {
        this.explicit = explicit;
    }

    public boolean isExplicit() {
        return explicit;
    }
}