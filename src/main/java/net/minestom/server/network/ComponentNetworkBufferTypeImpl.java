package net.minestom.server.network;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.*;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.kyori.adventure.text.object.SpriteObjectContents;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.adventure.serializer.nbt.NbtDataComponentValue;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.dialog.Dialog;
import net.minestom.server.registry.RegistryTranscoder;

import java.io.IOException;
import java.util.*;

import static net.minestom.server.network.NetworkBuffer.*;
import static net.minestom.server.network.NetworkBufferImpl.impl;

record ComponentNetworkBufferTypeImpl() implements NetworkBufferTypeImpl<Component> {

    @Override
    public void write(NetworkBuffer buffer, Component value) {
        Objects.requireNonNull(value, "Component cannot be null");

        buffer.write(BYTE, TAG_COMPOUND);
        writeInnerComponent(new NbtWriter(buffer), value);
    }

    @Override
    public Component read(NetworkBuffer buffer) {
        final Transcoder<BinaryTag> coder = buffer.registries() != null
                ? new RegistryTranscoder<>(Transcoder.NBT, buffer.registries())
                : Transcoder.NBT;
        return Codec.COMPONENT.decode(coder, buffer.read(NBT)).orElseThrow();
    }

    private static final byte TAG_END = 0;
    private static final byte TAG_BYTE = 1;
    private static final byte TAG_INT = 3;
    private static final byte TAG_STRING = 8;
    private static final byte TAG_LIST = 9;
    private static final byte TAG_COMPOUND = 10;
    private static final byte TAG_INT_ARRAY = 11;

    private void writeInnerComponent(NbtWriter nbt, Component component) {
        switch (component) {
            case TextComponent text -> {
                nbt.componentType("text");
                nbt.string("text", text.content());
            }
            case TranslatableComponent translatable -> {
                nbt.componentType("translatable");
                nbt.string("translate", translatable.key());

                final String fallback = translatable.fallback();
                if (fallback != null) nbt.string("fallback", fallback);

                final List<TranslationArgument> args = translatable.arguments();
                if (!args.isEmpty()) {
                    nbt.list("with", TAG_COMPOUND, args.size());
                    for (final TranslationArgument arg : args)
                        writeInnerComponent(nbt, arg.asComponent());
                }
            }
            case ScoreComponent score -> {
                nbt.componentType("score");

                nbt.compound("score");
                nbt.string("name", score.name());
                nbt.string("objective", score.objective());
                nbt.end();
            }
            case SelectorComponent selector -> {
                nbt.componentType("selector");
                nbt.string("selector", selector.pattern());

                final Component separator = selector.separator();
                if (separator != null) {
                    nbt.compound("separator");
                    writeInnerComponent(nbt, separator);
                }
            }
            case KeybindComponent keybind -> {
                nbt.componentType("keybind");
                nbt.string("keybind", keybind.keybind());
            }
            case NBTComponent<?> _ -> {
                //todo
                throw new UnsupportedOperationException("NBTComponent is not implemented yet");
            }
            case ObjectComponent object -> {
                nbt.componentType("object");

                switch (object.contents()) {
                    case SpriteObjectContents sprite -> {
                        if (!sprite.atlas().equals(SpriteObjectContents.DEFAULT_ATLAS))
                            nbt.string("atlas", sprite.atlas().asMinimalString());
                        nbt.string("sprite", sprite.sprite().asMinimalString());
                    }
                    case PlayerHeadObjectContents player -> {
                        nbt.compound("player");
                        {
                            final String name = player.name();
                            if (name != null) nbt.string("name", name);

                            final UUID id = player.id();
                            if (id != null) {
                                nbt.intArray("id", 4);

                                final long uuidMost = id.getMostSignificantBits();
                                final long uuidLeast = id.getLeastSignificantBits();
                                nbt.rawInt((int) (uuidMost >> 32));
                                nbt.rawInt((int) uuidMost);
                                nbt.rawInt((int) (uuidLeast >> 32));
                                nbt.rawInt((int) uuidLeast);
                            }

                            final List<PlayerHeadObjectContents.ProfileProperty> properties = player.profileProperties();
                            final int propertyCount = properties.size();
                            if (propertyCount > 0) {
                                nbt.list("properties", TAG_COMPOUND, propertyCount);
                                for (PlayerHeadObjectContents.ProfileProperty property : properties)
                                    writeProfileProperty(nbt, property);
                            }

                            final Key texture = player.texture();
                            if (texture != null) nbt.string("body", texture.asMinimalString());
                        }
                        nbt.end();

                        if (!player.hat()) nbt.byteTag("hat", (byte) 0);
                    }
                    default -> throw new UnsupportedOperationException("Unknown object contents: " + object.contents());
                }
                final Component fallback = object.fallback();
                if (fallback != null) {
                    nbt.tag(TAG_STRING, "fallback");
                    writeInnerComponent(nbt, fallback);
                }
            }
            default -> throw new UnsupportedOperationException("Unsupported component type: " + component.getClass());
        }

        // Children
        final List<Component> children = component.children();
        if (!children.isEmpty()) {
            nbt.list("extra", TAG_COMPOUND, children.size());
            for (final Component child : children)
                writeInnerComponent(nbt, child);
        }

        // Formatting/Interactivity
        writeComponentStyle(nbt, component.style());

        nbt.end();
    }

