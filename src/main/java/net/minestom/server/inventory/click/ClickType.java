package net.minestom.server.inventory.click;

public enum ClickType {

    LEFT_CLICK,
    RIGHT_CLICK,
    CHANGE_HELD,

    START_SHIFT_CLICK,
    SHIFT_CLICK,

    @Deprecated
    START_DRAGGING,

    START_LEFT_DRAGGING,
    START_RIGHT_DRAGGING,

    @Deprecated
    DRAGGING,

    LEFT_DRAGGING,
    RIGHT_DRAGGING,

    START_DOUBLE_CLICK,
    DOUBLE_CLICK,

    DROP

}
