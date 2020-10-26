package net.minestom.server.chat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// TODO format retention

/**
 * Represents multiple {@link ColoredText} batched together with the possibility to add
 * click and hover events.
 * <p>
 * Used when the message can contain both colored text and event (otherwise, use {@link ColoredText}).
 * <p>
 * You will need to call the static method to initialize the message {@link #of(ColoredText)},
 * events can be assigned with {@link #setClickEvent(ChatClickEvent)} and {@link #setHoverEvent(ChatHoverEvent)}
 * and new text element can also be appended {@link #append(ColoredText)}.
 */
public class RichMessage extends JsonMessage {

    private final List<RichComponent> components = new ArrayList<>();
    private RichComponent currentComponent;

    /**
     * @see #of(ColoredText) to create a rich message
     */
    private RichMessage() {
    }

    /**
     * Creates a {@link RichMessage} by adding the first rich component.
     *
     * @param coloredText the text composing the first rich component
     * @return the created rich message object
     */
    public static RichMessage of(@NotNull ColoredText coloredText) {
        Check.notNull(coloredText, "ColoredText cannot be null");

        RichMessage richMessage = new RichMessage();
        appendText(richMessage, coloredText, FormatRetention.ALL);

        return richMessage;
    }

    private static void appendText(@NotNull RichMessage richMessage, @NotNull ColoredText coloredText,
                                   @NotNull FormatRetention formatRetention) {
        RichComponent component = new RichComponent(coloredText, formatRetention);
        richMessage.components.add(component);
        richMessage.currentComponent = component;
    }

    /**
     * Sets the click event of the current rich component.
     *
     * @param clickEvent the click event to set
     * @return the rich message
     */
    public RichMessage setClickEvent(@Nullable ChatClickEvent clickEvent) {
        currentComponent.setClickEvent(clickEvent);
        return this;
    }

    /**
     * Sets the hover event of the current rich component.
     *
     * @param hoverEvent the hover event to set
     * @return the rich message
     */
    public RichMessage setHoverEvent(@Nullable ChatHoverEvent hoverEvent) {
        currentComponent.setHoverEvent(hoverEvent);
        return this;
    }

    /**
     * Sets the insertion string of the current rich component.
     *
     * @param insertion the string to insert in the chat box
     * @return the rich message
     */
    public RichMessage setInsertion(@Nullable String insertion) {
        currentComponent.setInsertion(insertion);
        return this;
    }

    /**
     * Adds a new rich component to the message.
     *
     * @param coloredText     the text composing the rich component
     * @param formatRetention the format retention of the added component
     * @return the rich message
     */
    public RichMessage append(@NotNull ColoredText coloredText, @NotNull FormatRetention formatRetention) {
        Check.notNull(coloredText, "ColoredText cannot be null");

        appendText(this, coloredText, formatRetention);
        return this;
    }

    /**
     * Adds a new rich component to the message,
     * the format retention is set to {@link FormatRetention#ALL}.
     *
     * @param coloredText the text composing the rich component
     * @return the rich message
     */
    public RichMessage append(@NotNull ColoredText coloredText) {
        return append(coloredText, FormatRetention.ALL);
    }

    @NotNull
    @Override
    public JsonObject getJsonObject() {
        List<RichComponent> cacheComponents = new ArrayList<>(components);

        // No component, return empty json object
        if (cacheComponents.isEmpty())
            return new JsonObject();

        RichComponent firstComponent = cacheComponents.remove(0);
        List<JsonObject> firstComponentObjects = getComponentObject(firstComponent);
        JsonObject mainObject = firstComponentObjects.remove(0);

        if (cacheComponents.isEmpty() && firstComponentObjects.isEmpty())
            return mainObject;

        JsonArray extraArray = new JsonArray();
        for (JsonObject firstComponentObject : firstComponentObjects) {
            extraArray.add(firstComponentObject);
        }

        for (RichComponent component : cacheComponents) {
            List<JsonObject> componentObjects = getComponentObject(component);
            for (JsonObject componentObject : componentObjects) {
                extraArray.add(componentObject);
            }
        }

        mainObject.add("extra", extraArray);


        return mainObject;
    }

    /**
     * Processes the components to add click/hover events.
     *
     * @param component the rich component to process
     * @return a list of processed components
     */
    private List<JsonObject> getComponentObject(@NotNull RichComponent component) {
        final ColoredText coloredText = component.getText();
        final List<JsonObject> componentObjects = coloredText.getComponents();

        final ChatClickEvent clickEvent = component.getClickEvent();
        final ChatHoverEvent hoverEvent = component.getHoverEvent();
        final String insertion = component.getInsertion();

        // Nothing to process
        if (clickEvent == null && hoverEvent == null && insertion == null) {
            return componentObjects;
        }

        // Add hover/click event to all components
        for (JsonObject componentObject : componentObjects) {
            // Add click event if any
            if (clickEvent != null) {
                final JsonObject clickObject =
                        getEventObject(clickEvent.getAction(), clickEvent.getValue());
                componentObject.add("clickEvent", clickObject);
            }
            // Add hover event if any
            if (hoverEvent != null) {
                final JsonObject hoverObject;
                if (hoverEvent.isJson()) {
                    // The value is a JsonObject
                    hoverObject = new JsonObject();
                    hoverObject.addProperty("action", hoverEvent.getAction());
                    hoverObject.add("value", hoverEvent.getValueObject());
                } else {
                    // The value is a raw string
                    final String hoverValue = hoverEvent.getValue();
                    Check.notNull(hoverValue, "The hover value cannot be null");
                    hoverObject = getEventObject(hoverEvent.getAction(), hoverValue);
                }
                componentObject.add("hoverEvent", hoverObject);
            }
            // Add insertion if any
            if (insertion != null) {
                componentObject.addProperty("insertion", insertion);
            }
        }

        return componentObjects;
    }

    private JsonObject getEventObject(@NotNull String action, @NotNull String value) {
        JsonObject eventObject = new JsonObject();
        eventObject.addProperty("action", action);
        eventObject.addProperty("value", value);
        return eventObject;
    }

    public enum FormatRetention {
        ALL, CLICK_EVENT, HOVER_EVENT, NONE
    }

    /**
     * Represents a {@link ColoredText} with a click and hover event (can be null).
     */
    private static class RichComponent {

        private final ColoredText text;
        private final FormatRetention formatRetention;
        private ChatClickEvent clickEvent;
        private ChatHoverEvent hoverEvent;
        private String insertion;

        private RichComponent(@NotNull ColoredText text, @NotNull FormatRetention formatRetention) {
            this.text = text;
            this.formatRetention = formatRetention;
        }

        @NotNull
        public ColoredText getText() {
            return text;
        }

        @NotNull
        public FormatRetention getFormatRetention() {
            return formatRetention;
        }

        @Nullable
        public ChatClickEvent getClickEvent() {
            return clickEvent;
        }

        public void setClickEvent(@Nullable ChatClickEvent clickEvent) {
            this.clickEvent = clickEvent;
        }

        public ChatHoverEvent getHoverEvent() {
            return hoverEvent;
        }

        public void setHoverEvent(@Nullable ChatHoverEvent hoverEvent) {
            this.hoverEvent = hoverEvent;
        }

        @Nullable
        public String getInsertion() {
            return insertion;
        }

        public void setInsertion(@Nullable String insertion) {
            this.insertion = insertion;
        }
    }

}