    private void writeComponentStyle(NbtWriter nbt, Style style) {
        final TextColor color = style.color();
        if (color != null) {
            nbt.string("color", color instanceof NamedTextColor namedColor
                    ? namedColor.name()
                    : color.asHexString());
        }

        final ShadowColor shadowColor = style.shadowColor();
        if (shadowColor != null) nbt.intTag("shadow_color", shadowColor.value());

        final Key font = style.font();
        if (font != null) nbt.string("font", font.asString());

        writeDecoration(nbt, style, TextDecoration.BOLD, "bold");
        writeDecoration(nbt, style, TextDecoration.ITALIC, "italic");
        writeDecoration(nbt, style, TextDecoration.UNDERLINED, "underlined");
        writeDecoration(nbt, style, TextDecoration.STRIKETHROUGH, "strikethrough");
        writeDecoration(nbt, style, TextDecoration.OBFUSCATED, "obfuscated");

        final String insertion = style.insertion();
        if (insertion != null) nbt.string("insertion", insertion);

        final ClickEvent<?> clickEvent = style.clickEvent();
        if (clickEvent != null) writeClickEvent(nbt, clickEvent);

        final HoverEvent<?> hoverEvent = style.hoverEvent();
        if (hoverEvent != null) writeHoverEvent(nbt, hoverEvent);
    }

    private void writeClickEvent(NbtWriter nbt, ClickEvent<?> clickEvent) {
        nbt.compound("click_event");
        nbt.string("action", clickEvent.action().name().toLowerCase(Locale.ROOT));

        switch (clickEvent.action()) {
            case ClickEvent.Action.OpenUrl _ -> {
                final ClickEvent.Payload.Text payload = checkPayload(clickEvent, ClickEvent.Payload.Text.class);
                nbt.string("url", payload.value());
            }
            case ClickEvent.Action.OpenFile _ -> {
                final ClickEvent.Payload.Text payload = checkPayload(clickEvent, ClickEvent.Payload.Text.class);
                nbt.string("path", payload.value());
            }
            case ClickEvent.Action.RunCommand _, ClickEvent.Action.SuggestCommand _ -> {
                final ClickEvent.Payload.Text payload = checkPayload(clickEvent, ClickEvent.Payload.Text.class);
                nbt.string("command", payload.value());
            }
            case ClickEvent.Action.ChangePage _ -> {
                final ClickEvent.Payload.Int payload = checkPayload(clickEvent, ClickEvent.Payload.Int.class);
                nbt.intTag("page", payload.integer());
            }
            case ClickEvent.Action.CopyToClipboard _ -> {
                final ClickEvent.Payload.Text payload = checkPayload(clickEvent, ClickEvent.Payload.Text.class);
                nbt.string("value", payload.value());
            }
            case ClickEvent.Action.ShowDialog _ -> {
                final ClickEvent.Payload.Dialog payload = checkPayload(clickEvent, ClickEvent.Payload.Dialog.class);

                final Transcoder<BinaryTag> coder = nbt.buffer().registries() != null
                        ? new RegistryTranscoder<>(Transcoder.NBT, nbt.buffer().registries())
                        : Transcoder.NBT;
                final BinaryTag dialog = Dialog.CODEC.encode(coder, Dialog.unwrap(payload.dialog())).orElseThrow();
                nbt.named("dialog", dialog, "Failed to write dialog click event payload");
            }
            case ClickEvent.Action.Custom _ -> {
                final ClickEvent.Payload.Custom payload = checkPayload(clickEvent, ClickEvent.Payload.Custom.class);
                nbt.string("id", payload.key().asString());
                nbt.named("payload", MinestomAdventure.unwrapNbt(payload.nbt()), "Failed to write custom click event payload");
            }
            default -> throw new UnsupportedOperationException("Unknown click event action: " + clickEvent.action());
        }

        nbt.end();
    }

