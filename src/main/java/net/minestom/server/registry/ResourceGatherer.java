package net.minestom.server.registry;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.villager.VillagerProfession;
import net.minestom.server.fluid.Fluid;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockState;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.Material;
import net.minestom.server.map.MapColors;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.raw_data.*;
import net.minestom.server.utils.NamespaceID;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Responsible for making sure Minestom has the necessary files to run (notably registry files)
 */
public class ResourceGatherer {
    public static final File DATA_FOLDER = new File("./minecraft_data/");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceGatherer.class);
    private static final File TMP_FOLDER = new File("./.minestom_tmp/");

    /**
     * Checks if registry/ folder is present
     * If it is not, download the minecraft server jar, run the data generator and extract the wanted files
     * If it is already present, directly return
     */
    public static void ensureResourcesArePresent(String version) throws IOException {
        if (DATA_FOLDER.exists()) {
            return;
        }
        LOGGER.info("{} folder does not exist. Minestom will now generate the necessary files.", DATA_FOLDER);

        if (!TMP_FOLDER.exists() && !TMP_FOLDER.mkdirs()) {
            throw new IOException("Failed to create tmp folder.");
        }

        LOGGER.info("Starting download of Minecraft server jar for version {} from Mojang servers...", version);
        File serverJar = downloadServerJar(version);
        LOGGER.info("Download complete.");

        runDataGenerator(serverJar);

        moveAndCleanup(version);
        LOGGER.info("Resource gathering done!");

        runDataInitializer(new File(DATA_FOLDER, "/json"), version);
    }

    private static void moveAndCleanup(String version) throws IOException {
        Path dataFolderPath = DATA_FOLDER.toPath();
        Path tmpFolderPath = TMP_FOLDER.toPath();
        Path generatedFolder = tmpFolderPath.resolve("generated");
        LOGGER.info("Data generator successful, removing server jar");
        Files.delete(tmpFolderPath.resolve("server_" + version + ".jar"));
        LOGGER.info("Removal successful, now moving data to {}", DATA_FOLDER);
        Files.walkFileTree(tmpFolderPath, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path relativePath = generatedFolder.relativize(dir);
                if (dir.startsWith(generatedFolder)) { // don't copy logs
                    Path resolvedPath = dataFolderPath.resolve(relativePath);
                    LOGGER.info("> Creating sub-folder {}", resolvedPath);
                    Files.createDirectories(resolvedPath);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                LOGGER.info("> Deleting folder {}", dir);
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relativePath = generatedFolder.relativize(file);
                if (file.startsWith(generatedFolder)) { // don't copy logs
                    Path resolvedPath = dataFolderPath.resolve(relativePath);
                    LOGGER.info("> Moving {}", relativePath);
                    Files.move(file, resolvedPath);
                } else {
                    LOGGER.info("> Deleting {}", relativePath);
                    Files.delete(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void runDataGenerator(File serverJar) throws IOException {
        // TODO: Use MinestomDataGenerator
        ProcessBuilder dataGenerator = new ProcessBuilder("java", "-cp", serverJar.getName(), "net.minecraft.data.Main", "--all", "--server", "--dev");
        dataGenerator.directory(TMP_FOLDER);
        LOGGER.info("Now running data generator with options '--dev', '--server', '--all'");
        LOGGER.info("Executing: {}", String.join(StringUtils.SPACE, dataGenerator.command()));
        LOGGER.info("Minestom will now wait for it to finish, here's its output:");
        LOGGER.info("");
        Process dataGeneratorProcess = dataGenerator.start();
        new BufferedReader(
                new InputStreamReader(dataGeneratorProcess.getInputStream())
        ).lines().forEach(LOGGER::info);
        LOGGER.info("");

        try {
            int resultCode = dataGeneratorProcess.waitFor();
            if (resultCode != 0) {
                throw new IOException("Data generator finished with non-zero return code " + resultCode + " verify that you have 'java' cli");
            }
        } catch (InterruptedException e) {
            throw new IOException("Data generator was interrupted.", e);
        }
    }

    private static File downloadServerJar(String version) throws IOException {
        // Mojang's version manifest is located at https://launchermeta.mojang.com/mc/game/version_manifest.json
        // If we query this (it's a json object), we can then search for the id we want.
        InputStream versionManifestStream = new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json").openStream();
        LOGGER.debug("Successfully queried Mojang's version_manifest.json.");

        JsonObject versionManifestJson = GSON.fromJson(new InputStreamReader(versionManifestStream), JsonObject.class);
        LOGGER.debug("Successfully read Mojang's version_manifest.json into a json object.");

        JsonArray versionArray = versionManifestJson.getAsJsonArray("versions");
        LOGGER.debug("Iterating over the version manifest to find a version with the id {}.", version);

        JsonObject versionEntry = null;
        for (JsonElement element : versionArray) {
            if (element.isJsonObject()) {
                JsonObject entry = element.getAsJsonObject();
                if (entry.get("id").getAsString().equals(version)) {
                    LOGGER.debug("Successfully found a version with the id {}.", version);
                    versionEntry = entry;
                    break;
                }
            }
        }
        if (versionEntry == null) {
            throw new IOException("Could not find " + version + " in Mojang's official list of minecraft versions.");
        }
        // We now have the entry we want and it gives us access to the json file containing the downloads.
        String versionUrl = versionEntry.get("url").getAsString();
        InputStream versionStream = new URL(versionUrl).openStream();
        LOGGER.debug("Successfully queried {}.json.", version);

        JsonObject versionJson = GSON.fromJson(new InputStreamReader(versionStream), JsonObject.class);
        LOGGER.debug("Successfully read {}.json into a json object.", version);

        // Now we need to navigate to "downloads.client.url" and "downloads.server.url" }
        JsonObject downloadsJson = versionJson.getAsJsonObject("downloads");

        // Designated spot if we ever need the client.

        // Server
        {
            JsonObject serverJson = downloadsJson.getAsJsonObject("server");
            final String jarURL = serverJson.get("url").getAsString();
            final String sha1 = serverJson.get("sha1").getAsString();

            LOGGER.debug("Found all information required to download the server JAR file.");
            LOGGER.debug("Attempting download.");
            return download(version, jarURL, sha1);
        }
    }

    private static File download(@NotNull String version, @NotNull String url, @NotNull String sha1Source) throws IOException {
        File target = new File(TMP_FOLDER, "server_" + version + ".jar");
        // Download
        try (FastBufferedInputStream in = new FastBufferedInputStream(new URL(url).openStream())) {
            Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Failed to download Minecraft server jar.", e);
        }
        // Verify checksum
        try (FileInputStream fis = new FileInputStream(target)) {
            String sha1Target = DigestUtils.sha1Hex(fis);
            if (!sha1Target.equals(sha1Source)) {
                LOGGER.debug("The checksum test failed after downloading the Minecraft server jar.");
                LOGGER.debug("The expected checksum was: {}.", sha1Source);
                LOGGER.debug("The calculated checksum was: {}.", sha1Target);
                throw new IOException("Failed to download Minecraft server jar.");
            }
        }
        return target;
    }


    private static void runDataInitializer(File inputFolder, String version) {
        loadBlockData(new File(inputFolder, version.replaceAll("\\.", "_") + "_blocks.json"));
        loadFluidData(new File(inputFolder, version.replaceAll("\\.", "_") + "_fluids.json"));
        loadEntityData(new File(inputFolder, version.replaceAll("\\.", "_") + "_entities.json"));
        loadMaterialData(new File(inputFolder, version.replaceAll("\\.", "_") + "_items.json"));
        loadEnchantmentData(new File(inputFolder, version.replaceAll("\\.", "_") + "_enchantments.json"));
        loadVillagerProfessionData(new File(inputFolder, version.replaceAll("\\.", "_") + "_villager_professions.json"));
    }

    private static void loadBlockData(@NotNull File blocksFile) {
        if (!blocksFile.exists()) {
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
            blockData.defaultBlockState = blockJson.get("defaultBlockState").getAsShort();
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
                // TODO: blockStateData.properties
                blockStateData.pushReaction = blockStateJson.get("pushReaction").getAsString();
                blockStateData.blocksMotion = blockStateJson.get("blocksMotion").getAsBoolean();
                blockStateData.flammable = blockStateJson.get("isFlammable").getAsBoolean();
                blockStateData.liquid = blockJson.get("isLiquid").getAsBoolean();
                blockStateData.replaceable = blockJson.get("isReplaceable").getAsBoolean();
                blockStateData.solid = blockJson.get("isSolid").getAsBoolean();
                blockStateData.solidBlocking = blockJson.get("isSolidBlocking").getAsBoolean();
                blockStateData.mapColor = MapColors.values()[blockJson.get("mapColorId").getAsInt()];
                blockStateData.boundingBox = blockJson.get("boundingBox").getAsString();
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

            String workSound = villagerProfessionJson.get("workSound").getAsString();
            if (workSound == null) {
                vpData.workSound = null;
            } else {
                vpData.workSound = Registries.getSoundEvent(workSound);
            }
        }
    }
}
