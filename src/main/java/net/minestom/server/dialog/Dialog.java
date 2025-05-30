package net.minestom.server.dialog;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.ObjectSet;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public sealed interface Dialog {
    @NotNull Registry<StructCodec<? extends Dialog>> REGISTRY = DynamicRegistry.fromMap("dialog_type",
            Map.entry(Key.key("notice"), Notice.CODEC),
            Map.entry(Key.key("server_links"), ServerLinks.CODEC),
            Map.entry(Key.key("dialog_list"), DialogList.CODEC),
            Map.entry(Key.key("multi_action"), MultiAction.CODEC),
            Map.entry(Key.key("confirmation"), Confirmation.CODEC));
    @NotNull Codec<Dialog> CODEC = Codec.RegistryTaggedUnion(REGISTRY, Dialog::codec, "type");

    record Notice(@NotNull DialogMetadata metadata, @NotNull DialogActionButton action) implements Dialog {
        public static final DialogActionButton DEFAULT_ACTION = new DialogActionButton(Component.translatable("gui.ok"), null, 150, null);
        public static final @NotNull StructCodec<Notice> CODEC = StructCodec.struct(
                StructCodec.INLINE, DialogMetadata.CODEC, Notice::metadata,
                "action", DialogActionButton.CODEC.optional(DEFAULT_ACTION), Notice::action,
                Notice::new);

        @Override
        public @NotNull StructCodec<? extends Dialog> codec() {
            return CODEC;
        }
    }

    record ServerLinks(
            @NotNull DialogMetadata metadata,
            @Nullable DialogActionButton exitAction,
            int columns, int buttonWidth
    ) implements Dialog {
        public static final @NotNull StructCodec<ServerLinks> CODEC = StructCodec.struct(
                StructCodec.INLINE, DialogMetadata.CODEC, ServerLinks::metadata,
                "exit_action", DialogActionButton.CODEC.optional(), ServerLinks::exitAction,
                "columns", Codec.INT.optional(2), ServerLinks::columns,
                "button_width", Codec.INT.optional(150), ServerLinks::buttonWidth,
                ServerLinks::new);

        @Override
        public @NotNull StructCodec<? extends Dialog> codec() {
            return CODEC;
        }
    }

    record DialogList(
            @NotNull DialogMetadata metadata,
            @NotNull ObjectSet<Dialog> dialogs,
            @Nullable DialogActionButton exitAction,
            int columns, int buttonWidth
    ) implements Dialog {
        public static final @NotNull StructCodec<DialogList> CODEC = StructCodec.struct(
                StructCodec.INLINE, DialogMetadata.CODEC, DialogList::metadata,
                "dialogs", ObjectSet.<Dialog>codec(null), DialogList::dialogs, // TODO
                "exit_action", DialogActionButton.CODEC.optional(), DialogList::exitAction,
                "columns", Codec.INT.optional(2), DialogList::columns,
                "button_width", Codec.INT.optional(150), DialogList::buttonWidth,
                DialogList::new);

        @Override
        public @NotNull StructCodec<? extends Dialog> codec() {
            return CODEC;
        }
    }

    record MultiAction(
            @NotNull DialogMetadata metadata,
            @NotNull List<DialogActionButton> actions,
            @Nullable DialogActionButton exitAction,
            int columns
    ) implements Dialog {
        public static final @NotNull StructCodec<MultiAction> CODEC = StructCodec.struct(
                StructCodec.INLINE, DialogMetadata.CODEC, MultiAction::metadata,
                "actions", DialogActionButton.CODEC.list(), MultiAction::actions,
                "exit_action", DialogActionButton.CODEC.optional(), MultiAction::exitAction,
                "columns", Codec.INT.optional(2), MultiAction::columns,
                MultiAction::new);

        @Override
        public @NotNull StructCodec<? extends Dialog> codec() {
            return CODEC;
        }
    }

    record Confirmation(
            @NotNull DialogMetadata metadata,
            @NotNull DialogActionButton yesButton,
            @NotNull DialogActionButton noButton
    ) implements Dialog {
        public static final @NotNull StructCodec<Confirmation> CODEC = StructCodec.struct(
                StructCodec.INLINE, DialogMetadata.CODEC, Confirmation::metadata,
                "yes", DialogActionButton.CODEC, Confirmation::yesButton,
                "no", DialogActionButton.CODEC, Confirmation::noButton,
                Confirmation::new);

        @Override
        public @NotNull StructCodec<? extends Dialog> codec() {
            return CODEC;
        }
    }

    @NotNull DialogMetadata metadata();

    @NotNull StructCodec<? extends Dialog> codec();

}
