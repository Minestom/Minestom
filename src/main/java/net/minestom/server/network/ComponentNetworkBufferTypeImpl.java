package net.minestom.server.network;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.*;
import net.minestom.server.adventure.serializer.nbt.NbtComponentSerializer;
import net.minestom.server.adventure.serializer.nbt.NbtDataComponentValue;
import net.minestom.server.utils.nbt.BinaryTagWriter;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static net.minestom.server.network.NetworkBuffer.*;
import static net.minestom.server.network.NetworkBufferImpl.impl;

record ComponentNetworkBufferTypeImpl() implements NetworkBufferTypeImpl<Component> {

    @Override
    public void write(@NotNull NetworkBuffer buffer, @NotNull Component value) {
        Check.notNull(value, "Component cannot be null");

        buffer.write(BYTE, TAG_COMPOUND);
        writeInnerComponent(buffer, value);
    }

    @Override
    public Component read(@NotNull NetworkBuffer buffer) {
        final BinaryTag nbt = buffer.read(NBT);
        return NbtComponentSerializer.nbt().deserialize(nbt);
    }

    // WRITING IMPL, pretty gross. Would not recommend reading.

    private static final byte TAG_END = 0;
    private static final byte TAG_BYTE = 1;
    private static final byte TAG_INT = 3;
    private static final byte TAG_STRING = 8;
    private static final byte TAG_LIST = 9;
    private static final byte TAG_COMPOUND = 10;

    private void writeInnerComponent(@NotNull NetworkBuffer buffer, @NotNull Component component) {
        buffer.write(BYTE, TAG_STRING); // Start first tag (always the type)
        buffer.write(STRING_IO_UTF8, "type");
        switch (component) {
            case TextComponent text -> {
                buffer.write(STRING_IO_UTF8, "text");

                buffer.write(BYTE, TAG_STRING); // Start "text" tag
                buffer.write(STRING_IO_UTF8, "text");
                buffer.write(STRING_IO_UTF8, text.content());
            }
            case TranslatableComponent translatable -> {
                buffer.write(STRING_IO_UTF8, "translatable");

                buffer.write(BYTE, TAG_STRING); // Start "translate" tag
                buffer.write(STRING_IO_UTF8, "translate");
                buffer.write(STRING_IO_UTF8, translatable.key());

                final String fallback = translatable.fallback();
                if (fallback != null) {
                    buffer.write(BYTE, TAG_STRING);
                    buffer.write(STRING_IO_UTF8, "fallback");
                    buffer.write(STRING_IO_UTF8, fallback);
                }

                final List<TranslationArgument> args = translatable.arguments();
                if (!args.isEmpty()) {
                    buffer.write(BYTE, TAG_LIST);
                    buffer.write(STRING_IO_UTF8, "with");
                    buffer.write(BYTE, TAG_COMPOUND); // List type
                    buffer.write(INT, args.size());
                    for (final TranslationArgument arg : args)
                        writeInnerComponent(buffer, arg.asComponent());
                }
            }
            case ScoreComponent score -> {
                buffer.write(STRING_IO_UTF8, "score");

                buffer.write(BYTE, TAG_COMPOUND); // Start "score" tag
                buffer.write(STRING_IO_UTF8, "score");
                {
                    buffer.write(BYTE, TAG_STRING);
                    buffer.write(STRING_IO_UTF8, "name");
                    buffer.write(STRING_IO_UTF8, score.name());

                    buffer.write(BYTE, TAG_STRING);
                    buffer.write(STRING_IO_UTF8, "objective");
                    buffer.write(STRING_IO_UTF8, score.objective());
                }
                buffer.write(BYTE, TAG_END); // End "score" tag

            }
            case SelectorComponent selector -> {
                buffer.write(STRING_IO_UTF8, "selector");

                buffer.write(BYTE, TAG_STRING);
                buffer.write(STRING_IO_UTF8, "selector");
                buffer.write(STRING_IO_UTF8, selector.pattern());

                final Component separator = selector.separator();
                if (separator != null) {
                    buffer.write(BYTE, TAG_COMPOUND);
                    buffer.write(STRING_IO_UTF8, "separator");
                    writeInnerComponent(buffer, separator);
                }
            }
            case KeybindComponent keybind -> {
                buffer.write(STRING_IO_UTF8, "keybind");

                buffer.write(BYTE, TAG_STRING);
                buffer.write(STRING_IO_UTF8, "keybind");
                buffer.write(STRING_IO_UTF8, keybind.keybind());
            }
            case NBTComponent<?, ?> nbt -> {
                //todo
                throw new UnsupportedOperationException("NBTComponent is not implemented yet");
            }
            default -> throw new UnsupportedOperationException("Unsupported component type: " + component.getClass());
        }

        // Children
        if (!component.children().isEmpty()) {
            buffer.write(BYTE, TAG_LIST);
            buffer.write(STRING_IO_UTF8, "extra");
            buffer.write(BYTE, TAG_COMPOUND); // List type

            buffer.write(INT, component.children().size());
            for (final Component child : component.children())
                writeInnerComponent(buffer, child);
        }

        // Formatting/Interactivity
        writeComponentStyle(buffer, component.style());

        buffer.write(BYTE, TAG_END);
    }

