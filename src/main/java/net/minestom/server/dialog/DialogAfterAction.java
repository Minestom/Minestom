package net.minestom.server.dialog;

import net.minestom.server.codec.Codec;

public enum DialogAfterAction {
    CLOSE,
    NONE,
    WAIT_FOR_RESPONSE;

    public static final Codec<DialogAfterAction> CODEC = Codec.Enum(DialogAfterAction.class);
}
