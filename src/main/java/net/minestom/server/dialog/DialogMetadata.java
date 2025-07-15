package net.minestom.server.dialog;

import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.utils.validate.Check;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record DialogMetadata(
        Component title,
        @Nullable Component externalTitle,
        boolean canCloseWithEscape,
        boolean pause,
        DialogAfterAction afterAction,
        List<DialogBody> body,
        List<DialogInput> inputs
) {
    public static final StructCodec<DialogMetadata> CODEC = StructCodec.struct(
            "title", Codec.COMPONENT, DialogMetadata::title,
            "external_title", Codec.COMPONENT.optional(), DialogMetadata::externalTitle,
            "can_close_with_escape", StructCodec.BOOLEAN.optional(true), DialogMetadata::canCloseWithEscape,
            "pause", StructCodec.BOOLEAN.optional(true), DialogMetadata::pause,
            "after_action", DialogAfterAction.CODEC.optional(DialogAfterAction.CLOSE), DialogMetadata::afterAction,
            "body", DialogBody.CODEC.listOrSingle().optional(List.of()), DialogMetadata::body,
            "inputs", DialogInput.CODEC.list().optional(List.of()), DialogMetadata::inputs,
            DialogMetadata::new);

    public DialogMetadata {
        Check.argCondition(pause && afterAction == DialogAfterAction.NONE,
                "Dialog may not have pause=true and afterAction=NONE");
    }

}
