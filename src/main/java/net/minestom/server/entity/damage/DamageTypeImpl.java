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
import java.util.concurrent.atomic.AtomicInteger;

record DamageTypeImpl(Registry.DamageTypeEntry registry, int id) implements DamageType {
    private static final Registry.Container<DamageType> CONTAINER;

    static {
        AtomicInteger i = new AtomicInteger();
        CONTAINER = Registry.createContainer(Registry.Resource.DAMAGE_TYPES,
                (namespace, properties) -> new DamageTypeImpl(Registry.damageType(namespace, properties), i.getAndIncrement()));
    }

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
        return id;
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
            lazyNbt = NBT.Compound(Map.of(
                    "type", NBT.String("minecraft:damage_type"),
                    "value", NBT.List(NBTType.TAG_Compound, damageTypes)
            ));
        }
        return lazyNbt;
    }
}
