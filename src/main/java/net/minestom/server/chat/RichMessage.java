package net.minestom.server.chat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minestom.server.utils.validate.Check;

import java.util.ArrayList;
import java.util.List;

// TODO format retention

/**
 * Represent multiple {@link ColoredText} batched together with the possibility to add
 * click and hover events
 * <p>
 * Used when the message can contain both colored text and event (otherwise, use {@link ColoredText})
 */
public class RichMessage extends JsonMessage {

    private List<RichComponent> components = new ArrayList<>();
    private RichComponent currentComponent;

    /**
     * Create a RichMessage by adding the first rich component
     *
     * @param coloredText the text composing the first rich component
     * @return the created rich message object
     */
    public static RichMessage of(ColoredText coloredText) {
        Check.notNull(coloredText, "ColoredText cannot be null");

        RichMessage richMessage = new RichMessage();
        appendText(richMessage, coloredText, FormatRetention.ALL);

        return richMessage;
    }

    private static void appendText(RichMessage richMessage, ColoredText coloredText, FormatRetention formatRetention) {
        RichComponent component = new RichComponent(coloredText, formatRetention);
        richMessage.components.add(component);
        richMessage.currentComponent = component;
    }

    /**
     * Set the click event of the current rich component
     *
     * @param clickEvent the click event to set
     * @return the rich message
     */
    public RichMessage setClickEvent(ChatClickEvent clickEvent) {
        Check.notNull(clickEvent, "ChatClickEvent cannot be null");

        currentComponent.setClickEvent(clickEvent);
        return this;
    }

    /**
     * Set the hover event of the current rich component
     *
     * @param hoverEvent the hover event to set
     * @return the rich message
     */
    public RichMessage setHoverEvent(ChatHoverEvent hoverEvent) {
        Check.notNull(hoverEvent, "ChatHoverEvent cannot be null");

        currentComponent.setHoverEvent(hoverEvent);
        return this;
    }

    /**
     * Set the insertion string of the current rich component
     *
     * @param insertion the string to insert in the chat box
     * @return the rich message
     */
    public RichMessage setInsertion(String insertion) {
        Check.notNull(insertion, "the insertion cannot be null");

        currentComponent.setInsertion(insertion);
        return this;
    }

    /**
     * Add a new rich component to the message
     *
     * @param coloredText     the text composing the rich component
     * @param formatRetention the format retention of the added component
     * @return the rich message
     */
    public RichMessage append(ColoredText coloredText, FormatRetention formatRetention) {
        Check.notNull(coloredText, "ColoredText cannot be null");

        appendText(this, coloredText, formatRetention);
        return this;
    }

    /**
     * Add a new rich component to the message,
     * the format retention is set to {@link FormatRetention#ALL}
     *
     * @param coloredText the text composing the rich component
     * @return the rich message
     */
    public RichMessage append(ColoredText coloredText) {
        return append(coloredText, FormatRetention.ALL);
    }

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
     * Process the components to add click/hover events
     *
     * @param component the rich component to process
     * @return a list of processed components
     */
    private List<JsonObject> getComponentObject(RichComponent component) {
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
                    hoverObject = getEventObject(hoverEvent.getAction(), hoverEvent.getValue());
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

    private JsonObject getEventObject(String action, String value) {
        JsonObject eventObject = new JsonObject();
        eventObject.addProperty("action", action);
        eventObject.addProperty("value", value);
        return eventObject;
    }

    public enum FormatRetention {
        ALL, CLICK_EVENT, HOVER_EVENT, NONE
    }

    /**
     * Represent a colored text with a click and hover event (can be null)
     */
    private static class RichComponent {

        private ColoredText text;
        private FormatRetention formatRetention;
        private ChatClickEvent clickEvent;
        private ChatHoverEvent hoverEvent;
        private String insertion;

        private RichComponent(ColoredText text, FormatRetention formatRetention) {
            this.text = text;
            this.formatRetention = formatRetention;
        }

        public ColoredText getText() {
            return text;
        }

        public FormatRetention getFormatRetention() {
            return formatRetention;
        }

        public ChatClickEvent getClickEvent() {
            return clickEvent;
        }

        public void setClickEvent(ChatClickEvent clickEvent) {
            this.clickEvent = clickEvent;
        }

        public ChatHoverEvent getHoverEvent() {
            return hoverEvent;
        }

        public void setHoverEvent(ChatHoverEvent hoverEvent) {
            this.hoverEvent = hoverEvent;
        }

        public String getInsertion() {
            return insertion;
        }

        public void setInsertion(String insertion) {
            this.insertion = insertion;
        }
    }

}
