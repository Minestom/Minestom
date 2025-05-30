package net.minestom.server.dialog;

import net.minestom.server.codec.Codec;

public enum DialogAction {
    CLOSE,
    NONE,
    WAIT_FOR_RESPONSE;

    public static final Codec<DialogAction> CODEC = Codec.Enum(DialogAction.class);
}
