package net.minestom.server.dialog;

public sealed interface Dialog {

    record Notice() implements Dialog {
    }
}
