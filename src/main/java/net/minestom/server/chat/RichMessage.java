package net.minestom.server.chat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minestom.server.utils.validate.Check;

import java.util.ArrayList;
import java.util.List;

public class RichMessage {

    private List<RichComponent> components = new ArrayList<>();
    private RichComponent currentComponent;

    public static RichMessage of(ColoredText coloredText, FormatRetention formatRetention) {
        Check.notNull(coloredText, "ColoredText cannot be null");

        RichMessage richMessage = new RichMessage();
        appendText(richMessage, coloredText, formatRetention);

        return richMessage;
    }

    public static RichMessage of(ColoredText coloredText) {
        return of(coloredText, FormatRetention.ALL);
    }

    private static void appendText(RichMessage richMessage, ColoredText coloredText, FormatRetention formatRetention) {
        RichComponent component = new RichComponent(coloredText, formatRetention);
        richMessage.components.add(component);
        richMessage.currentComponent = component;
    }

    public RichMessage setClickEvent(ChatClickEvent clickEvent) {
        Check.notNull(clickEvent, "ChatClickEvent cannot be null");

        currentComponent.setClickEvent(clickEvent);
        return this;
    }

    public RichMessage setHoverEvent(ChatHoverEvent hoverEvent) {
        Check.notNull(hoverEvent, "ChatHoverEvent cannot be null");

        currentComponent.setHoverEvent(hoverEvent);
        return this;
    }

    public RichMessage append(ColoredText coloredText, FormatRetention formatRetention) {
        Check.notNull(coloredText, "ColoredText cannot be null");

        appendText(this, coloredText, formatRetention);
        return this;
    }

    public RichMessage append(ColoredText coloredText) {
        return append(coloredText, FormatRetention.ALL);
    }

    @Override
    public String toString() {
        return getJsonObject().toString();
    }

    private JsonObject getJsonObject() {
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

    private List<JsonObject> getComponentObject(RichComponent component) {
        ColoredText coloredText = component.getText();
        List<JsonObject> componentObjects = coloredText.getComponents();

        ChatClickEvent clickEvent = component.getClickEvent();
        ChatHoverEvent hoverEvent = component.getHoverEvent();

        // Nothing to process
        if (clickEvent == null && hoverEvent == null) {
            return componentObjects;
        }

        for (JsonObject componentObject : componentObjects) {
            if (clickEvent != null) {
                final JsonObject clickObject =
                        getEventObject(clickEvent.getAction(), clickEvent.getValue());
                componentObject.add("clickEvent", clickObject);
            }
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

    private static class RichComponent {

        private ColoredText text;
        private FormatRetention formatRetention;
        private ChatClickEvent clickEvent;
        private ChatHoverEvent hoverEvent;

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
    }

}
