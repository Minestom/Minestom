package net.minestom.server.adventure.serializer.nbt;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.*;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.utils.validate.Check;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

//todo write tests for me!!
final class NbtComponentSerializerImpl implements NbtComponentSerializer {
    static final NbtComponentSerializer INSTANCE = new NbtComponentSerializerImpl();

    @Override
    public @NotNull Component deserialize(@NotNull BinaryTag input) {
        return deserializeAnyComponent(input);
    }

    @Override
    public @NotNull BinaryTag serialize(@NotNull Component component) {
        return serializeComponent(component);
    }

    // DESERIALIZATION

    private @NotNull Component deserializeAnyComponent(@NotNull BinaryTag nbt) {
        return switch (nbt) {
            case CompoundBinaryTag compound -> deserializeComponent(compound);
            case StringBinaryTag string -> Component.text(string.value());
            case ListBinaryTag list -> {
                var builder = Component.text();
                for (var element : list) {
                    builder.append(deserializeAnyComponent(element));
                }
                yield builder.build();
            }
            default -> throw new UnsupportedOperationException("Unknown NBT type: " + nbt.getClass().getName());
        };
    }

    private @NotNull Component deserializeComponent(@NotNull CompoundBinaryTag compound) {
        ComponentBuilder<?, ?> builder;
        var type = compound.get("type");
        if (type instanceof StringBinaryTag sType) {
            // If type is specified, use that
            builder = switch (sType.value()) {
                case "text" -> deserializeTextComponent(compound);
                case "translatable" -> deserializeTranslatableComponent(compound);
                case "score" -> deserializeScoreComponent(compound);
                case "selector" -> deserializeSelectorComponent(compound);
                case "keybind" -> deserializeKeybindComponent(compound);
                case "nbt" -> deserializeNbtComponent(compound);
                default -> throw new UnsupportedOperationException("Unknown component type: " + type);
            };
        } else {
            // Try to infer the type from the fields present.
            Set<String> keys = compound.keySet();
            if (keys.isEmpty()) {
                return Component.empty();
            } else if (keys.contains("text")) {
                builder = deserializeTextComponent(compound);
            } else if (keys.contains("translate")) {
                builder = deserializeTranslatableComponent(compound);
            } else if (keys.contains("score")) {
                builder = deserializeScoreComponent(compound);
            } else if (keys.contains("selector")) {
                builder = deserializeSelectorComponent(compound);
            } else if (keys.contains("keybind")) {
                builder = deserializeKeybindComponent(compound);
            } else if (keys.contains("nbt")) {
                builder = deserializeNbtComponent(compound);
            } else if (keys.contains("")) {
                //todo This feels like a bug, im not sure why this is created.
                builder = Component.text().content(compound.getString(""));
            } else throw new UnsupportedOperationException("Unable to infer component type");
        }

        // Children
        var extra = compound.getList("extra");
        if (extra.size() > 0) {
            var list = new ArrayList<ComponentLike>();
            for (var child : extra) list.add(deserializeAnyComponent(child));
            builder.append(list);
        }

        // Formatting
        builder.style(deserializeStyle(compound));

        return builder.build();
    }

