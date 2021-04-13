package net.minestom.server.chat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents multiple {@link ColoredText} batched together with the possibility to add
 * click and hover events.
 * <p>
 * Used when the message can contain both colored text and event (otherwise, use {@link ColoredText}).
 * <p>
 * You will need to call the static method to initialize the message {@link #of(ColoredText)},
 * events can be assigned with {@link #setClickEvent(ChatClickEvent)} and {@link #setHoverEvent(ChatHoverEvent)}
 * and new text element can also be appended {@link #append(ColoredText)}.
 * @deprecated Use {@link Component}
 */
@Deprecated
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

        RichMessage richMessage = new RichMessage();
        appendText(richMessage, coloredText);

        return richMessage;
    }

    private static void appendText(@NotNull RichMessage richMessage, @NotNull ColoredText coloredText) {
        RichComponent component = new RichComponent(coloredText);
        richMessage.components.add(component);
        richMessage.currentComponent = component;
    }

    /**
     * Adds a new rich component to the message.
     *
     * @param coloredText the text composing the rich component
     * @return the rich message
     */
    public RichMessage append(@NotNull ColoredText coloredText) {

        appendText(this, coloredText);
        return this;
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

    @NotNull
    @Override
    public JsonObject getJsonObject() {
        List<RichComponent> cacheComponents = new ArrayList<>(components);

        // No component, return empty json object
        if (cacheComponents.isEmpty())
            return new JsonObject();

        // The main object contains the extra array, with an empty text to do not share its state with the others
        JsonObject mainObject = new JsonObject();
        mainObject.addProperty("text", "");

        // The extra array contains all the components
        JsonArray extraArray = new JsonArray();

        // Add all the components
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
                    hoverObject.add("contents", hoverEvent.getValueObject());
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

    /**
     * Represents a {@link ColoredText} with a click and hover event (can be null).
     */
    private static class RichComponent {

        private final ColoredText text;
        private ChatClickEvent clickEvent;
        private ChatHoverEvent hoverEvent;
        private String insertion;

        private RichComponent(@NotNull ColoredText text) {
            this.text = text;
        }

        @NotNull
        public ColoredText getText() {
            return text;
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
