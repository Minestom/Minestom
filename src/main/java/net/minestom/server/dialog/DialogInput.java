package net.minestom.server.dialog;

public sealed interface DialogInput {

    record Boolean() implements DialogInput {
    }

    record NumberRange() implements DialogInput {
    }

    record SingleOption() implements DialogInput {
    }

    record Text() implements DialogInput {
    }
    
}
