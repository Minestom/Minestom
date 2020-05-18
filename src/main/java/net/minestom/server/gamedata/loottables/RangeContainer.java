package net.minestom.server.gamedata.loottables;

import com.google.gson.*;

import java.lang.reflect.Type;

public class RangeContainer {

    private int min;
    private int max;

    RangeContainer() {}

    public RangeContainer(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public static class Deserializer implements JsonDeserializer<RangeContainer> {

        @Override
        public RangeContainer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            RangeContainer range = new RangeContainer();
            if(json.isJsonPrimitive()) {
                range.min = json.getAsInt();
                range.max = json.getAsInt();
            } else if(json.isJsonObject()) {
                JsonObject obj = json.getAsJsonObject();
                if(!obj.has("min"))
                    throw new IllegalArgumentException("Missing 'min' property");
                if(!obj.has("max"))
                    throw new IllegalArgumentException("Missing 'max' property");
                range.min = obj.get("min").getAsInt();
                range.max = obj.get("max").getAsInt();
            } else {
                throw new IllegalArgumentException("Range must be single integer or an object with 'min' and 'max' properties");
            }
            return range;
        }
    }
}
