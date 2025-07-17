package net.minestom.server.dialog;

import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public sealed interface Dialog extends Holder.Direct<Dialog>, DialogLike {
    @NotNull Registry<StructCodec<? extends Dialog>> REGISTRY = DynamicRegistry.fromMap(
            Key.key("minecraft:dialog_type"),
            Map.entry(Key.key("notice"), Notice.CODEC),
            Map.entry(Key.key("server_links"), ServerLinks.CODEC),
            Map.entry(Key.key("dialog_list"), DialogList.CODEC),
            Map.entry(Key.key("multi_action"), MultiAction.CODEC),
            Map.entry(Key.key("confirmation"), Confirmation.CODEC));
    @NotNull Codec<Dialog> REGISTRY_CODEC = Codec.RegistryTaggedUnion(REGISTRY, Dialog::codec, "type");
    @NotNull NetworkBuffer.Type<Dialog> REGISTRY_NETWORK_TYPE = NetworkBuffer.TypedNBT(REGISTRY_CODEC);

    @NotNull NetworkBuffer.Type<Holder<Dialog>> NETWORK_TYPE = Holder.networkType(Registries::dialog, REGISTRY_NETWORK_TYPE);
    @NotNull Codec<Holder<Dialog>> CODEC = Holder.codec(Registries::dialog, REGISTRY_CODEC);

    /**
     * <p>Creates a new adventure {@link DialogLike} for the dialog at the given key.</p>
     *
     * <p>Useful for sending a dialog which has been pre-sent to the client in the Dialog registry.</p>
     *
     * @param key the key of the dialog (must be registered)
     * @return a new {@link DialogLike} for the dialog at the given key
     */
    static @NotNull DialogLike forKey(@NotNull RegistryKey<Dialog> key) {
        return new RegistryKeyDialog(key);
    }

    @ApiStatus.Internal
    static @NotNull DialogLike wrap(@NotNull Holder<Dialog> dialog) {
        return switch (dialog) {
            case Dialog direct -> direct;
            case RegistryKey<Dialog> reference -> new RegistryKeyDialog(reference);
            default -> throw new IllegalArgumentException("Unsupported dialog type: " + dialog.getClass().getName());
        };
    }

    @ApiStatus.Internal
    static @NotNull Holder<Dialog> unwrap(@NotNull DialogLike dialog) {
        return switch (dialog) {
            case Dialog direct -> direct;
            case RegistryKeyDialog reference -> reference.key();
            default -> throw new IllegalArgumentException("Unsupported dialog type: " + dialog.getClass().getName());
        };
    }

    /**
     * <p>Creates a new registry for dialogs, loading the vanilla dialogs.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static @NotNull DynamicRegistry<Dialog> createDefaultRegistry(@NotNull Registries registries) {
        return DynamicRegistry.createForDialogWithSelfReferentialLoadingNightmare(
                Key.key("minecraft:dialog"), REGISTRY_CODEC, RegistryData.Resource.DIALOGS, registries
        );
    }

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
            @NotNull HolderSet<Dialog> dialogs,
            @Nullable DialogActionButton exitAction,
            int columns, int buttonWidth
    ) implements Dialog {
        public static final @NotNull StructCodec<DialogList> CODEC = StructCodec.struct(
                StructCodec.INLINE, DialogMetadata.CODEC, DialogList::metadata,
                "dialogs", HolderSet.codec(Registries::dialog, Dialog.REGISTRY_CODEC), DialogList::dialogs,
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
