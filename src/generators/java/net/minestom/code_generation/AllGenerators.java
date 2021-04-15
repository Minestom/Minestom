package net.minestom.code_generation;

import net.minestom.code_generation.attribute.AttributeGenerator;
import net.minestom.code_generation.blocks.BlockGenerator;
import net.minestom.code_generation.entity.EntityTypeGenerator;
import net.minestom.code_generation.entity.VillagerProfessionGenerator;
import net.minestom.code_generation.entity.VillagerTypeGenerator;
import net.minestom.code_generation.fluid.FluidGenerator;
import net.minestom.code_generation.item.EnchantmentGenerator;
import net.minestom.code_generation.item.MaterialGenerator;
import net.minestom.code_generation.map.MapColorsGenerator;
import net.minestom.code_generation.particle.ParticleGenerator;
import net.minestom.code_generation.potion.PotionEffectGenerator;
import net.minestom.code_generation.potion.PotionTypeGenerator;
import net.minestom.code_generation.registry.RegistryGenerator;
import net.minestom.code_generation.sound.SoundGenerator;
import net.minestom.code_generation.statistics.StatisticGenerator;

import java.io.File;

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

        // FINISHED
        // Generate registries
        new RegistryGenerator(
                outputFolder
        ).generate();
        // Generate blocks
        new BlockGenerator(
                new File(MinestomCodeGenerator.DEFAULT_SOURCE_FOLDER_ROOT + "/json", targetVersion.replaceAll("\\.", "_") + "_blocks.json"),
                outputFolder
        ).generate();
        // Generate fluids
        new FluidGenerator(
                new File(MinestomCodeGenerator.DEFAULT_SOURCE_FOLDER_ROOT + "/json", targetVersion.replaceAll("\\.", "_") + "_fluids.json"),
                outputFolder
        ).generate();
        // Generate block entities

        // Generate entities
        new EntityTypeGenerator(
                new File(MinestomCodeGenerator.DEFAULT_SOURCE_FOLDER_ROOT + "/json", targetVersion.replaceAll("\\.", "_") + "_entities.json"),
                outputFolder
        ).generate();
        // Generate items
        new MaterialGenerator(
                new File(MinestomCodeGenerator.DEFAULT_SOURCE_FOLDER_ROOT + "/json", targetVersion.replaceAll("\\.", "_") + "_items.json"),
                outputFolder
        ).generate();
        // Generate enchantments
        new EnchantmentGenerator(
                new File(MinestomCodeGenerator.DEFAULT_SOURCE_FOLDER_ROOT + "/json", targetVersion.replaceAll("\\.", "_") + "_enchantments.json"),
                outputFolder
        ).generate();
        // Generate attributes
        new AttributeGenerator(
                new File(MinestomCodeGenerator.DEFAULT_SOURCE_FOLDER_ROOT + "/json", targetVersion.replaceAll("\\.", "_") + "_attributes.json"),
                outputFolder
        ).generate();
        // Generate potion effects
        new PotionEffectGenerator(
                new File(MinestomCodeGenerator.DEFAULT_SOURCE_FOLDER_ROOT + "/json", targetVersion.replaceAll("\\.", "_") + "_potion_effects.json"),
                outputFolder
        ).generate();
        // Generate potions
        new PotionTypeGenerator(
                new File(MinestomCodeGenerator.DEFAULT_SOURCE_FOLDER_ROOT + "/json", targetVersion.replaceAll("\\.", "_") + "_potions.json"),
                outputFolder
        ).generate();
        // Generate particles
        new ParticleGenerator(
                new File(MinestomCodeGenerator.DEFAULT_SOURCE_FOLDER_ROOT + "/json", targetVersion.replaceAll("\\.", "_") + "_particles.json"),
                outputFolder
        ).generate();
        // Generate sounds
        new SoundGenerator(
                new File(MinestomCodeGenerator.DEFAULT_SOURCE_FOLDER_ROOT + "/json", targetVersion.replaceAll("\\.", "_") + "_sounds.json"),
                outputFolder
        ).generate();
        // Generate villager professions
        new VillagerProfessionGenerator(
                new File(MinestomCodeGenerator.DEFAULT_SOURCE_FOLDER_ROOT + "/json", targetVersion.replaceAll("\\.", "_") + "_villager_professions.json"),
                outputFolder
        ).generate();
        // Generate villager types
        new VillagerTypeGenerator(
                new File(MinestomCodeGenerator.DEFAULT_SOURCE_FOLDER_ROOT + "/json", targetVersion.replaceAll("\\.", "_") + "_villager_types.json"),
                outputFolder
        ).generate();
        // Generate statistics
        new StatisticGenerator(
                new File(MinestomCodeGenerator.DEFAULT_SOURCE_FOLDER_ROOT + "/json", targetVersion.replaceAll("\\.", "_") + "_custom_statistics.json"),
                outputFolder
        ).generate();
        // Generate map colours
        new MapColorsGenerator(
                new File(MinestomCodeGenerator.DEFAULT_SOURCE_FOLDER_ROOT + "/json", targetVersion.replaceAll("\\.", "_") + "_map_colors.json"),
                outputFolder
        ).generate();
    }
}
