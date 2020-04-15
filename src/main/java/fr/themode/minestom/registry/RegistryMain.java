package fr.themode.minestom.registry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.themode.minestom.entity.EntityType;
import fr.themode.minestom.instance.block.Block;
import fr.themode.minestom.item.Material;
import fr.themode.minestom.particle.Particle;
import fr.themode.minestom.sound.Sound;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RegistryMain {

    public static final String BLOCKS_PATH = "registry/blocks.json";
    public static final String ITEMS_PATH = "registry/registries.json";
    public static final String ENTITIES_PATH = "registry/registries.json";
    public static final String SOUNDS_PATH = "registry/registries.json";
    public static final String PARTICLES_PATH = "registry/registries.json";

    public static void main(String[] args) {
        List<RegistryBlock> blocks = parseBlocks(BLOCKS_PATH);
        List<RegistryItem> items = parseItems(ITEMS_PATH);
        List<RegistryEntityType> entities = parseEntities(ENTITIES_PATH);
        List<RegistrySound> sounds = parseSounds(SOUNDS_PATH);
        List<RegistryParticle> particles = parseParticles(PARTICLES_PATH);
        //writeBlocksClass(blocks);
        //writeItemsClass(items);
        //writeEntitiesClass(entities);
        //writeSoundsClass(sounds);
        writeParticlesClass(particles);
    }

    public static void registerBlocks() {
        List<RegistryBlock> blocks = parseBlocks(BLOCKS_PATH);

        for (RegistryBlock registryBlock : blocks) {
            String name = registryBlock.name;
            Block block = Block.valueOf(name);
            block.initBlock(registryBlock.defaultId);

            for (RegistryBlock.BlockState blockState : registryBlock.states) {
                short id = blockState.id;
                String[] properties = blockState.propertiesValues.toArray(new String[blockState.propertiesValues.size()]);
                block.addBlockAlternative(id, properties);
            }
        }
    }

    public static void registerItems() {
        List<RegistryItem> items = parseItems(ITEMS_PATH);

        for (RegistryItem registryItem : items) {
            Material material = Material.valueOf(registryItem.name);
            try {
                Block block = Block.valueOf(registryItem.name);
                material.setIdentifier(registryItem.itemId, block);
            } catch (IllegalArgumentException e) {
                switch (material) {
                    case REDSTONE:
                        material.setIdentifier(registryItem.itemId, Block.REDSTONE_WIRE);
                        break;
                    default:
                        material.setIdentifier(registryItem.itemId, null);
                        break;
                }
            }
        }
    }

    public static void registerEntities() {
        List<RegistryEntityType> registryEntityTypes = parseEntities(ENTITIES_PATH);

        for (RegistryEntityType registryEntityType : registryEntityTypes) {
            EntityType entity = EntityType.valueOf(registryEntityType.name);
            entity.setIdentifier(registryEntityType.entityId);
        }
    }

    public static void registerSounds() {
        List<RegistrySound> registrySounds = parseSounds(SOUNDS_PATH);

        for (RegistrySound registrySound : registrySounds) {
            Sound sound = Sound.valueOf(registrySound.name);
            sound.setIdentifier(registrySound.id);
        }
    }

    public static void registerParticles() {
        List<RegistryParticle> registryParticles = parseParticles(PARTICLES_PATH);

        for (RegistryParticle registryParticle : registryParticles) {
            Particle particle = Particle.valueOf(registryParticle.name);
            particle.setIdentifier(registryParticle.id);
        }
    }

    private static void writeBlocksClass(List<RegistryBlock> blocks) {
        for (RegistryBlock registryBlock : blocks) {
            String line = registryBlock.name + ",";
            System.out.println(line);

        }
    }

    private static void writeItemsClass(List<RegistryItem> items) {
        for (RegistryItem registryItem : items) {
            String line = registryItem.name + ",";
            System.out.println(line);
        }
    }

    private static void writeEntitiesClass(List<RegistryEntityType> entities) {
        for (RegistryEntityType registryEntityType : entities) {
            String line = registryEntityType.name + ",";
            System.out.println(line);
        }
    }

    private static void writeSoundsClass(List<RegistrySound> sounds) {
        for (RegistrySound registrySound : sounds) {
            String line = registrySound.name + ",";
            System.out.println(line);
        }
    }

    private static void writeParticlesClass(List<RegistryParticle> particles) {
        for (RegistryParticle registryParticle : particles) {
            String line = registryParticle.name + ",";
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

                registryBlock.name = blockName.toUpperCase().replace("MINECRAFT:", "");

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

    private static List<RegistryItem> parseItems(String path) {
        List<RegistryItem> registryItems = new ArrayList<>();

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        JsonObject obj = gson.fromJson(bufferedReader, JsonObject.class);

        JsonObject itemsObject = obj.getAsJsonObject("minecraft:item");
        JsonObject entriesObject = itemsObject.getAsJsonObject("entries");

        Set<Map.Entry<String, JsonElement>> entriesEntries = entriesObject.entrySet();//will return members of your object
        for (Map.Entry<String, JsonElement> entryEntry : entriesEntries) {
            RegistryItem registryItem = new RegistryItem();
            registryItems.add(registryItem);
            String item = entryEntry.getKey();
            String itemName = item.toUpperCase().replace("MINECRAFT:", "");
            registryItem.name = itemName;
            short id = entryEntry.getValue().getAsJsonObject().get("protocol_id").getAsShort();
            registryItem.itemId = id;
        }

        return registryItems;
    }

    private static List<RegistryEntityType> parseEntities(String path) {
        List<RegistryEntityType> registryEntityTypes = new ArrayList<>();

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        JsonObject obj = gson.fromJson(bufferedReader, JsonObject.class);

        JsonObject itemsObject = obj.getAsJsonObject("minecraft:entity_type");
        JsonObject entriesObject = itemsObject.getAsJsonObject("entries");

        Set<Map.Entry<String, JsonElement>> entriesEntries = entriesObject.entrySet();//will return members of your object
        for (Map.Entry<String, JsonElement> entryEntry : entriesEntries) {
            RegistryEntityType registryEntityType = new RegistryEntityType();
            registryEntityTypes.add(registryEntityType);
            String item = entryEntry.getKey();
            String itemName = item.toUpperCase().replace("MINECRAFT:", "");
            registryEntityType.name = itemName;
            short id = entryEntry.getValue().getAsJsonObject().get("protocol_id").getAsShort();
            registryEntityType.entityId = id;
        }

        return registryEntityTypes;
    }

    private static List<RegistrySound> parseSounds(String path) {
        List<RegistrySound> registrySounds = new ArrayList<>();

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        JsonObject obj = gson.fromJson(bufferedReader, JsonObject.class);

        JsonObject itemsObject = obj.getAsJsonObject("minecraft:sound_event");
        JsonObject entriesObject = itemsObject.getAsJsonObject("entries");

        Set<Map.Entry<String, JsonElement>> entriesEntries = entriesObject.entrySet();//will return members of your object
        for (Map.Entry<String, JsonElement> entryEntry : entriesEntries) {
            RegistrySound registrySound = new RegistrySound();
            registrySounds.add(registrySound);
            String item = entryEntry.getKey();
            String itemName = item.toUpperCase().replace("MINECRAFT:", "").replace(".", "_");
            registrySound.name = itemName;
            short id = entryEntry.getValue().getAsJsonObject().get("protocol_id").getAsShort();
            registrySound.id = id;
        }

        return registrySounds;
    }

    private static List<RegistryParticle> parseParticles(String path) {
        List<RegistryParticle> registryParticles = new ArrayList<>();

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        JsonObject obj = gson.fromJson(bufferedReader, JsonObject.class);

        JsonObject itemsObject = obj.getAsJsonObject("minecraft:particle_type");
        JsonObject entriesObject = itemsObject.getAsJsonObject("entries");

        Set<Map.Entry<String, JsonElement>> entriesEntries = entriesObject.entrySet();//will return members of your object
        for (Map.Entry<String, JsonElement> entryEntry : entriesEntries) {
            RegistryParticle registryParticle = new RegistryParticle();
            registryParticles.add(registryParticle);
            String item = entryEntry.getKey();
            String itemName = item.toUpperCase().replace("MINECRAFT:", "").replace(".", "_");
            registryParticle.name = itemName;
            short id = entryEntry.getValue().getAsJsonObject().get("protocol_id").getAsShort();
            registryParticle.id = id;
        }

        return registryParticles;
    }

}
