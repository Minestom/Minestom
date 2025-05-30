package net.minestom.server.dialog;

import org.jetbrains.annotations.NotNull;

public sealed interface Dialog {

    record Notice(@NotNull DialogMetadata metadata) implements Dialog {
    }

    record ServerLinks(@NotNull DialogMetadata metadata) implements Dialog {
    }

    record DialogList(@NotNull DialogMetadata metadata) implements Dialog {
    }

    record MultiAction(@NotNull DialogMetadata metadata) implements Dialog {
    }

    record Confirmation(@NotNull DialogMetadata metadata) implements Dialog {
    }

    @NotNull DialogMetadata metadata();
    
}