    private void writeComponentStyle(@NotNull NetworkBuffer buffer, @NotNull Style style) {
        final TextColor color = style.color();
        if (color != null) {
            buffer.write(BYTE, TAG_STRING);
            buffer.write(STRING_IO_UTF8, "color");
            if (color instanceof NamedTextColor namedColor)
                buffer.write(STRING_IO_UTF8, namedColor.toString());
            else buffer.write(STRING_IO_UTF8, color.asHexString());
        }

        final ShadowColor shadowColor = style.shadowColor();
        if (shadowColor != null) {
            buffer.write(BYTE, TAG_INT);
            buffer.write(STRING_IO_UTF8, "shadow_color");
            buffer.write(INT, shadowColor.value());
        }

        final Key font = style.font();
        if (font != null) {
            buffer.write(BYTE, TAG_STRING);
            buffer.write(STRING_IO_UTF8, "font");
            buffer.write(STRING_IO_UTF8, font.asString());
        }

        final TextDecoration.State bold = style.decoration(TextDecoration.BOLD);
        if (bold != TextDecoration.State.NOT_SET) {
            buffer.write(BYTE, TAG_BYTE);
            buffer.write(STRING_IO_UTF8, "bold");
            buffer.write(BYTE, bold == TextDecoration.State.TRUE ? (byte) 1 : (byte) 0);
        }

        final TextDecoration.State italic = style.decoration(TextDecoration.ITALIC);
        if (italic != TextDecoration.State.NOT_SET) {
            buffer.write(BYTE, TAG_BYTE);
            buffer.write(STRING_IO_UTF8, "italic");
            buffer.write(BYTE, italic == TextDecoration.State.TRUE ? (byte) 1 : (byte) 0);
        }

        final TextDecoration.State underlined = style.decoration(TextDecoration.UNDERLINED);
        if (underlined != TextDecoration.State.NOT_SET) {
            buffer.write(BYTE, TAG_BYTE);
            buffer.write(STRING_IO_UTF8, "underlined");
            buffer.write(BYTE, underlined == TextDecoration.State.TRUE ? (byte) 1 : (byte) 0);
        }

        final TextDecoration.State strikethrough = style.decoration(TextDecoration.STRIKETHROUGH);
        if (strikethrough != TextDecoration.State.NOT_SET) {
            buffer.write(BYTE, TAG_BYTE);
            buffer.write(STRING_IO_UTF8, "strikethrough");
            buffer.write(BYTE, strikethrough == TextDecoration.State.TRUE ? (byte) 1 : (byte) 0);
        }

        final TextDecoration.State obfuscated = style.decoration(TextDecoration.OBFUSCATED);
        if (obfuscated != TextDecoration.State.NOT_SET) {
            buffer.write(BYTE, TAG_BYTE);
            buffer.write(STRING_IO_UTF8, "obfuscated");
            buffer.write(BYTE, obfuscated == TextDecoration.State.TRUE ? (byte) 1 : (byte) 0);
        }

        final String insertion = style.insertion();
        if (insertion != null) {
            buffer.write(BYTE, TAG_STRING);
            buffer.write(STRING_IO_UTF8, "insertion");
            buffer.write(STRING_IO_UTF8, insertion);
        }

        final ClickEvent clickEvent = style.clickEvent();
        if (clickEvent != null) writeClickEvent(buffer, clickEvent);

        final HoverEvent<?> hoverEvent = style.hoverEvent();
        if (hoverEvent != null) writeHoverEvent(buffer, hoverEvent);
    }

