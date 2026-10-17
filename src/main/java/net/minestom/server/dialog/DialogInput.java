package net.minestom.server.dialog;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

public sealed interface DialogInput extends ComponentHolder<DialogInput> {
    int DEFAULT_WIDTH = 200;

    Registry<StructCodec<? extends DialogInput>> REGISTRY = DynamicRegistry.fromMap(
            Key.key("input_control_type"),
            Map.entry(Key.key("boolean"), Boolean.CODEC),
            Map.entry(Key.key("number_range"), NumberRange.CODEC),
            Map.entry(Key.key("single_option"), SingleOption.CODEC),
            Map.entry(Key.key("text"), Text.CODEC));
    StructCodec<DialogInput> CODEC = Codec.RegistryTaggedUnion(REGISTRY, DialogInput::codec);

    static void validateKey(String key) {
        for (int i = 0; i < key.length(); i++) {
            final char c = key.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_')
                throw new IllegalArgumentException(MessageFormat.format("Invalid input key: {0}. Must match [a-zA-Z0-9_]+", key));
        }
    }

    record Boolean(
            String key,
            Component label,
            boolean initial,
            String onTrue,
            String onFalse
    ) implements DialogInput {

        public static final StructCodec<Boolean> CODEC = StructCodec.struct(
                "key", Codec.STRING, Boolean::key,
                "label", Codec.COMPONENT, Boolean::label,
                "initial", StructCodec.BOOLEAN.optional(false), Boolean::initial,
                "on_true", StructCodec.STRING.optional("true"), Boolean::onTrue,
                "on_false", StructCodec.STRING.optional("false"), Boolean::onFalse,
                Boolean::new);

        public Boolean {
            validateKey(key);
        }

        @Override
        public StructCodec<Boolean> codec() {
            return CODEC;
        }

        @Override
        public List<Component> components() {
            return List.of(label);
        }

        @Override
        public Boolean copyWithOperator(UnaryOperator<Component> operator) {
            return new Boolean(
                    key,
                    operator.apply(label),
                    initial,
                    onTrue,
                    onFalse
            );
        }
    }

    record NumberRange(
            String key, int width,
            Component label,
            String labelFormat,
            float start, float end,
            @Nullable Float initial,
            @Nullable Float step
    ) implements DialogInput {

        public static final StructCodec<NumberRange> CODEC = StructCodec.struct(
                "key", Codec.STRING, NumberRange::key,
                "width", Codec.INT.optional(DEFAULT_WIDTH), NumberRange::width,
                "label", Codec.COMPONENT, NumberRange::label,
                "label_format", Codec.STRING.optional("options.generic_value"), NumberRange::labelFormat,
                "start", Codec.FLOAT, NumberRange::start,
                "end", Codec.FLOAT, NumberRange::end,
                "initial", Codec.FLOAT.optional(), NumberRange::initial,
                "step", Codec.FLOAT.optional(), NumberRange::step,
                NumberRange::new);

        public NumberRange {
            validateKey(key);
        }

        @Override
        public StructCodec<NumberRange> codec() {
            return CODEC;
        }

        @Override
        public List<Component> components() {
            return List.of(label);
        }

        @Override
        public NumberRange copyWithOperator(UnaryOperator<Component> operator) {
            return new NumberRange(
                    key,
                    width,
                    operator.apply(label),
                    labelFormat,
                    start,
                    end,
                    initial,
                    step
            );
        }
    }

    record SingleOption(
            String key, int width,
            List<Option> options,
            Component label,
            boolean labelVisible
    ) implements DialogInput {
        public static final StructCodec<SingleOption> CODEC = StructCodec.struct(
                "key", Codec.STRING, SingleOption::key,
                "width", Codec.INT.optional(DEFAULT_WIDTH), SingleOption::width,
                "options", Option.CODEC.list(), SingleOption::options,
                "label", Codec.COMPONENT, SingleOption::label,
                "label_visible", Codec.BOOLEAN.optional(true), SingleOption::labelVisible,
                SingleOption::new);

        public SingleOption {
            validateKey(key);
            boolean found = false;
            for (var option : options) {
                if (!option.initial) continue;
                Check.argCondition(found, "Multiple initial options found in SingleOption input");
                found = true;
            }
        }

        @Override
        public StructCodec<SingleOption> codec() {
            return CODEC;
        }

        @Override
        public List<Component> components() {
            List<Component> components = new ArrayList<>();
            components.add(label);
            for (Option option : options) {
                components.addAll(option.components());
            }
            return List.copyOf(components);
        }

        @Override
        public SingleOption copyWithOperator(UnaryOperator<Component> operator) {

            List<Option> options = new ArrayList<>();
            for (Option option : this.options) {
                options.add(option.copyWithOperator(operator));
            }

            return new SingleOption(
                    key,
                    width,
                    options,
                    operator.apply(label),
                    labelVisible
            );
        }

        public record Option(String id, @Nullable Component display, boolean initial) implements ComponentHolder<Option> {
            public static final StructCodec<Option> CODEC = StructCodec.struct(
                    "id", Codec.STRING, Option::id,
                    "display", Codec.COMPONENT.optional(), Option::display,
                    "initial", Codec.BOOLEAN.optional(false), Option::initial,
                    Option::new);

            @Override
            public List<Component> components() {
                if (display == null) return List.of();
                return List.of(display);
            }

            @Override
            public Option copyWithOperator(UnaryOperator<Component> operator) {
                return new Option(id, display != null ? operator.apply(display) : null, initial);
            }
        }
    }

    record Text(
            String key, int width,
            Component label,
            boolean labelVisible,
            String initial,
            int maxLength,
            @Nullable Multiline multiline
    ) implements DialogInput {
        public static final StructCodec<Text> CODEC = StructCodec.struct(
                "key", Codec.STRING, Text::key,
                "width", Codec.INT.optional(DEFAULT_WIDTH), Text::width,
                "label", Codec.COMPONENT, Text::label,
                "label_visible", Codec.BOOLEAN.optional(true), Text::labelVisible,
                "initial", Codec.STRING.optional(""), Text::initial,
                "max_length", Codec.INT.optional(32), Text::maxLength,
                "multiline", Multiline.CODEC.optional(), Text::multiline,
                Text::new);

        public Text {
            validateKey(key);
        }

        @Override
        public StructCodec<Text> codec() {
            return CODEC;
        }

        @Override
        public List<Component> components() {
            return List.of(label);
        }

        @Override
        public Text copyWithOperator(UnaryOperator<Component> operator) {
            return new Text(
                    key,
                    width,
                    operator.apply(label),
                    labelVisible,
                    initial,
                    maxLength,
                    multiline
            );
        }

        public record Multiline(@Nullable Integer maxLines, @Nullable Integer height) {
            public static final StructCodec<Multiline> CODEC = StructCodec.struct(
                    "max_lines", Codec.INT.optional(), Multiline::maxLines,
                    "height", Codec.INT.optional(), Multiline::height,
                    Multiline::new);
        }
    }

    @ApiStatus.OverrideOnly
    StructCodec<? extends DialogInput> codec();

}
