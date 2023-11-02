package net.minestom.server.entity.damage;

import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTType;

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

    @Override
    public NBTCompound asNBT() {
        var elem = new HashMap<String, NBT>();
        elem.put("exhaustion", NBT.Float(registry.exhaustion()));
        elem.put("message_id", NBT.String(registry.messageId()));
        elem.put("scaling", NBT.String(registry.scaling()));
        return NBT.Compound(elem);
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
            var damageTypes = values().stream()
                    .map((damageType) -> NBT.Compound(Map.of(
                            "id", NBT.Int(damageType.id()),
                            "name", NBT.String(damageType.name()),
                            "element", damageType.asNBT()
                    )))
                    .toList();

            lazyNbt = NBT.Compound(Map.of(
                    "type", NBT.String("minecraft:damage_type"),
                    "value", NBT.List(NBTType.TAG_Compound, damageTypes)
            ));
        }
        return lazyNbt;
    }
}