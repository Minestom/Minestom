package net.minestom.server.item.armor;

import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TrimManager {
    private final Set<TrimMaterial> trimMaterials;
    private final Set<TrimPattern> trimPatterns;
    private NBTCompound trimMaterialCache = null;
    private NBTCompound trimPatternCache = null;

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


    public NBTCompound getTrimMaterialNBT() {
        if (trimMaterialCache == null) {
            var trimMaterials = this.trimMaterials.stream()
                    .map((trimMaterial) -> NBT.Compound(Map.of(
                            "id", NBT.Int(trimMaterial.id()),
                            "name", NBT.String(trimMaterial.name()),
                            "element", trimMaterial.asNBT()
                    )))
                    .toList();

            trimMaterialCache = NBT.Compound(Map.of(
                    "type", NBT.String("minecraft:trim_material"),
                    "value", NBT.List(NBTType.TAG_Compound, trimMaterials)
            ));
        }
        return trimMaterialCache;
    }

    public NBTCompound getTrimPatternNBT() {
        if (trimPatternCache == null) {
            var trimPatterns = this.trimPatterns.stream()
                    .map((trimPattern) -> NBT.Compound(Map.of(
                            "id", NBT.Int(trimPattern.id()),
                            "name", NBT.String(trimPattern.name()),
                            "element", trimPattern.asNBT()
                    )))
                    .toList();

            trimPatternCache = NBT.Compound(Map.of(
                    "type", NBT.String("minecraft:trim_pattern"),
                    "value", NBT.List(NBTType.TAG_Compound, trimPatterns)
            ));
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
        this.trimMaterialCache = null;
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
        this.trimMaterialCache = null;
        return this.trimPatterns.add(trimPattern);
    }

    public boolean removeTrimPattern(TrimPattern trimPattern) {
        this.trimMaterialCache = null;
        return this.trimPatterns.remove(trimPattern);
    }
}
