package net.minestom.server.dialog;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Holder;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * <p>Represents an action button action in a dialog.</p>
 *
 * <p>Notably some of these actions are duplicates from click events on components.
 * Until adventure supports these properly they are duplicated.</p>
 */
public sealed interface DialogAction {
    Registry<StructCodec<? extends DialogAction>> REGISTRY = DynamicRegistry.fromMap(
            Key.key("dialog_action_type"),
            Map.entry(Key.key("open_url"), OpenUrl.CODEC),
            Map.entry(Key.key("run_command"), RunCommand.CODEC),
            Map.entry(Key.key("suggest_command"), SuggestCommand.CODEC),
            Map.entry(Key.key("show_dialog"), ShowDialog.CODEC),
            Map.entry(Key.key("change_page"), ChangePage.CODEC),
            Map.entry(Key.key("copy_to_clipboard"), CopyToClipboard.CODEC),
            Map.entry(Key.key("custom"), Custom.CODEC),
            Map.entry(Key.key("dynamic/run_command"), DynamicRunCommand.CODEC),
            Map.entry(Key.key("dynamic/custom"), DynamicCustom.CODEC));
    StructCodec<DialogAction> CODEC = Codec.RegistryTaggedUnion(REGISTRY, DialogAction::codec, "type");

    record OpenUrl(String url) implements DialogAction {
        public static final StructCodec<OpenUrl> CODEC = StructCodec.struct(
                "url", StructCodec.STRING, OpenUrl::url,
                OpenUrl::new);

        @Override
        public StructCodec<? extends DialogAction> codec() {
            return CODEC;
        }
    }

    record RunCommand(String command) implements DialogAction {
        public static final StructCodec<RunCommand> CODEC = StructCodec.struct(
                "command", StructCodec.STRING, RunCommand::command,
                RunCommand::new);

        @Override
        public StructCodec<? extends DialogAction> codec() {
            return CODEC;
        }
    }

    record SuggestCommand(String command) implements DialogAction {
        public static final StructCodec<SuggestCommand> CODEC = StructCodec.struct(
                "command", StructCodec.STRING, SuggestCommand::command,
                SuggestCommand::new);

        @Override
        public StructCodec<? extends DialogAction> codec() {
            return CODEC;
        }
    }

    record ShowDialog(Holder<Dialog> dialog) implements DialogAction {
        public static final StructCodec<ShowDialog> CODEC = StructCodec.struct(
                "dialog", Holder.codec(Registries::dialog, Dialog.REGISTRY_CODEC), ShowDialog::dialog,
                ShowDialog::new);

        @Override
        public StructCodec<? extends DialogAction> codec() {
            return CODEC;
        }
    }

    record ChangePage(int page) implements DialogAction {
        public static final StructCodec<ChangePage> CODEC = StructCodec.struct(
                "page", StructCodec.INT, ChangePage::page,
                ChangePage::new);

        @Override
        public StructCodec<? extends DialogAction> codec() {
            return CODEC;
        }
    }

    record CopyToClipboard(String value) implements DialogAction {
        public static final StructCodec<CopyToClipboard> CODEC = StructCodec.struct(
                "value", StructCodec.STRING, CopyToClipboard::value,
                CopyToClipboard::new);

        @Override
        public StructCodec<? extends DialogAction> codec() {
            return CODEC;
        }
    }

    record Custom(Key key, @Nullable BinaryTag payload) implements DialogAction {
        public static final StructCodec<Custom> CODEC = StructCodec.struct(
                "id", Codec.KEY, Custom::key,
                "payload", Codec.NBT.optional(), Custom::payload,
                Custom::new);

        @Override
        public StructCodec<? extends DialogAction> codec() {
            return CODEC;
        }
    }

    record DynamicRunCommand(String template) implements DialogAction {
        public static final StructCodec<DynamicRunCommand> CODEC = StructCodec.struct(
                "template", StructCodec.STRING, DynamicRunCommand::template,
                DynamicRunCommand::new);

        @Override
        public StructCodec<? extends DialogAction> codec() {
            return CODEC;
        }
    }

    record DynamicCustom(Key key, @Nullable CompoundBinaryTag additions) implements DialogAction {
        public static final StructCodec<DynamicCustom> CODEC = StructCodec.struct(
                "id", Codec.KEY, DynamicCustom::key,
                "additions", Codec.NBT_COMPOUND.optional(), DynamicCustom::additions,
                DynamicCustom::new);

        @Override
        public StructCodec<? extends DialogAction> codec() {
            return CODEC;
        }
    }

    StructCodec<? extends DialogAction> codec();
}
