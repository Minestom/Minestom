package net.minestom.server.codec;

import net.kyori.adventure.text.*;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@ApiStatus.Internal
public final class ComponentCodecs {
    // Very gross :|
    private static final Codec<Component> COMPONENT_FORWARD = Codec.ForwardRef(() -> Codec.COMPONENT);

    public static final Codec<TextColor> TEXT_COLOR = new Codec<>() {
        @Override
        public @NotNull <D> Result<TextColor> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            final Result<String> colorResult = coder.getString(value);
            if (!(colorResult instanceof Result.Ok(String colorString)))
                return colorResult.cast();
            if (colorString.startsWith("#")) {
                final TextColor color = TextColor.fromHexString(colorString);
                if (color == null) return new Result.Error<>("Unknown color: " + colorString);
                return new Result.Ok<>(color);
            }
            final NamedTextColor namedColor = NamedTextColor.NAMES.value(colorString);
            if (namedColor == null) return new Result.Error<>("Unknown color: " + colorString);
            return new Result.Ok<>(namedColor);
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable TextColor value) {
            if (value == null) return new Result.Error<>("null");
            if (value instanceof NamedTextColor namedColor)
                return new Result.Ok<>(coder.createString(namedColor.toString()));
            return new Result.Ok<>(coder.createString(value.asHexString()));
        }
    };

    public static final Codec<ShadowColor> SHADOW_COLOR = Codec.INT.transform(ShadowColor::shadowColor, ShadowColor::value);

    private static final @Nullable Boolean stateToBool(@NotNull TextDecoration.State state) {
        return switch (state) {
            case NOT_SET -> null;
            case FALSE -> false;
            case TRUE -> true;
        };
    }

    public static final Codec<ClickEvent> CLICK_EVENT = Codec.Enum(ClickEvent.Action.class)
            .unionType(ComponentCodecs::clickEventCodec, ClickEvent::action);
    private static final StructCodec<ClickEvent> CLICK_EVENT_OPEN_URL = StructCodec.struct(
            "url", Codec.STRING, ClickEvent::value,
            ClickEvent::openUrl);
    private static final StructCodec<ClickEvent> CLICK_EVENT_OPEN_FILE = StructCodec.struct(
            "path", Codec.STRING, ClickEvent::value,
            ClickEvent::openFile);
    private static final StructCodec<ClickEvent> CLICK_EVENT_RUN_COMMAND = StructCodec.struct(
            "command", Codec.STRING, ClickEvent::value,
            ClickEvent::openUrl);
    private static final StructCodec<ClickEvent> CLICK_EVENT_SUGGEST_COMMAND = StructCodec.struct(
            "command", Codec.STRING, ClickEvent::value,
            ClickEvent::openUrl);
    private static final StructCodec<ClickEvent> CLICK_EVENT_CHANGE_PAGE = StructCodec.struct(
            "url", new CodecImpl.IntAsStringImpl(), ClickEvent::value,
            ClickEvent::openUrl);
    private static final StructCodec<ClickEvent> CLICK_EVENT_COPY_TO_CLIPBOARD = StructCodec.struct(
            "value", Codec.STRING, ClickEvent::value,
            ClickEvent::openUrl);

    private static StructCodec<ClickEvent> clickEventCodec(@NotNull ClickEvent.Action action) {
        return switch (action) {
            case OPEN_URL -> CLICK_EVENT_OPEN_URL;
            case OPEN_FILE -> CLICK_EVENT_OPEN_FILE;
            case RUN_COMMAND -> CLICK_EVENT_RUN_COMMAND;
            case SUGGEST_COMMAND -> CLICK_EVENT_SUGGEST_COMMAND;
            case CHANGE_PAGE -> CLICK_EVENT_CHANGE_PAGE;
            case COPY_TO_CLIPBOARD -> CLICK_EVENT_COPY_TO_CLIPBOARD;
        };
    }

    private static final Codec<HoverEvent.Action<?>> HOVER_EVENT_ACTION = Codec.STRING.transform(HoverEvent.Action.NAMES::value, HoverEvent.Action::toString);
    private static final Codec<HoverEvent<?>> HOVER_EVENT = HOVER_EVENT_ACTION.unionType(ComponentCodecs::hoverEventCodec, HoverEvent::action);

    private static final StructCodec<HoverEvent<?>> SHOW_TEXT = StructCodec.struct(
            "value", COMPONENT_FORWARD, hoverEvent -> (Component) hoverEvent.value(),
            HoverEvent::showText);
    private static final StructCodec<HoverEvent<?>> SHOW_ITEM = null; // TODO(1.21.5)
    private static final StructCodec<HoverEvent<?>> SHOW_ENTITY = null; // TODO(1.21.5)

    private static StructCodec<HoverEvent<?>> hoverEventCodec(@NotNull HoverEvent.Action<?> action) {
        if (action == HoverEvent.Action.SHOW_TEXT) return SHOW_TEXT;
        if (action == HoverEvent.Action.SHOW_ITEM) return SHOW_ITEM;
        if (action == HoverEvent.Action.SHOW_ENTITY) return SHOW_ENTITY;
        throw new IllegalStateException("Unknown hover event action: " + action);
    }

    public static final Codec<Style> STYLE = StructCodec.struct(
            "color", TEXT_COLOR.optional(), Style::color,
            "shadow_color", SHADOW_COLOR.optional(), Style::shadowColor,
            "bold", Codec.BOOLEAN.optional(), s -> stateToBool(s.decoration(TextDecoration.BOLD)),
            "italic", Codec.BOOLEAN.optional(), s -> stateToBool(s.decoration(TextDecoration.ITALIC)),
            "underlined", Codec.BOOLEAN.optional(), s -> stateToBool(s.decoration(TextDecoration.UNDERLINED)),
            "strikethrough", Codec.BOOLEAN.optional(), s -> stateToBool(s.decoration(TextDecoration.STRIKETHROUGH)),
            "obfuscated", Codec.BOOLEAN.optional(), s -> stateToBool(s.decoration(TextDecoration.OBFUSCATED)),
            "click_event", CLICK_EVENT.optional(), Style::clickEvent,
            "hover_event", HOVER_EVENT.optional(), Style::hoverEvent,
            "insertion", Codec.STRING.optional(), Style::insertion,
            "font", Codec.KEY.optional(), Style::font,
            (color, shadowColor, bold, italic, underlined, strikethrough, obfuscated, clickEvent, hoverEvent, insertion, font) -> Style.style()
                    .color(color)
                    .shadowColor(shadowColor)
                    .decoration(TextDecoration.BOLD, TextDecoration.State.byBoolean(bold))
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.byBoolean(italic))
                    .decoration(TextDecoration.UNDERLINED, TextDecoration.State.byBoolean(underlined))
                    .decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.byBoolean(strikethrough))
                    .decoration(TextDecoration.OBFUSCATED, TextDecoration.State.byBoolean(obfuscated))
                    .clickEvent(clickEvent)
                    .hoverEvent(hoverEvent)
                    .insertion(insertion)
                    .font(font)
                    .build()
    );

    private static final Codec<TextComponent> TEXT_CONTENT = StructCodec.struct(
            "text", Codec.STRING, TextComponent::content,
            Component::text);
    private static final Codec<TranslatableComponent> TRANSLATABLE_CONTENT = StructCodec.struct(
            "translate", Codec.STRING, TranslatableComponent::key,
            "fallback", Codec.STRING.optional(), TranslatableComponent::fallback,
            "with", COMPONENT_FORWARD.list().optional(List.of()), TranslatableComponent::args,
            Component::translatable);
    private static final Codec<ScoreComponent> SCORE_INNER_CONTENT = StructCodec.struct(
            "name", Codec.STRING, ScoreComponent::name,
            "objective", Codec.STRING, ScoreComponent::objective,
            Component::score);
    private static final Codec<ScoreComponent> SCORE_CONTENT = StructCodec.struct(
            "score", SCORE_INNER_CONTENT, component -> component,
            component -> component);
    private static final Codec<SelectorComponent> SELECTOR_CONTENT = StructCodec.struct(
            "selector", Codec.STRING, SelectorComponent::pattern,
            "separator", COMPONENT_FORWARD.optional(), SelectorComponent::separator,
            Component::selector);
    private static final Codec<KeybindComponent> KEYBIND_CONTENT = StructCodec.struct(
            "keybind", Codec.STRING, component -> component.keybind(),
            Component::keybind);
    private static final Codec<NBTComponent<?, ?>> NBT_CONTENT = new Codec<>() {
        @Override
        public @NotNull <D> Result<NBTComponent<?, ?>> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            return new Result.Error<>("NBTComponent decoding not implemented");
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable NBTComponent<?, ?> value) {
            return new Result.Error<>("NBTComponent decoding not implemented");
        }
    };

    // TODO(1.21.5) need to accept list or string, and try to encode as string if possible. never encode as list.
    public static final Codec<Component> COMPONENT = Codec.Recursive((componentCodec) -> {
        final Codec<List<Component>> childrenCodec = StructCodec.struct(
                "extra", componentCodec.list().optional(List.of()), children -> children,
                children -> children);
        return new Codec<>() {
            @Override
            public @NotNull <D> Result<Component> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
                final String maybeType = coder.getValue(value, "type").map(coder::getString).orElse(null);
                final Result<? extends Component> baseResult = switch (maybeType) {
                    case "text" -> TEXT_CONTENT.decode(coder, value);
                    case "translatable" -> TRANSLATABLE_CONTENT.decode(coder, value);
                    case "score" -> SCORE_CONTENT.decode(coder, value);
                    case "selector" -> SELECTOR_CONTENT.decode(coder, value);
                    case "keybind" -> KEYBIND_CONTENT.decode(coder, value);
                    case "nbt" -> NBT_CONTENT.decode(coder, value);
                    case null, default -> {
                        // Type was not included, try to guess based on the content.
                        final Result<? extends Component> textResult = TEXT_CONTENT.decode(coder, value);
                        if (textResult instanceof Result.Ok<? extends Component>)
                            yield textResult;
                        final Result<? extends Component> translatableResult = TRANSLATABLE_CONTENT.decode(coder, value);
                        if (translatableResult instanceof Result.Ok<? extends Component>)
                            yield translatableResult;
                        final Result<? extends Component> scoreResult = SCORE_CONTENT.decode(coder, value);
                        if (scoreResult instanceof Result.Ok<? extends Component>)
                            yield scoreResult;
                        final Result<? extends Component> selectorResult = SELECTOR_CONTENT.decode(coder, value);
                        if (selectorResult instanceof Result.Ok<? extends Component>)
                            yield selectorResult;
                        final Result<? extends Component> keybindResult = KEYBIND_CONTENT.decode(coder, value);
                        if (keybindResult instanceof Result.Ok<? extends Component>)
                            yield keybindResult;
                        final Result<? extends Component> nbtResult = NBT_CONTENT.decode(coder, value);
                        if (nbtResult instanceof Result.Ok<? extends Component>)
                            yield nbtResult;
                        yield new Result.Error<>("Unable to determine component type");
                    }
                };
                if (!(baseResult instanceof Result.Ok(Component base)))
                    return baseResult.cast();

                final Result<List<Component>> childrenResult = childrenCodec.decode(coder, value);
                if (!(childrenResult instanceof Result.Ok(List<Component> children)))
                    return childrenResult.cast();
                final Result<Style> styleResult = STYLE.decode(coder, value);
                if (!(styleResult instanceof Result.Ok(Style style)))
                    return styleResult.cast();
                return new Result.Ok<>(base.style(style).children(children));
            }

            @Override
            public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable Component value) {
                if (value == null) return new Result.Error<>("null");
                final Result<D> baseResult = switch (value) {
                    case TextComponent textComponent -> TEXT_CONTENT.encode(coder, textComponent);
                    case TranslatableComponent translatableComponent ->
                            TRANSLATABLE_CONTENT.encode(coder, translatableComponent);
                    case ScoreComponent scoreComponent -> SCORE_CONTENT.encode(coder, scoreComponent);
                    case SelectorComponent selectorComponent -> SELECTOR_CONTENT.encode(coder, selectorComponent);
                    case KeybindComponent keybindComponent -> KEYBIND_CONTENT.encode(coder, keybindComponent);
                    case NBTComponent<?, ?> nbtComponent -> NBT_CONTENT.encode(coder, nbtComponent);
                    default -> new Result.Error<>("Unknown component type: " + value.getClass());
                };
                if (!(baseResult instanceof Result.Ok(D base)))
                    return baseResult.cast();
                final Result<D> childrenResult = childrenCodec.encode(coder, value.children());
                if (!(childrenResult instanceof Result.Ok(D children)))
                    return childrenResult.cast();
                final Result<D> styleResult = STYLE.encode(coder, value.style());
                if (!(styleResult instanceof Result.Ok(D style)))
                    return styleResult.cast();
                return coder.mergeToMap(List.of(base, children, style));
            }
        };
    });
}
