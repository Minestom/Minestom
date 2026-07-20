package net.minestom.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

/// Defines how the client renders a score number, for example on a [Sidebar].
/// Instances are created through the [#blank()], [#styled(Component)]
/// and [#fixed(Component)] factories.
///
/// @param formatType the format type
/// @param content    the accompanying component, or null for [FormatType#BLANK]
public record NumberFormat(FormatType formatType,
                           @Nullable Component content) implements ComponentHolder<NumberFormat> {
    public static final NetworkBuffer.Type<NumberFormat> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(NetworkBuffer buffer, NumberFormat value) {
            buffer.write(NetworkBuffer.Enum(FormatType.class), value.formatType);
            if (value.formatType == FormatType.STYLED || value.formatType == FormatType.FIXED) {
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

    private NumberFormat() {
        this(FormatType.BLANK, null);
    }

    /// Returns a number format which hides the score entirely.
    ///
    /// @return a blank number format
    public static NumberFormat blank() {
        return new NumberFormat();
    }

    /// Returns a number format which renders the score with the style of the given component;
    /// its text content is ignored.
    ///
    /// @param style a component whose style is applied to the score
    /// @return a styled number format
    public static NumberFormat styled(Component style) {
        return new NumberFormat(FormatType.STYLED, style);
    }

    /// Returns a number format which displays the given component in place of the score.
    ///
    /// @param content the component shown instead of the score
    /// @return a fixed number format
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
        return new NumberFormat(formatType, operator.apply(content));
    }

    /// The protocol-level kind of a number format.
    public enum FormatType {
        /// The score is not displayed.
        BLANK,
        /// The score is displayed with the style of [NumberFormat#content()].
        STYLED,
        /// The score is replaced by [NumberFormat#content()].
        FIXED
    }
}