    private <T extends ClickEvent.Payload> T checkPayload(ClickEvent<?> clickEvent, Class<T> expected) {
        final ClickEvent.Payload payload = clickEvent.payload();
        if (!expected.isInstance(payload))
            throw new IllegalArgumentException(
                    "Expected " + expected.getSimpleName() + " for " + clickEvent.action() + ", got: " + payload.getClass());
        return expected.cast(payload);
    }

    @SuppressWarnings("unchecked")
    private void writeHoverEvent(NbtWriter nbt, HoverEvent<?> hoverEvent) {
        nbt.compound("hover_event");
        nbt.string("action", hoverEvent.action().toString().toLowerCase(Locale.ROOT));

        if (hoverEvent.action() == HoverEvent.Action.SHOW_TEXT) {
            nbt.compound("value");
            writeInnerComponent(nbt, (Component) hoverEvent.value());
        } else if (hoverEvent.action() == HoverEvent.Action.SHOW_ITEM) {
            var value = ((HoverEvent<HoverEvent.ShowItem>) hoverEvent).value();

            nbt.string("id", value.item().asString());
            nbt.intTag("count", value.count());

            nbt.compound("components");
            final Map<Key, NbtDataComponentValue> dataComponents = value.dataComponentsAs(NbtDataComponentValue.class);
            if (!dataComponents.isEmpty()) {
                for (final Map.Entry<Key, NbtDataComponentValue> entry : dataComponents.entrySet()) {
                    final BinaryTag dataComponentValue = entry.getValue().value();
                    if (dataComponentValue == null) {
                        nbt.compound("!" + entry.getKey().asString());
                        nbt.end();
                    } else {
                        nbt.named(entry.getKey().asString(), dataComponentValue);
                    }
                }
            }
            nbt.end();
        } else if (hoverEvent.action() == HoverEvent.Action.SHOW_ENTITY) {
            var value = ((HoverEvent<HoverEvent.ShowEntity>) hoverEvent).value();

            final Component name = value.name();
            if (name != null) {
                nbt.compound("name");
                writeInnerComponent(nbt, name);
            }

            nbt.string("id", value.type().asString());
            nbt.string("uuid", value.id().toString());
        } else {
            throw new UnsupportedOperationException("Unknown hover event action: " + hoverEvent.action());
        }

        nbt.end();
    }

    private static void writeProfileProperty(NbtWriter nbt, PlayerHeadObjectContents.ProfileProperty property) {
        nbt.string("name", property.name());
        nbt.string("value", property.value());
        final String signature = property.signature();
        if (signature != null) nbt.string("signature", signature);
        nbt.end();
    }

    private static void writeDecoration(NbtWriter nbt, Style style, TextDecoration decoration, String name) {
        final TextDecoration.State state = style.decoration(decoration);
        if (state != TextDecoration.State.NOT_SET)
            nbt.byteTag(name, state == TextDecoration.State.TRUE ? (byte) 1 : (byte) 0);
    }

    private record NbtWriter(NetworkBuffer buffer) {
        void componentType(String type) {
            string("type", type);
        }

        void compound(String name) {
            tag(TAG_COMPOUND, name);
        }

        void list(String name, byte elementType, int size) {
            tag(TAG_LIST, name);
            buffer.write(BYTE, elementType);
            buffer.write(INT, size);
        }

        void string(String name, String value) {
            tag(TAG_STRING, name);
            buffer.write(STRING_IO_UTF8, value);
        }

        void intTag(String name, int value) {
            tag(TAG_INT, name);
            rawInt(value);
        }

        void byteTag(String name, byte value) {
            tag(TAG_BYTE, name);
            buffer.write(BYTE, value);
        }

        void intArray(String name, int size) {
            tag(TAG_INT_ARRAY, name);
            rawInt(size);
        }

        void rawInt(int value) {
            buffer.write(INT, value);
        }

        void named(String name, BinaryTag value) {
            named(name, value, "Failed to write NBT tag");
        }

        void named(String name, BinaryTag value, String failureMessage) {
            try {
                impl(buffer).nbtWriter().writeNamed(name, value);
            } catch (IOException e) {
                throw new RuntimeException(failureMessage, e);
            }
        }

        void tag(byte type, String name) {
            buffer.write(BYTE, type);
            buffer.write(STRING_IO_UTF8, name);
        }

        void end() {
            buffer.write(BYTE, TAG_END);
        }
    }
}
