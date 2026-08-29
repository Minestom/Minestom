package net.minestom.server.property;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

final class ServerPropertyImpl {

    static final String MUTABLE_SUFFIX = ".mutable";

    private static final String MUTABLE_DEFAULT = "minestom.properties" + MUTABLE_SUFFIX;

    private ServerPropertyImpl() {
    }

    private static boolean mutable(String name) {
        final String raw = System.getProperty(name + MUTABLE_SUFFIX);
        if (raw != null) return Boolean.parseBoolean(raw);
        return Boolean.getBoolean(MUTABLE_DEFAULT);
    }

    static <T> ServerProperty<T> create(String name, T defaultValue, Function<String, ? extends T> parser) {
        return build(name, defaultValue, parser, null);
    }

    static <T> ServerProperty<T> create(String name, T defaultValue,
                                        Function<String, ? extends T> parser, Consumer<? super T> validator) {
        return build(name, defaultValue, parser, Objects.requireNonNull(validator, "validator"));
    }

    private static <T> ServerProperty<T> build(String name, T defaultValue,
                                               Function<String, ? extends T> parser, @Nullable Consumer<? super T> validator) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(defaultValue, "defaultValue");
        Objects.requireNonNull(parser, "parser");
        final T value = resolve(name, defaultValue, parser, validator);
        return mutable(name)
                ? new Mutable<>(name, defaultValue, validator, value)
                : new Immutable<>(name, defaultValue, value);
    }

    private static <T> T resolve(String name, T defaultValue,
                                 Function<String, ? extends T> parser, @Nullable Consumer<? super T> validator) {
        final String raw = System.getProperty(name);
        if (raw == null) {
            validate(validator, defaultValue);
            return defaultValue;
        }
        final T parsed;
        try {
            parsed = parser.apply(raw);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Property '" + name + "' has an unparseable value: " + raw, e);
        }
        validate(validator, parsed);
        return parsed;
    }

    private static <T> void validate(@Nullable Consumer<? super T> validator, T value) {
        if (validator != null) validator.accept(value);
    }

    /**
     * A property whose value was fixed when it was built. Its value is a record component, so reads
     * through a constant reference fold away to the value itself.
     *
     * @param name         the system property it was read from
     * @param defaultValue the value used when that system property was unset
     * @param value        the fixed value
     * @param <T>          the value type
     */
    record Immutable<T>(String name, T defaultValue, T value) implements ServerProperty<T> {

        @Override
        public T get() {
            return value;
        }

        @Override
        public void set(T value) {
            throw new IllegalStateException("Property '" + name
                    + "' is immutable, assign it with -D" + name
                    + "=<value> or make it writable with -D" + name + MUTABLE_SUFFIX + "=true");
        }

        @Override
        public boolean writable() {
            return false;
        }

        @Override
        public String toString() {
            return name + "=" + value;
        }
    }

    /**
     * A property that still accepts writes.
     * Reads cannot fold, which is why properties are immutable unless asked otherwise.
     */
    static final class Mutable<T> implements ServerProperty<T> {
        private final String name;
        private final T defaultValue;
        private final @Nullable Consumer<? super T> validator;

        private volatile T value;

        Mutable(String name, T defaultValue, @Nullable Consumer<? super T> validator, T value) {
            this.name = name;
            this.defaultValue = defaultValue;
            this.validator = validator;
            this.value = value;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public T defaultValue() {
            return defaultValue;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public void set(T value) {
            Objects.requireNonNull(value, "value");
            validate(validator, value);
            this.value = value;
        }

        @Override
        public boolean writable() {
            return true;
        }

        @Override
        public String toString() {
            return name + "=" + value;
        }
    }
}
