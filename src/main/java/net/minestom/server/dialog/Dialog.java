package net.minestom.server.dialog;

import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Holder;
import net.minestom.server.registry.HolderSet;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

public sealed interface Dialog extends Holder.Direct<Dialog>, DialogLike, Dialogs, ComponentHolder<Dialog> {
    Registry<StructCodec<? extends Dialog>> REGISTRY = DynamicRegistry.fromMap(
            Key.key("dialog_type"),
            Map.entry(Key.key("notice"), Notice.CODEC),
            Map.entry(Key.key("server_links"), ServerLinks.CODEC),
            Map.entry(Key.key("dialog_list"), DialogList.CODEC),
            Map.entry(Key.key("multi_action"), MultiAction.CODEC),
            Map.entry(Key.key("confirmation"), Confirmation.CODEC));
    StructCodec<Dialog> REGISTRY_CODEC = Codec.RegistryTaggedUnion(REGISTRY, Dialog::codec);
    NetworkBuffer.Type<Dialog> REGISTRY_NETWORK_TYPE = NetworkBuffer.TypedNBT(REGISTRY_CODEC);

    NetworkBuffer.Type<Holder<Dialog>> NETWORK_TYPE = Holder.networkType(Registries::dialog, REGISTRY_NETWORK_TYPE);
    Codec<Holder<Dialog>> CODEC = Holder.codec(Registries::dialog, REGISTRY_CODEC);

    /**
     * <p>Creates a new adventure {@link DialogLike} for the dialog at the given key.</p>
     *
     * <p>Useful for sending a dialog which has been pre-sent to the client in the Dialog registry.</p>
     *
     * @param key the key of the dialog (must be registered)
     * @return a new {@link DialogLike} for the dialog at the given key
     */
    static DialogLike forKey(RegistryKey<Dialog> key) {
        return new RegistryKeyDialog(key);
    }

    @ApiStatus.Internal
    static DialogLike wrap(Holder<Dialog> dialog) {
        return switch (dialog) {
            case Dialog direct -> direct;
            case RegistryKey<Dialog> reference -> new RegistryKeyDialog(reference);
            default -> throw new IllegalArgumentException("Unsupported dialog type: " + dialog.getClass().getName());
        };
    }

