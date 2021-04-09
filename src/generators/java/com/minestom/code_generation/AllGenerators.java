package com.minestom.code_generation;

import com.minestom.code_generation.attribute.AttributeGenerator;
import com.minestom.code_generation.blocks.BlockGenerator;
import com.minestom.code_generation.entity.EntityGenerator;
import com.minestom.code_generation.entity.VillagerProfessionGenerator;
import com.minestom.code_generation.fluid.FluidGenerator;
import com.minestom.code_generation.item.EnchantmentGenerator;
import com.minestom.code_generation.item.MaterialGenerator;
import com.minestom.code_generation.map.MapColorsGenerator;
import com.minestom.code_generation.particle.ParticleGenerator;
import com.minestom.code_generation.registry.RegistryGenerator;
import com.minestom.code_generation.sound.SoundGenerator;
import net.kyori.adventure.sound.Sound;

import java.io.File;

import static com.minestom.code_generation.MinestomCodeGenerator.DEFAULT_OUTPUT_FOLDER;
import static com.minestom.code_generation.MinestomCodeGenerator.DEFAULT_SOURCE_FOLDER_ROOT;

public class AllGenerators {
    public static void main(String[] args) {
        String targetVersion;
        if (args.length < 1) {
            System.err.println("Usage: <MC version> [target folder]");
            return;
        }
        targetVersion = args[0];
        // TODO: Download data for specific version (Resourcegatherer)

        File outputFolder = null;
        if (args.length >= 2) {
            outputFolder = new File(args[1]);
        }
        // Generate registries
        new RegistryGenerator(
                outputFolder
        ).generate();
        // Generate blocks
        new BlockGenerator(
                new File(DEFAULT_SOURCE_FOLDER_ROOT + "/json", targetVersion.replaceAll("\\.", "_") + "_blocks.json"),
                outputFolder
        ).generate();
        // Generate fluids
        new FluidGenerator(
                new File(DEFAULT_SOURCE_FOLDER_ROOT + "/json", targetVersion.replaceAll("\\.", "_") + "_fluids.json"),
                outputFolder
        ).generate();
        // Generate items
        new MaterialGenerator(
                new File(DEFAULT_SOURCE_FOLDER_ROOT + "/json", targetVersion.replaceAll("\\.", "_") + "_items.json"),
                outputFolder
        ).generate();
        // Generate enchantments
        new EnchantmentGenerator(
                new File(DEFAULT_SOURCE_FOLDER_ROOT + "/json", targetVersion.replaceAll("\\.", "_") + "_enchantments.json"),
                outputFolder
        ).generate();
        // Generate particles
        new ParticleGenerator(
                new File(DEFAULT_SOURCE_FOLDER_ROOT + "/json", targetVersion.replaceAll("\\.", "_") + "_particles.json"),
                outputFolder
        ).generate();
        // Generate sounds
        new SoundGenerator(
                new File(DEFAULT_SOURCE_FOLDER_ROOT + "/json", targetVersion.replaceAll("\\.", "_") + "_sounds.json"),
                outputFolder
        ).generate();
        // Generate attributes
        new AttributeGenerator(
                new File(DEFAULT_SOURCE_FOLDER_ROOT + "/json", targetVersion.replaceAll("\\.", "_") + "_attributes.json"),
                outputFolder
        ).generate();
        // Generate entities
        new EntityGenerator(
                new File(DEFAULT_SOURCE_FOLDER_ROOT + "/json", targetVersion.replaceAll("\\.", "_") + "_entities.json"),
                outputFolder
        ).generate();
        // Generate villager professions
        new VillagerProfessionGenerator(
                new File(DEFAULT_SOURCE_FOLDER_ROOT + "/json", targetVersion.replaceAll("\\.", "_") + "_villager_professions.json"),
                outputFolder
        ).generate();

        // FINISHED
        // Generate map colours
        new MapColorsGenerator(
                new File(DEFAULT_SOURCE_FOLDER_ROOT + "/json", targetVersion.replaceAll("\\.", "_") + "_map_colors.json"),
                DEFAULT_OUTPUT_FOLDER
        ).generate();
    }
}