    @Override
    public @NotNull Style deserializeStyle(@NotNull BinaryTag tag) {
        if (!(tag instanceof CompoundBinaryTag compound)) {
            return Style.empty();
        }

        var style = Style.style();
        var color = compound.getString("color");
        if (!color.isEmpty()) {
            var hexColor = TextColor.fromHexString(color);
            if (hexColor != null) {
                style.color(hexColor);
            } else {
                var namedColor = NamedTextColor.NAMES.value(color);
                if (namedColor != null) {
                    style.color(namedColor);
                } else {
                    throw new UnsupportedOperationException("Unknown color: " + color);
                }
            }
        }
        @Subst("minecraft:default") var font = compound.getString("font");
        if (!font.isEmpty()) style.font(Key.key(font));
        BinaryTag bold = compound.get("bold");
        if (bold instanceof ByteBinaryTag b)
            style.decoration(TextDecoration.BOLD, b.value() == 1 ? TextDecoration.State.TRUE : TextDecoration.State.FALSE);
        BinaryTag italic = compound.get("italic");
        if (italic instanceof ByteBinaryTag b)
            style.decoration(TextDecoration.ITALIC, b.value() == 1 ? TextDecoration.State.TRUE : TextDecoration.State.FALSE);
        BinaryTag underlined = compound.get("underlined");
        if (underlined instanceof ByteBinaryTag b)
            style.decoration(TextDecoration.UNDERLINED, b.value() == 1 ? TextDecoration.State.TRUE : TextDecoration.State.FALSE);
        BinaryTag strikethrough = compound.get("strikethrough");
        if (strikethrough instanceof ByteBinaryTag b)
            style.decoration(TextDecoration.STRIKETHROUGH, b.value() == 1 ? TextDecoration.State.TRUE : TextDecoration.State.FALSE);
        BinaryTag obfuscated = compound.get("obfuscated");
        if (obfuscated instanceof ByteBinaryTag b)
            style.decoration(TextDecoration.OBFUSCATED, b.value() == 1 ? TextDecoration.State.TRUE : TextDecoration.State.FALSE);

        // Interactivity
        var insertion = compound.getString("insertion");
        if (!insertion.isEmpty()) style.insertion(insertion);
        var clickEvent = compound.getCompound("clickEvent");
        if (clickEvent.size() > 0) style.clickEvent(deserializeClickEvent(clickEvent));
        var hoverEvent = compound.getCompound("hoverEvent");
        if (hoverEvent.size() > 0) style.hoverEvent(deserializeHoverEvent(hoverEvent));

        return style.build();
    }

    private @NotNull ComponentBuilder<?, ?> deserializeTextComponent(@NotNull CompoundBinaryTag compound) {
        var text = compound.getString("text");
        Check.notNull(text, "Text component must have a text field");
        return Component.text().content(text);
    }

    private @NotNull ComponentBuilder<?, ?> deserializeTranslatableComponent(@NotNull CompoundBinaryTag compound) {
        var key = compound.getString("translate");
        Check.notNull(key, "Translatable component must have a translate field");
        var builder = Component.translatable().key(key);

        var fallback = compound.get("fallback");
        if (fallback instanceof StringBinaryTag s) builder.fallback(s.value());

        ListBinaryTag args = compound.getList("with", BinaryTagTypes.COMPOUND);
        if (args.size() > 0) {
            var list = new ArrayList<ComponentLike>();
            for (var arg : args) list.add(deserializeComponent((CompoundBinaryTag) arg));
            builder.arguments(list);
        }

        return builder;
    }

    private @NotNull ComponentBuilder<?, ?> deserializeScoreComponent(@NotNull CompoundBinaryTag compound) {
        var scoreCompound = compound.getCompound("score");
        Check.notNull(scoreCompound, "Score component must have a score field");
        var name = scoreCompound.getString("name");
        Check.notNull(name, "Score component score field must have a name field");
        var objective = scoreCompound.getString("objective");
        Check.notNull(objective, "Score component score field must have an objective field");
        var builder = Component.score().name(name).objective(objective);

        var value = scoreCompound.getString("value");
        if (!value.isEmpty())
            //noinspection deprecation
            builder.value(value);

        return builder;
    }

    private @NotNull ComponentBuilder<?, ?> deserializeSelectorComponent(@NotNull CompoundBinaryTag compound) {
        var selector = compound.getString("selector");
        Check.notNull(selector, "Selector component must have a selector field");
        var builder = Component.selector().pattern(selector);

        var separator = compound.get("separator");
        if (separator != null) builder.separator(deserializeAnyComponent(separator));

        return builder;
    }

    private @NotNull ComponentBuilder<?, ?> deserializeKeybindComponent(@NotNull CompoundBinaryTag compound) {
        var keybind = compound.getString("keybind");
        Check.notNull(keybind, "Keybind component must have a keybind field");
        return Component.keybind().keybind(keybind);
    }

    private @NotNull ComponentBuilder<?, ?> deserializeNbtComponent(@NotNull CompoundBinaryTag compound) {
        throw new UnsupportedOperationException("NBTComponent is not implemented yet");
    }