    @ApiStatus.Internal
    static Holder<Dialog> unwrap(DialogLike dialog) {
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
    static DynamicRegistry<Dialog> createDefaultRegistry(Registries registries) {
        return DynamicRegistry.create(BuiltinRegistries.DIALOG, REGISTRY_CODEC,
                registries, (delegate, registry) -> new Registries.Delegating() {
                    @Override
                    public Registries registries() {
                        return delegate;
                    }

                    @Override
                    public DynamicRegistry<Dialog> dialog() {
                        return registry;
                    }
                });
    }

    record Notice(DialogMetadata metadata, DialogActionButton action) implements Dialog {
        public static final DialogActionButton DEFAULT_ACTION = new DialogActionButton(Component.translatable("gui.ok"), null, 150, null);
        public static final StructCodec<Notice> CODEC = StructCodec.struct(
                StructCodec.INLINE, DialogMetadata.CODEC, Notice::metadata,
                "action", DialogActionButton.CODEC.optional(DEFAULT_ACTION), Notice::action,
                Notice::new);

        @Override
        public StructCodec<Notice> codec() {
            return CODEC;
        }

        @Override
        public List<Component> components() {
            List<Component> components = new ArrayList<>(metadata.components());
            components.addAll(action.components());
            return components;
        }

        @Override
        public Notice copyWithOperator(UnaryOperator<Component> operator) {
            return new Notice(
                    metadata.copyWithOperator(operator),
                    action.copyWithOperator(operator)
            );
        }
    }

    record ServerLinks(
            DialogMetadata metadata,
            @Nullable DialogActionButton exitAction,
            int columns, int buttonWidth
    ) implements Dialog {
        public static final StructCodec<ServerLinks> CODEC = StructCodec.struct(
                StructCodec.INLINE, DialogMetadata.CODEC, ServerLinks::metadata,
                "exit_action", DialogActionButton.CODEC.optional(), ServerLinks::exitAction,
                "columns", Codec.INT.optional(2), ServerLinks::columns,
                "button_width", Codec.INT.optional(150), ServerLinks::buttonWidth,
                ServerLinks::new);

        @Override
        public StructCodec<ServerLinks> codec() {
            return CODEC;
        }

        @Override
        public List<Component> components() {
            List<Component> components = new ArrayList<>(metadata.components());
            if (exitAction != null) components.addAll(exitAction.components());
            return components;
        }

        @Override
        public ServerLinks copyWithOperator(UnaryOperator<Component> operator) {
            return new ServerLinks(
                    metadata.copyWithOperator(operator),
                    exitAction != null ? exitAction.copyWithOperator(operator) : null,
                    columns,
                    buttonWidth
            );
        }
    }

    record DialogList(
            DialogMetadata metadata,
            HolderSet<Dialog> dialogs,
            @Nullable DialogActionButton exitAction,
            int columns, int buttonWidth
    ) implements Dialog {
        public static final StructCodec<DialogList> CODEC = StructCodec.struct(
                StructCodec.INLINE, DialogMetadata.CODEC, DialogList::metadata,
                "dialogs", HolderSet.codec(Registries::dialog, Codec.ForwardRef(() -> Dialog.REGISTRY_CODEC)), DialogList::dialogs,
                "exit_action", DialogActionButton.CODEC.optional(), DialogList::exitAction,
                "columns", Codec.INT.optional(2), DialogList::columns,
                "button_width", Codec.INT.optional(150), DialogList::buttonWidth,
                DialogList::new);

        @Override
        public StructCodec<DialogList> codec() {
            return CODEC;
        }

        @Override
        public List<Component> components() {
            List<Component> components = new ArrayList<>(metadata.components());
            if (dialogs instanceof HolderSet.Direct<Dialog>(List<Dialog> values)) {
                for (Dialog value : values) {
                    components.addAll(value.components());
                }
            }
            if (exitAction != null) components.addAll(exitAction.components());
            return components;
        }

        @Override
        public DialogList copyWithOperator(UnaryOperator<Component> operator) {
            List<Dialog> dialogs = new ArrayList<>();
            if (this.dialogs instanceof HolderSet.Direct<Dialog>(List<Dialog> values)) {
                for (Dialog value : values) {
                    dialogs.add(value.copyWithOperator(operator));
                }
            }

            return new DialogList(
                    metadata.copyWithOperator(operator),
                    new HolderSet.Direct<>(dialogs),
                    exitAction != null ? exitAction.copyWithOperator(operator) : null,
                    columns,
                    buttonWidth
            );
        }
    }

    record MultiAction(
            DialogMetadata metadata,
            List<DialogActionButton> actions,
            @Nullable DialogActionButton exitAction,
            int columns
    ) implements Dialog {
        public static final StructCodec<MultiAction> CODEC = StructCodec.struct(
                StructCodec.INLINE, DialogMetadata.CODEC, MultiAction::metadata,
                "actions", DialogActionButton.CODEC.list(), MultiAction::actions,
                "exit_action", DialogActionButton.CODEC.optional(), MultiAction::exitAction,
                "columns", Codec.INT.optional(2), MultiAction::columns,
                MultiAction::new);

        @Override
        public StructCodec<MultiAction> codec() {
            return CODEC;
        }

        @Override
        public List<Component> components() {
            List<Component> components = new ArrayList<>(metadata.components());

            for (DialogActionButton action : actions) {
                components.addAll(action.components());
            }

            if (exitAction != null) components.addAll(exitAction.components());
            return components;
        }

        @Override
        public MultiAction copyWithOperator(UnaryOperator<Component> operator) {
            List<DialogActionButton> actions = new ArrayList<>();
            for (DialogActionButton action : this.actions) {
                actions.add(action.copyWithOperator(operator));
            }

            return new MultiAction(
                    metadata.copyWithOperator(operator),
                    actions,
                    exitAction != null ? exitAction.copyWithOperator(operator) : null,
                    columns
            );
        }
    }

    record Confirmation(
            DialogMetadata metadata,
            DialogActionButton yesButton,
            DialogActionButton noButton
    ) implements Dialog {
        public static final StructCodec<Confirmation> CODEC = StructCodec.struct(
                StructCodec.INLINE, DialogMetadata.CODEC, Confirmation::metadata,
                "yes", DialogActionButton.CODEC, Confirmation::yesButton,
                "no", DialogActionButton.CODEC, Confirmation::noButton,
                Confirmation::new);

        @Override
        public StructCodec<Confirmation> codec() {
            return CODEC;
        }

        @Override
        public List<Component> components() {
            List<Component> components = new ArrayList<>(metadata.components());

            components.addAll(yesButton.components());
            components.addAll(noButton.components());
            return components;
        }

        @Override
        public Confirmation copyWithOperator(UnaryOperator<Component> operator) {
            return new Confirmation(
                    metadata.copyWithOperator(operator),
                    yesButton.copyWithOperator(operator),
                    noButton.copyWithOperator(operator)
            );
        }
    }

    DialogMetadata metadata();

    @ApiStatus.OverrideOnly
    StructCodec<? extends Dialog> codec();

}
