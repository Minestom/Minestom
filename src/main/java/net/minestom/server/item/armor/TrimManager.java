package net.minestom.server.item.armor;

import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class TrimManager {
    private final Set<TrimMaterial> trimMaterials;
    private final Set<TrimPattern> trimPatterns;
    private CompoundBinaryTag trimMaterialCache = null;
    private CompoundBinaryTag trimPatternCache = null;

    public TrimManager() {
        this.trimMaterials = new HashSet<>();
        this.trimPatterns = new HashSet<>();
    }

    public @Nullable TrimMaterial fromIngredient(Material ingredient) {
        return this.trimMaterials.stream().filter(trimMaterial -> trimMaterial.ingredient().equals(ingredient)).findFirst().orElse(null);
    }

    public @Nullable TrimPattern fromTemplate(Material material) {
        return this.trimPatterns.stream().filter(trimPattern -> trimPattern.template().equals(material)).findFirst().orElse(null);
    }


    public CompoundBinaryTag getTrimMaterialNBT() {
        if (trimMaterialCache == null) {
            ListBinaryTag.Builder<CompoundBinaryTag> entries = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);
            for (TrimMaterial trimMaterial : this.trimMaterials)
                entries.add(trimMaterial.asNBT());
            trimMaterialCache = CompoundBinaryTag.builder()
                    .putString("type", "minecraft:trim_material")
                    .put("value", entries.build())
                    .build();
        }
        return trimMaterialCache;
    }

    public CompoundBinaryTag getTrimPatternNBT() {
        if (trimPatternCache == null) {
            ListBinaryTag.Builder<CompoundBinaryTag> entries = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);
            for (TrimPattern trimPattern : this.trimPatterns)
                entries.add(trimPattern.asNBT());
            trimPatternCache = CompoundBinaryTag.builder()
                    .putString("type", "minecraft:trim_pattern")
                    .put("value", entries.build())
                    .build();
        }

        return trimPatternCache;
    }

    public Set<TrimMaterial> getTrimMaterials() {
        return Set.copyOf(this.trimMaterials);
    }

    public Set<TrimPattern> getTrimPatterns() {
        return Set.copyOf(trimPatterns);
    }

    public void addDefaultTrimMaterials() {
        this.trimMaterialCache = null;
        this.trimMaterials.addAll(TrimMaterial.values());
    }

    public void addDefaultTrimPatterns() {
        this.trimPatternCache = null;
        this.trimPatterns.addAll(TrimPattern.values());
    }

    public boolean addTrimMaterial(TrimMaterial trimMaterial) {
        this.trimMaterialCache = null;
        return this.trimMaterials.add(trimMaterial);
    }

    public boolean removeTrimMaterial(TrimMaterial trimMaterial) {
        this.trimMaterialCache = null;
        return this.trimMaterials.remove(trimMaterial);
    }

    public boolean addTrimPattern(TrimPattern trimPattern) {
        this.trimPatternCache = null;
        return this.trimPatterns.add(trimPattern);
    }

    public boolean removeTrimPattern(TrimPattern trimPattern) {
        this.trimPatternCache = null;
        return this.trimPatterns.remove(trimPattern);
    }
}
