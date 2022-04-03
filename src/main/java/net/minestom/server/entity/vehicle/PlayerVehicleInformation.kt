package net.minestom.server.entity.vehicle

class PlayerVehicleInformation {
    var sideways = 0f
        private set
    var forward = 0f
        private set
    private var jump = false
    private var unmount = false
    fun shouldJump(): Boolean {
        return jump
    }

    fun shouldUnmount(): Boolean {
        return unmount
    }

    /**
     * Refresh internal data
     *
     * @param sideways the new sideways value
     * @param forward  the new forward value
     * @param jump     the new jump value
     * @param unmount  the new unmount value
     */
    fun refresh(sideways: Float, forward: Float, jump: Boolean, unmount: Boolean) {
        this.sideways = sideways
        this.forward = forward
        this.jump = jump
        this.unmount = unmount
    }
}