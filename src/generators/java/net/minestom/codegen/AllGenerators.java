package net.minestom.codegen;

import net.minestom.codegen.blocks.BlockEnumGenerator;
import net.minestom.codegen.enchantment.EnchantmentEnumGenerator;
import net.minestom.codegen.entitytypes.EntityTypeEnumGenerator;
import net.minestom.codegen.fluids.FluidEnumGenerator;
import net.minestom.codegen.items.ItemEnumGenerator;
import net.minestom.codegen.particles.ParticleEnumGenerator;
import net.minestom.codegen.potions.PotionEnumGenerator;
import net.minestom.codegen.sounds.SoundEnumGenerator;
import net.minestom.codegen.stats.BiomesEnumGenerator;
import net.minestom.codegen.stats.StatsEnumGenerator;

import java.io.IOException;

public class AllGenerators {

    public static void main(String[] args) throws IOException {
        BlockEnumGenerator.main(args);
        ItemEnumGenerator.main(args); // must be done after block
        PotionEnumGenerator.main(args);
        EnchantmentEnumGenerator.main(args);
        EntityTypeEnumGenerator.main(args);
        SoundEnumGenerator.main(args);
        ParticleEnumGenerator.main(args);
        StatsEnumGenerator.main(args);
        FluidEnumGenerator.main(args);
        RegistriesGenerator.main(args);
    }
}
