package net.minestom.server.dialog;

public sealed interface DialogBody {

    record Item() implements DialogBody {
    }

    record PlainMessage() implements DialogBody {
    }
    
}
