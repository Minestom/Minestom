package net.minestom.server.network;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.*;
import net.minestom.server.adventure.MinestomDataComponentValue;
import net.minestom.server.adventure.serializer.nbt.NbtComponentSerializer;
import net.minestom.server.component.DataComponent;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.nbt.BinaryTagWriter;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

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
        writeUtf(buffer, "type");
        switch (component) {
            case TextComponent text -> {
                writeUtf(buffer, "text");

                buffer.write(BYTE, TAG_STRING); // Start "text" tag
                writeUtf(buffer, "text");
                writeUtf(buffer, text.content());
            }
            case TranslatableComponent translatable -> {
                writeUtf(buffer, "translatable");

                buffer.write(BYTE, TAG_STRING); // Start "translate" tag
                writeUtf(buffer, "translate");
                writeUtf(buffer, translatable.key());

                final String fallback = translatable.fallback();
                if (fallback != null) {
                    buffer.write(BYTE, TAG_STRING);
                    writeUtf(buffer, "fallback");
                    writeUtf(buffer, fallback);
                }

                final List<TranslationArgument> args = translatable.arguments();
                if (!args.isEmpty()) {
                    buffer.write(BYTE, TAG_LIST);
                    writeUtf(buffer, "with");
                    buffer.write(BYTE, TAG_COMPOUND); // List type
                    buffer.write(INT, args.size());
                    for (final TranslationArgument arg : args)
                        writeInnerComponent(buffer, arg.asComponent());
                }
            }
            case ScoreComponent score -> {
                writeUtf(buffer, "score");

                buffer.write(BYTE, TAG_COMPOUND); // Start "score" tag
                writeUtf(buffer, "score");
                {
                    buffer.write(BYTE, TAG_STRING);
                    writeUtf(buffer, "name");
                    writeUtf(buffer, score.name());

                    buffer.write(BYTE, TAG_STRING);
                    writeUtf(buffer, "objective");
                    writeUtf(buffer, score.objective());
                }
                buffer.write(BYTE, TAG_END); // End "score" tag

            }
            case SelectorComponent selector -> {
                writeUtf(buffer, "selector");

                buffer.write(BYTE, TAG_STRING);
                writeUtf(buffer, "selector");
                writeUtf(buffer, selector.pattern());

                final Component separator = selector.separator();
                if (separator != null) {
                    buffer.write(BYTE, TAG_COMPOUND);
                    writeUtf(buffer, "separator");
                    writeInnerComponent(buffer, separator);
                }
            }
            case KeybindComponent keybind -> {
                writeUtf(buffer, "keybind");

                buffer.write(BYTE, TAG_STRING);
                writeUtf(buffer, "keybind");
                writeUtf(buffer, keybind.keybind());
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
            writeUtf(buffer, "extra");
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
            writeUtf(buffer, "color");
            if (color instanceof NamedTextColor namedColor)
                writeUtf(buffer, namedColor.toString());
            else writeUtf(buffer, color.asHexString());
        }

        final ShadowColor shadowColor = style.shadowColor();
        if (shadowColor != null) {
            buffer.write(BYTE, TAG_INT);
            writeUtf(buffer, "shadow_color");
            buffer.write(INT, shadowColor.value());
        }

        final Key font = style.font();
        if (font != null) {
            buffer.write(BYTE, TAG_STRING);
            writeUtf(buffer, "font");
            writeUtf(buffer, font.asString());
        }

        final TextDecoration.State bold = style.decoration(TextDecoration.BOLD);
        if (bold != TextDecoration.State.NOT_SET) {
            buffer.write(BYTE, TAG_BYTE);
            writeUtf(buffer, "bold");
            buffer.write(BYTE, bold == TextDecoration.State.TRUE ? (byte) 1 : (byte) 0);
        }

        final TextDecoration.State italic = style.decoration(TextDecoration.ITALIC);
        if (italic != TextDecoration.State.NOT_SET) {
            buffer.write(BYTE, TAG_BYTE);
            writeUtf(buffer, "italic");
            buffer.write(BYTE, italic == TextDecoration.State.TRUE ? (byte) 1 : (byte) 0);
        }

        final TextDecoration.State underlined = style.decoration(TextDecoration.UNDERLINED);
        if (underlined != TextDecoration.State.NOT_SET) {
            buffer.write(BYTE, TAG_BYTE);
            writeUtf(buffer, "underlined");
            buffer.write(BYTE, underlined == TextDecoration.State.TRUE ? (byte) 1 : (byte) 0);
        }

        final TextDecoration.State strikethrough = style.decoration(TextDecoration.STRIKETHROUGH);
        if (strikethrough != TextDecoration.State.NOT_SET) {
            buffer.write(BYTE, TAG_BYTE);
            writeUtf(buffer, "strikethrough");
            buffer.write(BYTE, strikethrough == TextDecoration.State.TRUE ? (byte) 1 : (byte) 0);
        }

        final TextDecoration.State obfuscated = style.decoration(TextDecoration.OBFUSCATED);
        if (obfuscated != TextDecoration.State.NOT_SET) {
            buffer.write(BYTE, TAG_BYTE);
            writeUtf(buffer, "obfuscated");
            buffer.write(BYTE, obfuscated == TextDecoration.State.TRUE ? (byte) 1 : (byte) 0);
        }

        final String insertion = style.insertion();
        if (insertion != null) {
            buffer.write(BYTE, TAG_STRING);
            writeUtf(buffer, "insertion");
            writeUtf(buffer, insertion);
        }

        final ClickEvent clickEvent = style.clickEvent();
        if (clickEvent != null) writeClickEvent(buffer, clickEvent);

        final HoverEvent<?> hoverEvent = style.hoverEvent();
        if (hoverEvent != null) writeHoverEvent(buffer, hoverEvent);
    }

    private void writeClickEvent(@NotNull NetworkBuffer buffer, @NotNull ClickEvent clickEvent) {
        buffer.write(BYTE, TAG_COMPOUND);
        writeUtf(buffer, "clickEvent");

        buffer.write(BYTE, TAG_STRING);
        writeUtf(buffer, "action");
        writeUtf(buffer, clickEvent.action().name().toLowerCase(Locale.ROOT));

        buffer.write(BYTE, TAG_STRING);
        writeUtf(buffer, "value");
        writeUtf(buffer, clickEvent.value());

        buffer.write(BYTE, TAG_END);
    }

    @SuppressWarnings("unchecked")
    private void writeHoverEvent(@NotNull NetworkBuffer buffer, @NotNull HoverEvent<?> hoverEvent) {
        buffer.write(BYTE, TAG_COMPOUND);
        writeUtf(buffer, "hoverEvent");

        buffer.write(BYTE, TAG_STRING);
        writeUtf(buffer, "action");
        writeUtf(buffer, hoverEvent.action().toString().toLowerCase(Locale.ROOT));

        buffer.write(BYTE, TAG_COMPOUND); // Start contents tag
        writeUtf(buffer, "contents");
        if (hoverEvent.action() == HoverEvent.Action.SHOW_TEXT) {
            writeInnerComponent(buffer, (Component) hoverEvent.value());
        } else if (hoverEvent.action() == HoverEvent.Action.SHOW_ITEM) {
            var value = ((HoverEvent<HoverEvent.ShowItem>) hoverEvent).value();

            buffer.write(BYTE, TAG_STRING);
            writeUtf(buffer, "id");
            writeUtf(buffer, value.item().asString());

            buffer.write(BYTE, TAG_INT);
            writeUtf(buffer, "count");
            buffer.write(INT, value.count());

            buffer.write(BYTE, TAG_COMPOUND);
            writeUtf(buffer, "components");
            BinaryTagWriter nbtWriter = impl(buffer).nbtWriter();
            BinaryTagSerializer.Context context = impl(buffer).registries() == null ? BinaryTagSerializer.Context.EMPTY : new BinaryTagSerializer.ContextWithRegistries(impl(buffer).registries(), true);
            for (var entry : value.dataComponents().entrySet()) {
                // If it's a removed value
                if (entry.getValue().equals(DataComponentValue.removed())) {
                    buffer.write(BYTE, TAG_COMPOUND);
                    writeUtf(buffer, "!" + entry.getKey().asString());
                    buffer.write(BYTE, TAG_END);
                } else if (entry.getValue() instanceof MinestomDataComponentValue minestomDataComponentValue && minestomDataComponentValue.component().isSerialized()) {
                    // Otherwise, write the value
                    try {
                        nbtWriter.writeNamed(minestomDataComponentValue.component().key().asString(), ((DataComponent<Object>) minestomDataComponentValue.component())
                                .write(context, minestomDataComponentValue.value()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    throw new IllegalArgumentException("Tried to serialize an unknown data component type");
                }
            }
            buffer.write(BYTE, TAG_END);

            buffer.write(BYTE, TAG_END); // End contents tag
        } else if (hoverEvent.action() == HoverEvent.Action.SHOW_ENTITY) {
            var value = ((HoverEvent<HoverEvent.ShowEntity>) hoverEvent).value();

            final Component name = value.name();
            if (name != null) {
                buffer.write(BYTE, TAG_COMPOUND);
                writeUtf(buffer, "name");
                writeInnerComponent(buffer, name);
            }

            buffer.write(BYTE, TAG_STRING);
            writeUtf(buffer, "type");
            writeUtf(buffer, value.type().asString());

            buffer.write(BYTE, TAG_STRING);
            writeUtf(buffer, "id");
            writeUtf(buffer, value.id().toString());

            buffer.write(BYTE, TAG_END); // End contents tag
        } else {
            throw new UnsupportedOperationException("Unknown hover event action: " + hoverEvent.action());
        }

        buffer.write(BYTE, TAG_END);
    }

    /**
     * This is a very gross version of {@link java.io.DataOutputStream#writeUTF(String)}. We need the data in the java
     * modified utf-8 format, and I couldnt find a method without creating a new buffer for it.
     *
     * @param buffer the buffer to write to
     * @param str    the string to write
     */
    private static void writeUtf(@NotNull NetworkBuffer buffer, @NotNull String str) {
        final int strlen = str.length();
        int utflen = strlen; // optimized for ASCII

        for (int i = 0; i < strlen; i++) {
            int c = str.charAt(i);
            if (c >= 0x80 || c == 0)
                utflen += (c >= 0x800) ? 2 : 1;
        }

        if (utflen > 65535 || /* overflow */ utflen < strlen)
            throw new RuntimeException("UTF-8 string too long");

        buffer.write(SHORT, (short) utflen);
        buffer.ensureWritable(utflen);
        var impl = (NetworkBufferImpl) buffer;
        int i;
        for (i = 0; i < strlen; i++) { // optimized for initial run of ASCII
            int c = str.charAt(i);
            if (c >= 0x80 || c == 0) break;
            impl._putByte(buffer.writeIndex(), (byte) c);
            impl.advanceWrite(1);
        }

        for (; i < strlen; i++) {
            int c = str.charAt(i);
            if (c < 0x80 && c != 0) {
                impl._putByte(buffer.writeIndex(), (byte) c);
                impl.advanceWrite(1);
            } else if (c >= 0x800) {
                impl._putByte(buffer.writeIndex(), (byte) (0xE0 | ((c >> 12) & 0x0F)));
                impl._putByte(buffer.writeIndex() + 1, (byte) (0x80 | ((c >> 6) & 0x3F)));
                impl._putByte(buffer.writeIndex() + 2, (byte) (0x80 | ((c >> 0) & 0x3F)));
                impl.advanceWrite(3);
            } else {
                impl._putByte(buffer.writeIndex(), (byte) (0xC0 | ((c >> 6) & 0x1F)));
                impl._putByte(buffer.writeIndex() + 1, (byte) (0x80 | ((c >> 0) & 0x3F)));
                impl.advanceWrite(2);
            }
        }
    }
}
