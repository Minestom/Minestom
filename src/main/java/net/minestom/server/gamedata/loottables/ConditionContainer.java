package net.minestom.server.gamedata.loottables;

import com.google.gson.*;
import net.minestom.server.gamedata.Condition;
import net.minestom.server.utils.NamespaceID;

import java.lang.reflect.Type;

public abstract class ConditionContainer {
    private ConditionContainer() {}

    public abstract Condition create(LootTableManager lootTableManager);


    static class Deserializer implements JsonDeserializer<ConditionContainer> {

        private final LootTableManager lootTableManager;

        Deserializer(LootTableManager lootTableManager) {
            this.lootTableManager = lootTableManager;
        }

        @Override
        public ConditionContainer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            String type = obj.get("condition").getAsString();
            JsonDeserializer<? extends Condition> deserializer = lootTableManager.getConditionDeserializer(NamespaceID.from(type));
            return new ConditionContainer() {
                @Override
                public Condition create(LootTableManager lootTableManager) {
                    return deserializer.deserialize(obj, typeOfT, context);
                }
            };
        }
    }
}
