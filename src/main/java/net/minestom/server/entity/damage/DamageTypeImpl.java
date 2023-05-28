package net.minestom.server.entity.damage;

import net.minestom.server.MinecraftServer;
import net.minestom.server.message.Messenger;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

record DamageTypeImpl(Registry.DamageTypeEntry registry) implements DamageType {
    private static final Registry.Container<DamageType> CONTAINER = Registry.createContainer(Registry.Resource.DAMAGE_TYPES,
            (namespace, properties) -> new DamageTypeImpl(Registry.damageType(namespace, properties)));

    static DamageType get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static DamageType getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static DamageType getId(int id) {
        return CONTAINER.getId(id);
    }

    static Collection<DamageType> values() {
        return CONTAINER.values();
    }

    @Override
    public String toString() {
        return name();
    }

    @Override
    public int id() {
        return CONTAINER.toId(name());
    }

    private static NBTCompound lazyNbt = null;

    static NBTCompound getNBT() {
        if (lazyNbt == null) {
            var damageTypeData = Registry.load(Registry.Resource.DAMAGE_TYPES);
            var damageTypes = new ArrayList<NBT>();
            int i = 0;
            for (var entry : damageTypeData.entrySet()) {
                var elem = new HashMap<String, NBT>();
                for (var e : entry.getValue().entrySet()) {
                    if (e.getValue() instanceof String s) {
                        elem.put(e.getKey(), NBT.String(s));
                    } else if (e.getValue() instanceof Double f) {
                        elem.put(e.getKey(), NBT.Float(f.floatValue()));
                    } else if (e.getValue() instanceof Integer integer) {
                        elem.put(e.getKey(), NBT.Int(integer));
                    }
                }
                damageTypes.add(NBT.Compound(Map.of(
                        "id", NBT.Int(i++),
                        "name", NBT.String(entry.getKey()),
                        "element", NBT.Compound(elem)
                )));
            }
            NBTCompound damageTypeCompound = NBT.Compound(Map.of(
                    "type", NBT.String("minecraft:damage_type"),
                    "value", NBT.List(NBTType.TAG_Compound, damageTypes)
            ));

            lazyNbt = NBT.Compound(Map.of(
                    "minecraft:chat_type", Messenger.chatRegistry(),
                    "minecraft:dimension_type", MinecraftServer.getDimensionTypeManager().toNBT(),
                    "minecraft:worldgen/biome", MinecraftServer.getBiomeManager().toNBT(),
                    "minecraft:damage_type", damageTypeCompound));
        }
        return lazyNbt;
    }
}