    private @NotNull ClickEvent deserializeClickEvent(@NotNull CompoundBinaryTag compound) {
        var actionName = compound.getString("action");
        Check.notNull(actionName, "Click event must have an action field");
        var action = ClickEvent.Action.NAMES.value(actionName);
        Check.notNull(action, "Unknown click event action: " + actionName);
        var value = compound.getString("value");
        Check.notNull(value, "Click event must have a value field");
        return ClickEvent.clickEvent(action, value);
    }

    private @NotNull HoverEvent<?> deserializeHoverEvent(@NotNull CompoundBinaryTag compound) {
        var actionName = compound.getString("action");
        Check.notNull(actionName, "Hover event must have an action field");
        var contents = compound.getCompound("contents");
        Check.notNull(contents, "Hover event must have a contents field");

        var action = HoverEvent.Action.NAMES.value(actionName);
        if (action == HoverEvent.Action.SHOW_TEXT) {
            return HoverEvent.showText(deserializeComponent(contents));
        } else if (action == HoverEvent.Action.SHOW_ITEM) {
            @Subst("minecraft:stick") var id = contents.getString("id");
            Check.notNull(id, "Show item hover event must have an id field");
            var count = contents.getInt("count");
            var tag = contents.getString("tag");
            var binaryTag = tag.isEmpty() ? null : BinaryTagHolder.binaryTagHolder(tag);
            return HoverEvent.showItem(Key.key(id), count, binaryTag);
        } else if (action == HoverEvent.Action.SHOW_ENTITY) {
            var name = contents.getCompound("name");
            var nameComponent = name.size() == 0 ? null : deserializeComponent(name);
            @Subst("minecraft:pig") var type = contents.getString("type");
            Check.notNull(type, "Show entity hover event must have a type field");
            var id = contents.getString("id");
            Check.notNull(id, "Show entity hover event must have an id field");
            return HoverEvent.showEntity(Key.key(type), UUID.fromString(id), nameComponent);
        } else {
            throw new UnsupportedOperationException("Unknown hover event action: " + actionName);
        }
    }

    // SERIALIZATION

    private @NotNull CompoundBinaryTag serializeComponent(@NotNull Component component) {
        CompoundBinaryTag.Builder compound = CompoundBinaryTag.builder();

        // Base component types
        if (component instanceof TextComponent text) {
            compound.putString("type", "text");
            compound.putString("text", text.content());
        } else if (component instanceof TranslatableComponent translatable) {
            compound.putString("type", "translatable");
            compound.putString("translate", translatable.key());
            var fallback = translatable.fallback();
            if (fallback != null) compound.putString("fallback", fallback);
            var args = translatable.arguments();
            if (!args.isEmpty()) compound.put("with", serializeTranslationArgs(args));
        } else if (component instanceof ScoreComponent score) {
            compound.putString("type", "score");
            CompoundBinaryTag.Builder scoreCompound = CompoundBinaryTag.builder();
            scoreCompound.putString("name", score.name());
            scoreCompound.putString("objective", score.objective());
            @SuppressWarnings("deprecation") var value = score.value();
            if (value != null) scoreCompound.putString("value", value);
            compound.put("score", scoreCompound.build());
        } else if (component instanceof SelectorComponent selector) {
            compound.putString("type", "selector");
            compound.putString("selector", selector.pattern());
            var separator = selector.separator();
            if (separator != null) compound.put("separator", serializeComponent(separator));
        } else if (component instanceof KeybindComponent keybind) {
            compound.putString("type", "keybind");
            compound.putString("keybind", keybind.keybind());
        } else if (component instanceof NBTComponent<?, ?> nbt) {
            //todo
            throw new UnsupportedOperationException("NBTComponent is not implemented yet");
        } else {
            throw new UnsupportedOperationException("Unknown component type: " + component.getClass().getName());
        }

        // Children
        if (!component.children().isEmpty()) {
            ListBinaryTag.Builder<CompoundBinaryTag> children = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);
            for (var child : component.children())
                children.add(serializeComponent(child));
            compound.put("extra", children.build());
        }

        // Formatting/Interactivity
        compound.put(serializeStyle(component.style()));

