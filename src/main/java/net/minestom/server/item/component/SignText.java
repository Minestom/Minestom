package net.minestom.server.item.component;

import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.color.DyeColor;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * One text face of a sign.
 *
 * @param messages         the four lines, must hold exactly {@value #LINES} entries
 * @param filteredMessages the four lines as shown to players with chat filtering on, null to show {@code messages} to everyone
 * @param color            the dye the text is written in
 * @param hasGlowingText   whether the text glows in the dark
 */
public record SignText(
        List<Component> messages,
        @Nullable List<Component> filteredMessages,
        DyeColor color,
        boolean hasGlowingText
) {
    public static final int LINES = 4;

    private static final NetworkBuffer.Type<List<Component>> MESSAGES_NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(NetworkBuffer buffer, List<Component> value) {
            for (Component message : value) buffer.write(NetworkBuffer.COMPONENT, message);
        }

        @Override
        public List<Component> read(NetworkBuffer buffer) {
            final Component[] messages = new Component[LINES];
            for (int i = 0; i < LINES; i++) messages[i] = buffer.read(NetworkBuffer.COMPONENT);
            return List.of(messages);
        }
    };

    public static final NetworkBuffer.Type<SignText> NETWORK_TYPE = NetworkBufferTemplate.template(
            MESSAGES_NETWORK_TYPE, SignText::messages,
            MESSAGES_NETWORK_TYPE.optional(), SignText::filteredMessages,
            DyeColor.NETWORK_TYPE, SignText::color,
            NetworkBuffer.BOOLEAN, SignText::hasGlowingText,
            SignText::new);

    public static final Codec<SignText> CODEC = StructCodec.struct(
            "messages", Codec.COMPONENT.list(LINES), SignText::messages,
            "filtered_messages", Codec.COMPONENT.list(LINES).optional(), SignText::filteredMessages,
            "color", DyeColor.CODEC, SignText::color,
            "has_glowing_text", Codec.BOOLEAN, SignText::hasGlowingText,
            SignText::new);

    public SignText {
        messages = List.copyOf(messages);
        if (messages.size() != LINES) {
            throw new IllegalArgumentException("Sign text must have " + LINES + " lines!");
        }
        if (filteredMessages != null) {
            filteredMessages = List.copyOf(filteredMessages);
            if (filteredMessages.size() != LINES) {
                throw new IllegalArgumentException("Filtered sign text must have " + LINES + " lines!");
            }
        }
    }
}
