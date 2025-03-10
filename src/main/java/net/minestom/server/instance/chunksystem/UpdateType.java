package net.minestom.server.instance.chunksystem;

/**
 * <b>Order of the entries is important. Ordered from least important to most important.</b>
 */
enum UpdateType {
    /**
     * When a normal load update happens, which was not explicitly requested (origin of claim).
     * Basically when a claim tries to load a chunk because of the radius of the claim.
     */
    LOAD_PROPAGATE,
    /**
     * When an unload update occurs. This should always be handled before any implicit loads,
     * to make sure we actually unload chunks at some point. Otherwise, if there are many chunks
     * to load and the unload update has a low priority, it will never get handled.
     */
    UNLOAD_PROPAGATE,
    /**
     * Used for the origin chunk of a claim. All origin chunks are handled as prioritized and handled
     * before any implicit updates. This should make the entire system more snappy to requests.
     */
    ADD_CLAIM_EXPLICIT,
    /**
     * Used for the origin chunk of a claim. All origin chunks are handled as prioritized and handled
     * before any implicit updates. This should make the entire system more snappy to requests.
     */
    REMOVE_CLAIM_EXPLICIT;

    UpdateType propagated() {
        if (isLoad()) return LOAD_PROPAGATE;
        if (isUnload()) return UNLOAD_PROPAGATE;
        throw new IllegalStateException();
    }

    boolean isLoad() {
        return this == LOAD_PROPAGATE || this == ADD_CLAIM_EXPLICIT;
    }

    boolean isUnload() {
        return this == UNLOAD_PROPAGATE || this == REMOVE_CLAIM_EXPLICIT;
    }
}