        return compound.build();
    }

    @Override
    public @NotNull CompoundBinaryTag serializeStyle(@NotNull Style style) {
        CompoundBinaryTag.Builder compound = CompoundBinaryTag.builder();

        var color = style.color();
        if (color != null) {
            if (color instanceof NamedTextColor named) {
                compound.putString("color", named.toString());
            } else {
                compound.putString("color", color.asHexString());
            }
        }
        var font = style.font();
        if (font != null)
            compound.putString("font", font.toString());
        var bold = style.decoration(TextDecoration.BOLD);
        if (bold != TextDecoration.State.NOT_SET)
            compound.putBoolean("bold", bold == TextDecoration.State.TRUE);
        var italic = style.decoration(TextDecoration.ITALIC);
        if (italic != TextDecoration.State.NOT_SET)
            compound.putBoolean("italic", italic == TextDecoration.State.TRUE);
        var underlined = style.decoration(TextDecoration.UNDERLINED);
        if (underlined != TextDecoration.State.NOT_SET)
            compound.putBoolean("underlined", underlined == TextDecoration.State.TRUE);
        var strikethrough = style.decoration(TextDecoration.STRIKETHROUGH);
        if (strikethrough != TextDecoration.State.NOT_SET)
            compound.putBoolean("strikethrough", strikethrough == TextDecoration.State.TRUE);
        var obfuscated = style.decoration(TextDecoration.OBFUSCATED);
        if (obfuscated != TextDecoration.State.NOT_SET)
            compound.putBoolean("obfuscated", obfuscated == TextDecoration.State.TRUE);

        var insertion = style.insertion();
        if (insertion != null) compound.putString("insertion", insertion);
        var clickEvent = style.clickEvent();
        if (clickEvent != null) compound.put("clickEvent", serializeClickEvent(clickEvent));
        var hoverEvent = style.hoverEvent();
        if (hoverEvent != null) compound.put("hoverEvent", serializeHoverEvent(hoverEvent));

        return compound.build();
    }

    private @NotNull BinaryTag serializeTranslationArgs(@NotNull Collection<TranslationArgument> args) {
        ListBinaryTag.Builder<CompoundBinaryTag> argList = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);
        for (var arg : args)
            argList.add(serializeComponent(arg.asComponent()));
        return argList.build();
    }

    private @NotNull BinaryTag serializeClickEvent(@NotNull ClickEvent event) {
        return CompoundBinaryTag.builder()
                .putString("action", event.action().toString())
                .putString("value", event.value())
                .build();
    }

    @SuppressWarnings("unchecked")
    private @NotNull BinaryTag serializeHoverEvent(@NotNull HoverEvent<?> event) {
        CompoundBinaryTag.Builder compound = CompoundBinaryTag.builder();

        //todo surely there is a better way to do this?
        compound.putString("action", event.action().toString());
        if (event.action() == HoverEvent.Action.SHOW_TEXT) {
            var value = ((HoverEvent<Component>) event).value();
            compound.put("contents", serializeComponent(value));
        } else if (event.action() == HoverEvent.Action.SHOW_ITEM) {
            var value = ((HoverEvent<HoverEvent.ShowItem>) event).value();

            CompoundBinaryTag.Builder itemCompound = CompoundBinaryTag.builder();
            itemCompound.putString("id", value.item().asString());
            if (value.count() != 1) itemCompound.putInt("count", value.count());
            var tag = value.nbt();
            if (tag != null) itemCompound.putString("tag", tag.string());

            compound.put("contents", itemCompound.build());
        } else if (event.action() == HoverEvent.Action.SHOW_ENTITY) {
            var value = ((HoverEvent<HoverEvent.ShowEntity>) event).value();

            CompoundBinaryTag.Builder entityCompound = CompoundBinaryTag.builder();
            var name = value.name();
            if (name != null) entityCompound.put("name", serializeComponent(name));
            entityCompound.putString("type", value.type().asString());
            entityCompound.putString("id", value.id().toString());

            compound.put("contents", entityCompound.build());
        } else {
            throw new UnsupportedOperationException("Unknown hover event action: " + event.action());
        }

        return compound.build();
    }

}