    private void writeClickEvent(@NotNull NetworkBuffer buffer, @NotNull ClickEvent clickEvent) {
        buffer.write(BYTE, TAG_COMPOUND);
        buffer.write(STRING_IO_UTF8, "click_event");

        buffer.write(BYTE, TAG_STRING);
        buffer.write(STRING_IO_UTF8, "action");
        buffer.write(STRING_IO_UTF8, clickEvent.action().name().toLowerCase(Locale.ROOT));

        switch (clickEvent.action()) {
            case OPEN_URL -> {
                buffer.write(BYTE, TAG_STRING);
                buffer.write(STRING_IO_UTF8, "url");
                buffer.write(STRING_IO_UTF8, clickEvent.value());
            }
            case OPEN_FILE -> {
                buffer.write(BYTE, TAG_STRING);
                buffer.write(STRING_IO_UTF8, "path");
                buffer.write(STRING_IO_UTF8, clickEvent.value());
            }
            case RUN_COMMAND, SUGGEST_COMMAND -> {
                buffer.write(BYTE, TAG_STRING);
                buffer.write(STRING_IO_UTF8, "command");
                buffer.write(STRING_IO_UTF8, clickEvent.value());
            }
            case CHANGE_PAGE -> {
                buffer.write(BYTE, TAG_INT);
                buffer.write(STRING_IO_UTF8, "page");
                int page = Integer.parseInt(clickEvent.value());
                buffer.write(INT, page);
            }
            default -> { // Includes COPY_TO_CLIPBOARD
                buffer.write(BYTE, TAG_STRING);
                buffer.write(STRING_IO_UTF8, "value");
                buffer.write(STRING_IO_UTF8, clickEvent.value());
            }
        }

        buffer.write(BYTE, TAG_END);
    }

    @SuppressWarnings("unchecked")
    private void writeHoverEvent(@NotNull NetworkBuffer buffer, @NotNull HoverEvent<?> hoverEvent) {
        buffer.write(BYTE, TAG_COMPOUND);
        buffer.write(STRING_IO_UTF8, "hover_event");

        buffer.write(BYTE, TAG_STRING);
        buffer.write(STRING_IO_UTF8, "action");
        buffer.write(STRING_IO_UTF8, hoverEvent.action().toString().toLowerCase(Locale.ROOT));

        if (hoverEvent.action() == HoverEvent.Action.SHOW_TEXT) {
            buffer.write(BYTE, TAG_COMPOUND);
            buffer.write(STRING_IO_UTF8, "value");
            writeInnerComponent(buffer, (Component) hoverEvent.value());
        } else if (hoverEvent.action() == HoverEvent.Action.SHOW_ITEM) {
            var value = ((HoverEvent<HoverEvent.ShowItem>) hoverEvent).value();

            buffer.write(BYTE, TAG_STRING);
            buffer.write(STRING_IO_UTF8, "id");
            buffer.write(STRING_IO_UTF8, value.item().asString());

            buffer.write(BYTE, TAG_INT);
            buffer.write(STRING_IO_UTF8, "count");
            buffer.write(INT, value.count());

            buffer.write(BYTE, TAG_COMPOUND);
            buffer.write(STRING_IO_UTF8, "components");
            final Map<Key, NbtDataComponentValue> dataComponents = value.dataComponentsAs(NbtDataComponentValue.class);
            if (!dataComponents.isEmpty()) {
                final BinaryTagWriter nbtWriter = new BinaryTagWriter(IOView.of(buffer));
                try {
                    for (final Map.Entry<Key, NbtDataComponentValue> entry : dataComponents.entrySet()) {
                        final BinaryTag dataComponentValue = entry.getValue().value();
                        if (dataComponentValue == null) {
                            buffer.write(BYTE, TAG_COMPOUND);
                            buffer.write(STRING_IO_UTF8, "!" + entry.getKey().asString());
                            buffer.write(BYTE, TAG_END);
                        } else {
                            nbtWriter.writeNamed(entry.getKey().asString(), dataComponentValue);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            buffer.write(BYTE, TAG_END);
        } else if (hoverEvent.action() == HoverEvent.Action.SHOW_ENTITY) {
            var value = ((HoverEvent<HoverEvent.ShowEntity>) hoverEvent).value();

            final Component name = value.name();
            if (name != null) {
                buffer.write(BYTE, TAG_COMPOUND);
                buffer.write(STRING_IO_UTF8, "name");
                writeInnerComponent(buffer, name);
            }

            buffer.write(BYTE, TAG_STRING);
            buffer.write(STRING_IO_UTF8, "id");
            buffer.write(STRING_IO_UTF8, value.type().asString());

            buffer.write(BYTE, TAG_STRING);
            buffer.write(STRING_IO_UTF8, "uuid");
            buffer.write(STRING_IO_UTF8, value.id().toString());
        } else {
            throw new UnsupportedOperationException("Unknown hover event action: " + hoverEvent.action());
        }

        buffer.write(BYTE, TAG_END);
    }
}
