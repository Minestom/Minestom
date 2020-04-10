package fr.themode.minestom.registry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.themode.minestom.instance.block.Block;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RegistryMain {

    public static void main(String[] args) {
        List<RegistryBlock> blocks = parseBlocks("registry/blocks.json");
        writeBlocksClass(blocks);


    }

    public static void registerBlocks() {
        List<RegistryBlock> blocks = parseBlocks("registry/blocks.json");

        for (RegistryBlock registryBlock : blocks) {
            String name = registryBlock.name.toUpperCase().replace("MINECRAFT:", "");
            Block block = Block.valueOf(name);
            block.initBlock(registryBlock.defaultId);

            for (RegistryBlock.BlockState blockState : registryBlock.states) {
                short id = blockState.id;
                String[] properties = blockState.propertiesValues.toArray(new String[registryBlock.states.size()]);
                block.addBlockAlternative(id, properties);
            }
        }

    }

    private static void writeBlocksClass(List<RegistryBlock> blocks) {
        final String prefix = "public static final Blocks ";

        for (RegistryBlock registryBlock : blocks) {
            String line = "";
            // Add block name as var name
            String name = registryBlock.name.toUpperCase().replace("MINECRAFT:", "");
            line += name;
            line += ",";

            System.out.println(line);

        }
    }


    private static List<RegistryBlock> parseBlocks(String path) {
        List<RegistryBlock> blocks = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));

            Gson gson = new Gson();
            JsonObject obj = gson.fromJson(bufferedReader, JsonObject.class);
            Set<Map.Entry<String, JsonElement>> entries = obj.entrySet();//will return members of your object
            for (Map.Entry<String, JsonElement> entry : entries) {
                RegistryBlock registryBlock = new RegistryBlock();
                blocks.add(registryBlock);

                String blockName = entry.getKey();

                registryBlock.name = blockName;

                JsonObject blockObject = entry.getValue().getAsJsonObject();
                JsonObject propertiesObject = blockObject.getAsJsonObject("properties");

                // Get all properties keys
                if (propertiesObject != null) {
                    Set<Map.Entry<String, JsonElement>> propertiesEntries = propertiesObject.entrySet();//will return members of your object
                    for (Map.Entry<String, JsonElement> propertyEntry : propertiesEntries) {
                        String propertyName = propertyEntry.getKey();

                        registryBlock.propertiesIdentifiers.add(propertyName);
                    }
                }

                // Get states
                JsonArray statesArray = blockObject.getAsJsonArray("states");
                for (JsonElement stateElement : statesArray) {
                    JsonObject stateObject = stateElement.getAsJsonObject();
                    RegistryBlock.BlockState blockState = new RegistryBlock.BlockState();
                    registryBlock.states.add(blockState);

                    short id = stateObject.get("id").getAsShort();
                    boolean isDefault = stateObject.has("default");

                    blockState.id = id;
                    blockState.isDefault = isDefault;

                    JsonObject statePropertiesObject = stateObject.getAsJsonObject("properties");
                    if (statePropertiesObject != null) {
                        Set<Map.Entry<String, JsonElement>> statePropertiesEntries = statePropertiesObject.entrySet();//will return members of your object
                        for (Map.Entry<String, JsonElement> propertyEntry : statePropertiesEntries) {
                            String propertyValue = propertyEntry.getValue().getAsString();

                            blockState.propertiesValues.add(propertyValue);
                        }
                    }

                    // Fill the default information
                    if (isDefault) {
                        registryBlock.defaultId = blockState.id;
                        registryBlock.defaultPropertiesValues = blockState.propertiesValues;
                    }
                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        return blocks;
    }

}
