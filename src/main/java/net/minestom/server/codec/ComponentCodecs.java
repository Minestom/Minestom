package net.minestom.server.codec;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.*;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.codec.Transcoder.MapBuilder;
import net.minestom.server.codec.Transcoder.MapLike;
import net.minestom.server.dialog.Dialog;
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

    private static @Nullable Boolean stateToBool(@NotNull TextDecoration.State state) {
        return switch (state) {
            case NOT_SET -> null;
            case FALSE -> false;
            case TRUE -> true;
        };
    }

    public static final StructCodec<ClickEvent> CLICK_EVENT = new StructCodec<>() {
        private static final Codec<ClickEvent.Action> ACTION_CODEC = Codec.Enum(ClickEvent.Action.class);

        @Override
        public @NotNull <D> Result<ClickEvent> decodeFromMap(@NotNull Transcoder<D> coder, @NotNull MapLike<D> map) {
            final Result<ClickEvent.Action> actionResult = map.getValue("action").map(value -> ACTION_CODEC.decode(coder, value));
            if (!(actionResult instanceof Result.Ok(var action)))
                return actionResult.cast();

            return switch (action) {
                case OPEN_URL -> map.getValue("url").map(value -> Codec.STRING.decode(coder, value))
                        .mapResult(ClickEvent::openUrl);
                case OPEN_FILE -> map.getValue("path").map(value -> Codec.STRING.decode(coder, value))
                        .mapResult(ClickEvent::openFile);
                case RUN_COMMAND -> map.getValue("command").map(value -> Codec.STRING.decode(coder, value))
                        .mapResult(ClickEvent::runCommand);
                case SUGGEST_COMMAND -> map.getValue("command").map(value -> Codec.STRING.decode(coder, value))
                        .mapResult(ClickEvent::suggestCommand);
                case CHANGE_PAGE -> map.getValue("page").map(value -> Codec.INT.decode(coder, value))
                        .mapResult(ClickEvent::changePage);
                case COPY_TO_CLIPBOARD -> map.getValue("value").map(value -> Codec.STRING.decode(coder, value))
                        .mapResult(ClickEvent::copyToClipboard);
                case SHOW_DIALOG -> map.getValue("dialog").map(value -> Dialog.CODEC.decode(coder, value))
                        .mapResult(dialog -> ClickEvent.showDialog(Dialog.wrap(dialog)));
                case CUSTOM -> {
                    final Result<Key> idResult = map.getValue("id").map(value -> Codec.KEY.decode(coder, value));
                    if (!(idResult instanceof Result.Ok(Key id)))
                        yield idResult.cast();

                    BinaryTag payload = CompoundBinaryTag.empty(); // Default to empty. It is optional technically, but adventure does not support that.
                    if (map.hasValue("payload")) {
                        final Result<BinaryTag> payloadResult = map.getValue("payload")
                                .map(value -> Codec.RAW_VALUE.decode(coder, value))
                                .map(value -> value.convertTo(Transcoder.NBT));
                        if (!(payloadResult instanceof Result.Ok(BinaryTag rawValue)))
                            yield payloadResult.cast();
                        payload = rawValue;
                    }

                    yield new Result.Ok<>(ClickEvent.custom(id, MinestomAdventure.wrapNbt(payload)));
                }
            };
        }

        @Override
        public @NotNull <D> Result<D> encodeToMap(@NotNull Transcoder<D> coder, @NotNull ClickEvent value, @NotNull MapBuilder<D> map) {
            final Result<D> actionResult = ACTION_CODEC.encode(coder, value.action());
            if (!(actionResult instanceof Result.Ok(D actionValue)))
                return actionResult.cast();
            map.put("action", actionValue);

            return encodePayload(coder, switch (value.action()) {
                case OPEN_URL -> "url";
                case OPEN_FILE -> "path";
                case RUN_COMMAND, SUGGEST_COMMAND -> "command";
                case CHANGE_PAGE -> "page";
                case COPY_TO_CLIPBOARD -> "value";
                case SHOW_DIALOG -> "dialog";
                case CUSTOM -> "__IGNORED__"; // Custom payload keys are written inside its writer
            }, value.payload(), map);
        }

        private static <D> @NotNull Result<D> encodePayload(@NotNull Transcoder<D> coder, @NotNull String name, @NotNull ClickEvent.Payload payload, @NotNull MapBuilder<D> map) {
            return switch (payload) {
                case ClickEvent.Payload.Text string -> {
                    map.put(name, coder.createString(string.value()));
                    yield new Result.Ok<>(map.build());
                }
                case ClickEvent.Payload.Int integer -> {
                    map.put(name, coder.createInt(integer.integer()));
                    yield new Result.Ok<>(map.build());
                }
                case ClickEvent.Payload.Dialog dialog -> {
                    final Result<D> dialogResult = Dialog.CODEC.encode(coder, Dialog.unwrap(dialog.dialog()));
                    if (!(dialogResult instanceof Result.Ok(D dialogValue)))
                        yield dialogResult.cast();
                    map.put(name, dialogValue);
                    yield new Result.Ok<>(map.build());
                }
                case ClickEvent.Payload.Custom custom -> {
                    map.put("id", coder.createString(custom.key().asString()));
                    final RawValue payloadRawValue = RawValue.of(Transcoder.NBT, MinestomAdventure.unwrapNbt(custom.nbt()));
                    final Result<D> payloadResult = Codec.RAW_VALUE.encode(coder, payloadRawValue);
                    if (!(payloadResult instanceof Result.Ok(D customPayload)))
                        yield payloadResult.cast();
                    map.put("payload", customPayload);
                    yield new Result.Ok<>(map.build());
                }
                default ->
                        throw new UnsupportedOperationException("Unknown click event payload type: " + payload.getClass());
            };
        }
    };

    private static final Codec<HoverEvent.Action<?>> HOVER_EVENT_ACTION = Codec.STRING.transform(HoverEvent.Action.NAMES::value, HoverEvent.Action::toString);
    private static final Codec<HoverEvent<?>> HOVER_EVENT = HOVER_EVENT_ACTION.unionType("action", ComponentCodecs::hoverEventCodec, HoverEvent::action);

    private static final StructCodec<HoverEvent<Component>> SHOW_TEXT = StructCodec.struct(
            "value", COMPONENT_FORWARD, HoverEvent::value,
            HoverEvent::showText);
    private static final StructCodec<HoverEvent<HoverEvent.ShowItem>> SHOW_ITEM = StructCodec.struct(
            "id", Codec.KEY, hoverEvent -> hoverEvent.value().item(),
            "count", Codec.INT.optional(1), hoverEvent -> hoverEvent.value().count(),
            HoverEvent::showItem); // TODO(1.21.5): components
    private static final StructCodec<HoverEvent<HoverEvent.ShowEntity>> SHOW_ENTITY = StructCodec.struct(
            "id", Codec.KEY, hoverEvent -> hoverEvent.value().type(),
            "uuid", Codec.UUID_COERCED, hoverEvent -> hoverEvent.value().id(),
            "name", COMPONENT_FORWARD, hoverEvent -> hoverEvent.value().name(),
            HoverEvent::showEntity);

    private static StructCodec<? extends HoverEvent<?>> hoverEventCodec(@NotNull HoverEvent.Action<?> action) {
        if (action == HoverEvent.Action.SHOW_TEXT) return SHOW_TEXT;
        if (action == HoverEvent.Action.SHOW_ITEM) return SHOW_ITEM;
        if (action == HoverEvent.Action.SHOW_ENTITY) return SHOW_ENTITY;
        throw new IllegalStateException("Unknown hover event action: " + action);
    }

    public static final StructCodec<Style> STYLE = StructCodec.struct(
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

    private static final StructCodec<TextComponent> TEXT_CONTENT = StructCodec.struct(
            "text", Codec.STRING, TextComponent::content,
            Component::text);
    private static final StructCodec<TranslatableComponent> TRANSLATABLE_CONTENT = StructCodec.struct(
            "translate", Codec.STRING, TranslatableComponent::key,
            "fallback", Codec.STRING.optional(), TranslatableComponent::fallback,
            "with", COMPONENT_FORWARD.list().optional(List.of()), TranslatableComponent::args,
            Component::translatable);
    private static final StructCodec<ScoreComponent> SCORE_INNER_CONTENT = StructCodec.struct(
            "name", Codec.STRING, ScoreComponent::name,
            "objective", Codec.STRING, ScoreComponent::objective,
            Component::score);
    private static final StructCodec<ScoreComponent> SCORE_CONTENT = StructCodec.struct(
            "score", SCORE_INNER_CONTENT, component -> component,
            component -> component);
    private static final StructCodec<SelectorComponent> SELECTOR_CONTENT = StructCodec.struct(
            "selector", Codec.STRING, SelectorComponent::pattern,
            "separator", COMPONENT_FORWARD.optional(), SelectorComponent::separator,
            Component::selector);
    private static final StructCodec<KeybindComponent> KEYBIND_CONTENT = StructCodec.struct(
            "keybind", Codec.STRING, component -> component.keybind(),
            Component::keybind);
    private static final StructCodec<NBTComponent<?, ?>> NBT_CONTENT = new StructCodec<>() {
        @Override
        public @NotNull <D> Result<NBTComponent<?, ?>> decodeFromMap(@NotNull Transcoder<D> coder, @NotNull MapLike<D> map) {
            return new Result.Error<>("NBTComponent not yet supported");
        }

        @Override
        public @NotNull <D> Result<D> encodeToMap(@NotNull Transcoder<D> coder, @NotNull NBTComponent<?, ?> value, @NotNull MapBuilder<D> map) {
            return new Result.Error<>("NBTComponent not yet supported");
        }
    };

    public static final Codec<Component> COMPONENT = Codec.Recursive((componentCodec) -> {
        final Codec<List<Component>> componentListCodec = componentCodec.list();
        final StructCodec<List<Component>> childrenCodec = StructCodec.struct(
                "extra", componentListCodec.optional(List.of()), children -> children,
                children -> children);
        return new Codec<>() {
            @Override
            public @NotNull <D> Result<Component> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
                // A single string is a valid serialized form of a text component, try it.
                final Result<String> stringResult = coder.getString(value);
                if (stringResult instanceof Result.Ok(String string))
                    return new Result.Ok<>(Component.text(string));
                // A list of components is a valid serialized form of a component, try it.
                final Result<List<Component>> listResult = componentListCodec.decode(coder, value);
                if (listResult instanceof Result.Ok(List<Component> list))
                    return new Result.Ok<>(Component.empty().children(list));

                // Otherwise it must be an object and we need to infer the type
                final Result<MapLike<D>> mapResult = coder.getMap(value);
                if (!(mapResult instanceof Result.Ok(MapLike<D> map)))
                    return mapResult.cast();

                final String maybeType = map.getValue("type").map(coder::getString).orElse(null);
                final Result<? extends Component> baseResult = switch (maybeType) {
                    case "text" -> TEXT_CONTENT.decodeFromMap(coder, map);
                    case "translatable" -> TRANSLATABLE_CONTENT.decodeFromMap(coder, map);
                    case "score" -> SCORE_CONTENT.decodeFromMap(coder, map);
                    case "selector" -> SELECTOR_CONTENT.decodeFromMap(coder, map);
                    case "keybind" -> KEYBIND_CONTENT.decodeFromMap(coder, map);
                    case "nbt" -> NBT_CONTENT.decodeFromMap(coder, map);
                    case null, default -> {
                        // Type was not included, try to guess based on the content.
                        final Result<? extends Component> textResult = TEXT_CONTENT.decodeFromMap(coder, map);
                        if (textResult instanceof Result.Ok<? extends Component>)
                            yield textResult;
                        final Result<? extends Component> translatableResult = TRANSLATABLE_CONTENT.decodeFromMap(coder, map);
                        if (translatableResult instanceof Result.Ok<? extends Component>)
                            yield translatableResult;
                        final Result<? extends Component> scoreResult = SCORE_CONTENT.decodeFromMap(coder, map);
                        if (scoreResult instanceof Result.Ok<? extends Component>)
                            yield scoreResult;
                        final Result<? extends Component> selectorResult = SELECTOR_CONTENT.decodeFromMap(coder, map);
                        if (selectorResult instanceof Result.Ok<? extends Component>)
                            yield selectorResult;
                        final Result<? extends Component> keybindResult = KEYBIND_CONTENT.decodeFromMap(coder, map);
                        if (keybindResult instanceof Result.Ok<? extends Component>)
                            yield keybindResult;
                        final Result<? extends Component> nbtResult = NBT_CONTENT.decodeFromMap(coder, map);
                        if (nbtResult instanceof Result.Ok<? extends Component>)
                            yield nbtResult;
                        yield new Result.Error<>("Unable to determine component type");
                    }
                };

                return baseResult
                        .map(base -> childrenCodec.decodeFromMap(coder, map).mapResult(base::children))
                        .map(style -> STYLE.decodeFromMap(coder, map).mapResult(style::style));
            }

            @Override
            public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable Component value) {
                if (value == null) return new Result.Error<>("null");

                // As a special case we want to encode text components with no children or styling as strings directly.
                if (value instanceof TextComponent text && value.children().isEmpty() && value.style().isEmpty())
                    return new Result.Ok<>(coder.createString(text.content()));

                // Otherwise an object. Never encode as a list even through it is a supported decode format.
                final MapBuilder<D> map = coder.createMap();
                final Result<D> baseResult = switch (value) {
                    case TextComponent textComponent -> TEXT_CONTENT.encodeToMap(coder, textComponent, map);
                    case TranslatableComponent translatableComponent ->
                            TRANSLATABLE_CONTENT.encodeToMap(coder, translatableComponent, map);
                    case ScoreComponent scoreComponent -> SCORE_CONTENT.encodeToMap(coder, scoreComponent, map);
                    case SelectorComponent selectorComponent ->
                            SELECTOR_CONTENT.encodeToMap(coder, selectorComponent, map);
                    case KeybindComponent keybindComponent -> KEYBIND_CONTENT.encodeToMap(coder, keybindComponent, map);
                    case NBTComponent<?, ?> nbtComponent -> NBT_CONTENT.encodeToMap(coder, nbtComponent, map);
                    default -> new Result.Error<>("Unknown component type: " + value.getClass());
                };

                return baseResult
                        .map(ignored -> childrenCodec.encodeToMap(coder, value.children(), map))
                        .map(ignored -> STYLE.encodeToMap(coder, value.style(), map))
                        .mapResult(ignored -> map.build());
            }
        };
    });
}
