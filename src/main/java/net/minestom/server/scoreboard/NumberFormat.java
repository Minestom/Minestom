package net.minestom.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

public record NumberFormat(FormatType formatType,
                           @Nullable Component content) implements ComponentHolder<NumberFormat> {
    private NumberFormat() {
        this(FormatType.BLANK, null);
    }

    public static final NetworkBuffer.Type<NumberFormat> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(NetworkBuffer buffer, NumberFormat value) {
            buffer.write(NetworkBuffer.Enum(FormatType.class), value.formatType);
            if (value.formatType == FormatType.STYLED) {
                assert value.content != null;
                buffer.write(NetworkBuffer.COMPONENT, value.content);
            } else if (value.formatType == FormatType.FIXED) {
                assert value.content != null;
                buffer.write(NetworkBuffer.COMPONENT, value.content);
            }
        }

        @Override
        public NumberFormat read(NetworkBuffer buffer) {
            final FormatType formatType = buffer.read(NetworkBuffer.Enum(FormatType.class));
            final Component content = formatType != FormatType.BLANK ? buffer.read(NetworkBuffer.COMPONENT) : null;
            return new NumberFormat(formatType, content);
        }
    };

    /**
     * A number format which has no sidebar score displayed
     *
     * @return a blank number format
     */
    public static NumberFormat blank() {
        return new NumberFormat();
    }

    /**
     * A number format which lets the sidebar scores be styled
     *
     * @param style a styled component
     */
    public static NumberFormat styled(Component style) {
        return new NumberFormat(FormatType.STYLED, style);
    }

    /**
     * A number format which lets the sidebar scores be styled with explicit text
     *
     * @param content the fixed component
     */
    public static NumberFormat fixed(Component content) {
        return new NumberFormat(FormatType.FIXED, content);
    }

    @Override
    public Collection<Component> components() {
        return content != null ? List.of(content) : List.of();
    }

    @Override
    public NumberFormat copyWithOperator(UnaryOperator<Component> operator) {
        if (content == null) return this;

        return new NumberFormat(
                formatType,
                operator.apply(content)
        );
    }

    private enum FormatType {
        BLANK, STYLED, FIXED
    }
}
