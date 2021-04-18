package net.minestom.server.raw_data;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.villager.VillagerProfession;
import net.minestom.server.fluid.Fluid;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockState;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.Material;
import net.minestom.server.map.MapColors;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class DataInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static void runDataInitializer(File inputFolder, String version) {
        loadBlockData(new File(inputFolder, version.replaceAll("\\.", "_") + "_blocks.json"));
        loadFluidData(new File(inputFolder, version.replaceAll("\\.", "_") + "_fluids.json"));
        loadEntityData(new File(inputFolder, version.replaceAll("\\.", "_") + "_entities.json"));
        loadMaterialData(new File(inputFolder, version.replaceAll("\\.", "_") + "_items.json"));
        loadEnchantmentData(new File(inputFolder, version.replaceAll("\\.", "_") + "_enchantments.json"));
        loadVillagerProfessionData(new File(inputFolder, version.replaceAll("\\.", "_") + "_villager_professions.json"));
    }

    private static void loadBlockData(@NotNull File blocksFile) {
        if (!blocksFile.getAbsoluteFile().exists()) {
            LOGGER.error("Failed to find blocks.json.");
            LOGGER.error("Stopped raw data engine for blocks.");
            return;
        }
        JsonArray blocksJson;
        try {
            blocksJson = GSON.fromJson(new JsonReader(new FileReader(blocksFile)), JsonArray.class);
        } catch (FileNotFoundException e) {
            LOGGER.error("Failed to find blocks.json.");
            LOGGER.error("Stopped raw data engine for blocks.");
            return;
        }
        for (JsonElement jsonElement : blocksJson) {
            // Load data
            JsonObject blockJson = jsonElement.getAsJsonObject();
            NamespaceID blockId = NamespaceID.from(blockJson.get("id").getAsString());

            // Get correct block
            Block b = Registries.getBlock(blockId);
            // CASE: Returning the defaulted value, however make sure to include the defaulted data itself.
            // to clarify: We return AIR if it doesn't exist, but we also need Air in the HashMap
            // Therefore we check the id for this functionality.
            if (b == Block.AIR && !blockId.equals(Block.AIR.getId())) {
                // This should honestly never happen as the values in the Registry are based on the json file.
                continue;
            }

            RawBlockData blockData = b.getBlockData();

            blockData.explosionResistance = blockJson.get("explosionResistance").getAsDouble();
            blockData.friction = blockJson.get("friction").getAsDouble();
            blockData.speedFactor = blockJson.get("speedFactor").getAsDouble();
            blockData.jumpFactor = blockJson.get("jumpFactor").getAsDouble();
            blockData.item = Registries.getMaterial(blockJson.get("itemId").getAsString());

            JsonArray blockStatesJson = blockJson.get("states").getAsJsonArray();
            for (JsonElement jsonElement2 : blockStatesJson) {
                JsonObject blockStateJson = jsonElement2.getAsJsonObject();
                short stateId = blockStateJson.get("id").getAsShort();

                // Get correct Blockstate
                BlockState bs = Registries.getBlockState(stateId);
                // CASE: Returning the defaulted value, however make sure to include the defaulted data itself.
                if (bs == BlockState.AIR_0 && stateId != BlockState.AIR_0.getId()) {
                    // This should honestly never happen as the values in the Registry are based on the json file.
                    continue;
                }

                RawBlockStateData blockStateData = bs.getBlockStateData();

                blockStateData.destroySpeed = blockStateJson.get("destroySpeed").getAsDouble();
                blockStateData.lightEmission = blockStateJson.get("lightEmission").getAsInt();
                blockStateData.occluding = blockStateJson.get("doesOcclude").getAsBoolean();
                blockStateData.pushReaction = blockStateJson.get("pushReaction").getAsString();
                blockStateData.blocksMotion = blockStateJson.get("blocksMotion").getAsBoolean();
                blockStateData.flammable = blockStateJson.get("isFlammable").getAsBoolean();
                blockStateData.liquid = blockStateJson.get("isLiquid").getAsBoolean();
                blockStateData.replaceable = blockStateJson.get("isReplaceable").getAsBoolean();
                blockStateData.solid = blockStateJson.get("isSolid").getAsBoolean();
                blockStateData.solidBlocking = blockStateJson.get("isSolidBlocking").getAsBoolean();
                blockStateData.mapColor = MapColors.values()[blockStateJson.get("mapColorId").getAsInt()];
                blockStateData.boundingBox = blockStateJson.get("boundingBox").getAsString();
            }
        }
    }

    private static void loadFluidData(@NotNull File fluidsFile) {
        if (!fluidsFile.exists()) {
            LOGGER.error("Failed to find fluids.json.");
            LOGGER.error("Stopped raw data engine for fluids.");
            return;
        }
        JsonArray fluidsJson;
        try {
            fluidsJson = GSON.fromJson(new JsonReader(new FileReader(fluidsFile)), JsonArray.class);
        } catch (FileNotFoundException e) {
            LOGGER.error("Failed to find fluids.json.");
            LOGGER.error("Stopped raw data engine for fluids.");
            return;
        }
        for (JsonElement jsonElement : fluidsJson) {
            // Load data
            JsonObject fluidJson = jsonElement.getAsJsonObject();
            NamespaceID fluidId = NamespaceID.from(fluidJson.get("id").getAsString());

            // Get correct fluid
            Fluid f = Registries.getFluid(fluidId);
            // CASE: Returning the defaulted value, however make sure to include the defaulted data itself.
            // to clarify: We return AIR if it doesn't exist, but we also need Air in the HashMap
            // Therefore we check the id for this functionality.
            if (f == Fluid.EMPTY && !fluidId.equals(Fluid.EMPTY.getId())) {
                // This should honestly never happen as the values in the Registry are based on the json file.
                continue;
            }

            RawFluidData fluidData = f.getFluidData();

            fluidData.bucketItem = Registries.getMaterial(fluidJson.get("bucketId").getAsString());
        }
    }

    private static void loadEntityData(@NotNull File entitiesFile) {
        if (!entitiesFile.exists()) {
            LOGGER.error("Failed to find entities.json.");
            LOGGER.error("Stopped raw data engine for entities.");
            return;
        }
        JsonArray entitiesJson;
        try {
            entitiesJson = GSON.fromJson(new JsonReader(new FileReader(entitiesFile)), JsonArray.class);
        } catch (FileNotFoundException e) {
            LOGGER.error("Failed to find entities.json.");
            LOGGER.error("Stopped raw data engine for entities.");
            return;
        }
        for (JsonElement jsonElement : entitiesJson) {
            // Load data
            JsonObject entityJson = jsonElement.getAsJsonObject();
            NamespaceID entityId = NamespaceID.from(entityJson.get("id").getAsString());

            // Get correct item
            EntityType et = Registries.getEntityType(entityId);
            if (et == null) {
                // shouldn't happen
                continue;
            }

            RawEntityTypeData entityData = et.getEntityTypeData();

            entityData.fireImmune = entityJson.get("fireImmune").getAsBoolean();
        }
    }

    private static void loadMaterialData(@NotNull File materialsFile) {
        if (!materialsFile.exists()) {
            LOGGER.error("Failed to find items.json.");
            LOGGER.error("Stopped raw data engine for items.");
            return;
        }
        JsonArray materialsJson;
        try {
            materialsJson = GSON.fromJson(new JsonReader(new FileReader(materialsFile)), JsonArray.class);
        } catch (FileNotFoundException e) {
            LOGGER.error("Failed to find items.json.");
            LOGGER.error("Stopped raw data engine for items.");
            return;
        }
        for (JsonElement jsonElement : materialsJson) {
            // Load data
            JsonObject materialJson = jsonElement.getAsJsonObject();
            NamespaceID materialId = NamespaceID.from(materialJson.get("id").getAsString());

            // Get correct item
            Material m = Registries.getMaterial(materialId);
            // CASE: Returning the defaulted value, however make sure to include the defaulted data itself.
            // to clarify: We return AIR if it doesn't exist, but we also need Air in the HashMap
            // Therefore we check the id for this functionality.
            if (m == Material.AIR && !materialId.equals(Material.AIR.getId())) {
                // This should honestly never happen as the values in the Registry are based on the json file.
                continue;
            }

            RawMaterialData materialData = m.getMaterialData();

            materialData.damageable = materialJson.get("depletes").getAsBoolean();
            materialData.maxDurability = materialJson.get("maxDamage").getAsInt();
            materialData.edible = materialJson.get("edible").getAsBoolean();
            materialData.fireResistant = materialJson.get("fireResistant").getAsBoolean();
            materialData.block = Registries.getBlock(materialJson.get("blockId").getAsString());
            materialData.eatingSound = Registries.getSoundEvent(materialJson.get("eatingSound").getAsString());
            materialData.drinkingSound = Registries.getSoundEvent(materialJson.get("drinkingSound").getAsString());

            // Is it an armor item
            if (materialJson.get("armorProperties") != null) {
                JsonObject armorJson = materialJson.get("armorProperties").getAsJsonObject();

                RawMaterialData.RawArmorData armorData = new RawMaterialData.RawArmorData();
                armorData.defense = armorJson.get("defense").getAsInt();
                armorData.toughness = armorJson.get("toughness").getAsDouble();
                // set correct slot
                String slot = armorJson.get("slot").getAsString().toUpperCase();
                switch (slot) {
                    case "HEAD": {
                        armorData.slot = EntityEquipmentPacket.Slot.HELMET;
                        break;
                    }
                    case "CHEST": {
                        armorData.slot = EntityEquipmentPacket.Slot.CHESTPLATE;
                        break;
                    }
                    case "LEGS": {
                        armorData.slot = EntityEquipmentPacket.Slot.LEGGINGS;
                        break;
                    }
                    case "FEET": {
                        armorData.slot = EntityEquipmentPacket.Slot.BOOTS;
                        break;
                    }
                }

                materialData.armorData = armorData;
            }
        }
    }

    private static void loadEnchantmentData(@NotNull File enchantmentsFile) {
        if (!enchantmentsFile.exists()) {
            LOGGER.error("Failed to find enchantments.json.");
            LOGGER.error("Stopped raw data engine for enchantments.");
            return;
        }
        JsonArray enchantmentsJson;
        try {
            enchantmentsJson = GSON.fromJson(new JsonReader(new FileReader(enchantmentsFile)), JsonArray.class);
        } catch (FileNotFoundException e) {
            LOGGER.error("Failed to find enchantments.json.");
            LOGGER.error("Stopped raw data engine for enchantments.");
            return;
        }
        for (JsonElement jsonElement : enchantmentsJson) {
            // Load data
            JsonObject enchantmentJson = jsonElement.getAsJsonObject();
            NamespaceID enchantmentId = NamespaceID.from(enchantmentJson.get("id").getAsString());

            // Get correct item
            Enchantment e = Registries.getEnchantment(enchantmentId);
            if (e == null) {
                // shouldn't happen
                continue;
            }
            RawEnchantmentData enchantmentData = e.getEnchantmentData();

            enchantmentData.maxLevel = enchantmentJson.get("maxLevel").getAsInt();
            enchantmentData.minLevel = enchantmentJson.get("minLevel").getAsInt();
            enchantmentData.rarity = enchantmentJson.get("rarity").getAsString();
            enchantmentData.curse = enchantmentJson.get("curse").getAsBoolean();
            enchantmentData.discoverable = enchantmentJson.get("discoverable").getAsBoolean();
            enchantmentData.tradeable = enchantmentJson.get("tradeable").getAsBoolean();
            enchantmentData.treasureExclusive = enchantmentJson.get("treasureOnly").getAsBoolean();
            enchantmentData.category = enchantmentJson.get("category").getAsString();
        }
    }

    private static void loadVillagerProfessionData(@NotNull File villagerProfessionsFile) {
        if (!villagerProfessionsFile.exists()) {
            LOGGER.error("Failed to find villager_professions.json.");
            LOGGER.error("Stopped raw data engine for villager professions.");
            return;
        }
        JsonArray villagerProfessionsJson;
        try {
            villagerProfessionsJson = GSON.fromJson(new JsonReader(new FileReader(villagerProfessionsFile)), JsonArray.class);
        } catch (FileNotFoundException e) {
            LOGGER.error("Failed to find enchantments.json.");
            LOGGER.error("Stopped raw data engine for enchantments.");
            return;
        }
        for (JsonElement jsonElement : villagerProfessionsJson) {
            // Load data
            JsonObject villagerProfessionJson = jsonElement.getAsJsonObject();
            NamespaceID villagerProfessionId = NamespaceID.from(villagerProfessionJson.get("id").getAsString());

            // Get correct item
            VillagerProfession vp = Registries.getVillagerProfession(villagerProfessionId);
            if (vp == null) {
                // shouldn't happen
                continue;
            }
            RawVillagerProfessionData vpData = vp.getVillagerProfessionData();

            JsonElement workSound = villagerProfessionJson.get("workSound");
            if (workSound == null) {
                vpData.workSound = null;
            } else {
                vpData.workSound = Registries.getSoundEvent(workSound.getAsString());
            }
        }
    }
}
