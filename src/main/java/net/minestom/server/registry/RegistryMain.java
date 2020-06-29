package net.minestom.server.registry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockAlternative;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.Material;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.PotionType;
import net.minestom.server.sound.Sound;
import net.minestom.server.stat.StatisticType;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RegistryMain {

    public static final String BLOCKS_PATH = "minecraft_data/reports/blocks.json";
    public static final String ITEMS_PATH = "minecraft_data/reports/registries.json";
    public static final String ENTITIES_PATH = "minecraft_data/reports/registries.json";
    public static final String SOUNDS_PATH = "minecraft_data/reports/registries.json";
    public static final String PARTICLES_PATH = "minecraft_data/reports/registries.json";
    public static final String STATS_PATH = "minecraft_data/reports/registries.json";

    public static void main(String[] args) {
        List<RegistryBlock> blocks = parseBlocks(BLOCKS_PATH);
        List<RegistryItem> items = parseItems(ITEMS_PATH);
        List<RegistryEntityType> entities = parseEntities(ENTITIES_PATH);
        List<RegistrySound> sounds = parseSounds(SOUNDS_PATH);
        List<RegistryParticle> particles = parseParticles(PARTICLES_PATH);
        List<RegistryStat> stats = parseStats(STATS_PATH);
        List<RegistryEnchantment> enchantments = parseEnchantments(STATS_PATH);
        List<RegistryPotion> potions = parsePotions(STATS_PATH);
        //writeBlocksClass(blocks);
        //writeItemsClass(items);
        //writeEntitiesClass(entities);
        //writeSoundsClass(sounds);
        //writeStatsClass(stats);
        //writeEnchantmentsClass(enchantments);
        writePotionsClass(potions);
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

    public static void registerStats() {
        List<RegistryStat> registryStats = parseStats(STATS_PATH);

        for (RegistryStat registryStat : registryStats) {
            StatisticType stat = StatisticType.valueOf(registryStat.name);
            stat.setIdentifier(registryStat.id);
        }
    }

    public static void registerEnchantments() {
        List<RegistryEnchantment> enchantments = parseEnchantments(STATS_PATH);

        for (RegistryEnchantment registryEnchantment : enchantments) {
            Enchantment enchantment = Enchantment.valueOf(registryEnchantment.name);
            enchantment.setIdentifier(registryEnchantment.id);
        }
    }

    public static void registerPotions() {
        List<RegistryPotion> potions = parsePotions(STATS_PATH);

        for (RegistryPotion registryPotion : potions) {
            PotionType potionType = PotionType.valueOf(registryPotion.name);
            potionType.setIdentifier(registryPotion.id);
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

    private static void writeStatsClass(List<RegistryStat> stats) {
        for (RegistryStat registryStat : stats) {
            String line = registryStat.name + ",";
            System.out.println(line);
        }
    }

    private static void writeEnchantmentsClass(List<RegistryEnchantment> enchantments) {
        for (RegistryEnchantment registryEnchantment : enchantments) {
            String line = registryEnchantment.name + ",";
            System.out.println(line);
        }
    }

    private static void writePotionsClass(List<RegistryPotion> potions) {
        for (RegistryPotion registryPotion : potions) {
            String line = registryPotion.name + ",";
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
                            String propertyValue = propertyEntry.getKey() + "=" + propertyEntry.getValue().getAsString();

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
        JsonObject entriesObject = parse(path, "minecraft:item");
        List<RegistryItem> registryItems = new ArrayList<>();

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
        JsonObject entriesObject = parse(path, "minecraft:entity_type");
        List<RegistryEntityType> registryEntityTypes = new ArrayList<>();

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
        JsonObject entriesObject = parse(path, "minecraft:sound_event");
        List<RegistrySound> registrySounds = new ArrayList<>();

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
        JsonObject entriesObject = parse(path, "minecraft:particle_type");
        List<RegistryParticle> registryParticles = new ArrayList<>();

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

    private static List<RegistryStat> parseStats(String path) {
        JsonObject entriesObject = parse(path, "minecraft:custom_stat");
        List<RegistryStat> registryStats = new ArrayList<>();

        Set<Map.Entry<String, JsonElement>> entriesEntries = entriesObject.entrySet();//will return members of your object
        for (Map.Entry<String, JsonElement> entryEntry : entriesEntries) {
            RegistryStat registryStat = new RegistryStat();
            registryStats.add(registryStat);
            String item = entryEntry.getKey();
            String itemName = item.toUpperCase().replace("MINECRAFT:", "").replace(".", "_");
            registryStat.name = itemName;
            short id = entryEntry.getValue().getAsJsonObject().get("protocol_id").getAsShort();
            registryStat.id = id;
        }

        return registryStats;
    }

    private static List<RegistryEnchantment> parseEnchantments(String path) {
        JsonObject entriesObject = parse(path, "minecraft:enchantment");
        List<RegistryEnchantment> registryEnchantments = new ArrayList<>();

        Set<Map.Entry<String, JsonElement>> entriesEntries = entriesObject.entrySet();//will return members of your object
        for (Map.Entry<String, JsonElement> entryEntry : entriesEntries) {
            RegistryEnchantment registryEnchantment = new RegistryEnchantment();
            registryEnchantments.add(registryEnchantment);
            String item = entryEntry.getKey();
            String itemName = item.toUpperCase().replace("MINECRAFT:", "").replace(".", "_");
            registryEnchantment.name = itemName;
            short id = entryEntry.getValue().getAsJsonObject().get("protocol_id").getAsShort();
            registryEnchantment.id = id;
        }

        return registryEnchantments;
    }

    private static List<RegistryPotion> parsePotions(String path) {
        JsonObject entriesObject = parse(path, "minecraft:potion");
        List<RegistryPotion> registryPotions = new ArrayList<>();

        Set<Map.Entry<String, JsonElement>> entriesEntries = entriesObject.entrySet();//will return members of your object
        for (Map.Entry<String, JsonElement> entryEntry : entriesEntries) {
            RegistryPotion registryPotion = new RegistryPotion();
            registryPotions.add(registryPotion);
            String item = entryEntry.getKey();
            String itemName = item.toUpperCase().replace("MINECRAFT:", "").replace(".", "_");
            registryPotion.name = itemName;
            short id = entryEntry.getValue().getAsJsonObject().get("protocol_id").getAsShort();
            registryPotion.id = id;
        }

        return registryPotions;
    }

    private static JsonObject parse(String path, String key) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        JsonObject obj = gson.fromJson(bufferedReader, JsonObject.class);

        JsonObject itemsObject = obj.getAsJsonObject(key);
        JsonObject entriesObject = itemsObject.getAsJsonObject("entries");

        return entriesObject;
    }

}
