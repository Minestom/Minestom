package net.minestom.server.adventure.serializer.nbt;

import net.kyori.adventure.key.Key;
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
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTType;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

//todo write tests for me!!
final class NbtComponentSerializerImpl implements NbtComponentSerializer {
    static final NbtComponentSerializer INSTANCE = new NbtComponentSerializerImpl();

    @Override
    public @NotNull Component deserialize(@NotNull NBT input) {
        return deserializeAnyComponent(input);
    }

    @Override
    public @NotNull NBT serialize(@NotNull Component component) {
        return serializeComponent(component);
    }

    // DESERIALIZATION

    private @NotNull Component deserializeAnyComponent(@NotNull NBT nbt) {
        if (nbt instanceof NBTCompound compound) {
            return deserializeComponent(compound);
        } else {
            //todo raw string + list
            throw new UnsupportedOperationException("Unknown NBT type: " + nbt.getClass().getName());
        }
    }

    private @NotNull Component deserializeComponent(@NotNull NBTCompound compound) {
        ComponentBuilder<?, ?> builder;
        var type = compound.getString("type");
        if (type != null) {
            // If type is specified, use that
            builder = switch (type) {
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
            if (compound.containsKey("text")) {
                builder = deserializeTextComponent(compound);
            } else if (compound.containsKey("translate")) {
                builder = deserializeTranslatableComponent(compound);
            } else if (compound.containsKey("score")) {
                builder = deserializeScoreComponent(compound);
            } else if (compound.containsKey("selector")) {
                builder = deserializeSelectorComponent(compound);
            } else if (compound.containsKey("keybind")) {
                builder = deserializeKeybindComponent(compound);
            } else if (compound.containsKey("nbt")) {
                builder = deserializeNbtComponent(compound);
            } else throw new UnsupportedOperationException("Unable to infer component type");
        }

        // Children
        var extra = compound.getList("extra");
        Check.argCondition(extra != null && !extra.getSubtagType().equals(NBTType.TAG_Compound),
                "Extra field must be a list of compounds");
        if (extra != null) {
            var list = new ArrayList<ComponentLike>();
            for (var child : extra) list.add(deserializeAnyComponent(child));
            builder.append(list);
        }

        // Formatting
        var style = Style.style();
        var color = compound.getString("color");
        if (color != null) {
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
        if (font != null) style.font(Key.key(font));
        var bold = compound.getByte("bold");
        if (bold != null) style.decoration(TextDecoration.BOLD, bold == 1 ? TextDecoration.State.TRUE : TextDecoration.State.FALSE);
        var italic = compound.getByte("italic");
        if (italic != null) style.decoration(TextDecoration.ITALIC, italic == 1 ? TextDecoration.State.TRUE : TextDecoration.State.FALSE);
        var underlined = compound.getByte("underlined");
        if (underlined != null) style.decoration(TextDecoration.UNDERLINED, underlined == 1 ? TextDecoration.State.TRUE : TextDecoration.State.FALSE);
        var strikethrough = compound.getByte("strikethrough");
        if (strikethrough != null) style.decoration(TextDecoration.STRIKETHROUGH, strikethrough == 1 ? TextDecoration.State.TRUE : TextDecoration.State.FALSE);
        var obfuscated = compound.getByte("obfuscated");
        if (obfuscated != null) style.decoration(TextDecoration.OBFUSCATED, obfuscated == 1 ? TextDecoration.State.TRUE : TextDecoration.State.FALSE);
        builder.style(style.build());

        // Interactivity
        var insertion = compound.getString("insertion");
        if (insertion != null) builder.insertion(insertion);
        var clickEvent = compound.getCompound("clickEvent");
        if (clickEvent != null) builder.clickEvent(deserializeClickEvent(clickEvent));
        var hoverEvent = compound.getCompound("hoverEvent");
        if (hoverEvent != null) builder.hoverEvent(deserializeHoverEvent(hoverEvent));

        return builder.build();
    }

    private @NotNull ComponentBuilder<?, ?> deserializeTextComponent(@NotNull NBTCompound compound) {
        var text = compound.getString("text");
        Check.notNull(text, "Text component must have a text field");
        return Component.text().content(text);
    }

    private @NotNull ComponentBuilder<?, ?> deserializeTranslatableComponent(@NotNull NBTCompound compound) {
        var key = compound.getString("translate");
        Check.notNull(key, "Translatable component must have a translate field");
        var builder = Component.translatable().key(key);

        var fallback = compound.getString("fallback");
        if (fallback != null) builder.fallback(fallback);

        NBTList<NBTCompound> args = compound.getList("with");
        Check.argCondition(args != null && !args.getSubtagType().equals(NBTType.TAG_Compound),
                "Translatable component with field must be a list of compounds");
        if (args != null) {
            var list = new ArrayList<ComponentLike>();
            for (var arg : args) list.add(deserializeComponent(arg));
            builder.arguments(list);
        }

        return builder;
    }

    private @NotNull ComponentBuilder<?, ?> deserializeScoreComponent(@NotNull NBTCompound compound) {
        var scoreCompound = compound.getCompound("score");
        Check.notNull(scoreCompound, "Score component must have a score field");
        var name = scoreCompound.getString("name");
        Check.notNull(name, "Score component score field must have a name field");
        var objective = scoreCompound.getString("objective");
        Check.notNull(objective, "Score component score field must have an objective field");
        var builder = Component.score().name(name).objective(objective);

        var value = scoreCompound.getString("value");
        if (value != null)
            //noinspection deprecation
            builder.value(value);

        return builder;
    }

    private @NotNull ComponentBuilder<?, ?> deserializeSelectorComponent(@NotNull NBTCompound compound) {
        var selector = compound.getString("selector");
        Check.notNull(selector, "Selector component must have a selector field");
        var builder = Component.selector().pattern(selector);

        var separator = compound.get("separator");
        if (separator != null) builder.separator(deserializeAnyComponent(separator));

        return builder;
    }

    private @NotNull ComponentBuilder<?, ?> deserializeKeybindComponent(@NotNull NBTCompound compound) {
        var keybind = compound.getString("keybind");
        Check.notNull(keybind, "Keybind component must have a keybind field");
        return Component.keybind().keybind(keybind);
    }

    private @NotNull ComponentBuilder<?, ?> deserializeNbtComponent(@NotNull NBTCompound compound) {
        throw new UnsupportedOperationException("NBTComponent is not implemented yet");
    }

    private @NotNull ClickEvent deserializeClickEvent(@NotNull NBTCompound compound) {
        var actionName = compound.getString("action");
        Check.notNull(actionName, "Click event must have an action field");
        var action = ClickEvent.Action.NAMES.value(actionName);
        Check.notNull(action, "Unknown click event action: " + actionName);
        var value = compound.getString("value");
        Check.notNull(value, "Click event must have a value field");
        return ClickEvent.clickEvent(action, value);
    }

    private @NotNull HoverEvent<?> deserializeHoverEvent(@NotNull NBTCompound compound) {
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
            var countInt = count == null ? 1 : count;
            var tag = contents.getString("tag");
            var binaryTag = tag == null ? null : BinaryTagHolder.binaryTagHolder(tag);
            return HoverEvent.showItem(Key.key(id), countInt, binaryTag);
        } else if (action == HoverEvent.Action.SHOW_ENTITY) {
            var name = contents.getCompound("name");
            var nameComponent = name == null ? null : deserializeComponent(name);
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

    private @NotNull NBT serializeComponent(@NotNull Component component) {
        MutableNBTCompound compound = new MutableNBTCompound();

        // Base component types
        if (component instanceof TextComponent text) {
            compound.setString("type", "text");
            compound.setString("text", text.content());
        } else if (component instanceof TranslatableComponent translatable) {
            compound.setString("type", "translatable");
            compound.setString("translate", translatable.key());
            var fallback = translatable.fallback();
            if (fallback != null) compound.setString("fallback", fallback);
            var args = translatable.arguments();
            if (!args.isEmpty()) compound.set("with", serializeTranslationArgs(args));
        } else if (component instanceof ScoreComponent score) {
            compound.setString("type", "score");
            var scoreCompound = new MutableNBTCompound();
            scoreCompound.setString("name", score.name());
            scoreCompound.setString("objective", score.objective());
            @SuppressWarnings("deprecation") var value = score.value();
            if (value != null) scoreCompound.setString("value", value);
            compound.set("score", scoreCompound.toCompound());
        } else if (component instanceof SelectorComponent selector) {
            compound.setString("type", "selector");
            compound.setString("selector", selector.pattern());
            var separator = selector.separator();
            if (separator != null) compound.set("separator", serializeComponent(separator));
        } else if (component instanceof KeybindComponent keybind) {
            compound.setString("type", "keybind");
            compound.setString("keybind", keybind.keybind());
        } else if (component instanceof NBTComponent<?, ?> nbt) {
            //todo
            throw new UnsupportedOperationException("NBTComponent is not implemented yet");
        } else {
            throw new UnsupportedOperationException("Unknown component type: " + component.getClass().getName());
        }

        // Children
        if (!component.children().isEmpty()) {
            var children = new ArrayList<NBT>();
            for (var child : component.children())
                children.add(serializeComponent(child));
            compound.set("extra", new NBTList<>(NBTType.TAG_Compound, children));
        }

        // Formatting
        var style = component.style();
        var color = style.color();
        if (color != null) {
            if (color instanceof NamedTextColor named) {
                compound.setString("color", named.toString());
            } else {
                compound.setString("color", color.asHexString());
            }
        }
        var font = style.font();
        if (font != null)
            compound.setString("font", font.toString());
        var bold = style.decoration(TextDecoration.BOLD);
        if (bold != TextDecoration.State.NOT_SET)
            setBool(compound, "bold", bold == TextDecoration.State.TRUE);
        var italic = style.decoration(TextDecoration.ITALIC);
        if (italic != TextDecoration.State.NOT_SET)
            setBool(compound, "italic", italic == TextDecoration.State.TRUE);
        var underlined = style.decoration(TextDecoration.UNDERLINED);
        if (underlined != TextDecoration.State.NOT_SET)
            setBool(compound, "underlined", underlined == TextDecoration.State.TRUE);
        var strikethrough = style.decoration(TextDecoration.STRIKETHROUGH);
        if (strikethrough != TextDecoration.State.NOT_SET)
            setBool(compound, "strikethrough", strikethrough == TextDecoration.State.TRUE);
        var obfuscated = style.decoration(TextDecoration.OBFUSCATED);
        if (obfuscated != TextDecoration.State.NOT_SET)
            setBool(compound, "obfuscated", obfuscated == TextDecoration.State.TRUE);

        // Interactivity
        var insertion = component.insertion();
        if (insertion != null) compound.setString("insertion", insertion);
        var clickEvent = component.clickEvent();
        if (clickEvent != null) compound.set("clickEvent", serializeClickEvent(clickEvent));
        var hoverEvent = component.hoverEvent();
        if (hoverEvent != null) compound.set("hoverEvent", serializeHoverEvent(hoverEvent));

        return compound.toCompound();
    }

    private @NotNull NBT serializeTranslationArgs(@NotNull Collection<TranslationArgument> args) {
        var list = new ArrayList<NBT>();
        for (var arg : args)
            list.add(serializeComponent(arg.asComponent()));
        return new NBTList<>(NBTType.TAG_Compound, list);
    }

    private @NotNull NBT serializeClickEvent(@NotNull ClickEvent event) {
        var compound = new MutableNBTCompound();
        compound.setString("action", event.action().toString());
        compound.setString("value", event.value());
        return compound.toCompound();
    }

    @SuppressWarnings("unchecked")
    private @NotNull NBT serializeHoverEvent(@NotNull HoverEvent<?> event) {
        var compound = new MutableNBTCompound();

        //todo surely there is a better way to do this?
        compound.setString("action", event.action().toString());
        if (event.action() == HoverEvent.Action.SHOW_TEXT) {
            var value = ((HoverEvent<Component>) event).value();
            compound.set("contents", serializeComponent(value));
        } else if (event.action() == HoverEvent.Action.SHOW_ITEM) {
            var value = ((HoverEvent<HoverEvent.ShowItem>) event).value();

            var itemCompound = new MutableNBTCompound();
            itemCompound.setString("id", value.item().asString());
            if (value.count() != 1) itemCompound.setInt("count", value.count());
            var tag = value.nbt();
            if (tag != null) itemCompound.setString("tag", tag.string());

            compound.set("contents", itemCompound.toCompound());
        } else if (event.action() == HoverEvent.Action.SHOW_ENTITY) {
            var value = ((HoverEvent<HoverEvent.ShowEntity>) event).value();

            var entityCompound = new MutableNBTCompound();
            var name = value.name();
            if (name != null) entityCompound.set("name", serializeComponent(name));
            entityCompound.setString("type", value.type().asString());
            entityCompound.setString("id", value.id().toString());

            compound.set("contents", entityCompound.toCompound());
        } else {
            throw new UnsupportedOperationException("Unknown hover event action: " + event.action());
        }

        return compound.toCompound();
    }

    private void setBool(@NotNull MutableNBTCompound compound, @NotNull String key, boolean value) {
        compound.setByte(key, value ? (byte) 1 : 0);
    }


}
