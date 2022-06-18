package net.minestom.server.message.registry;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTType;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class ChatRegistryManager {
    private final AtomicInteger chatTypesId = new AtomicInteger();
    private final Int2ObjectMap<ChatType> idToType = new Int2ObjectOpenHashMap<>();
    private final Object2IntMap<ChatType> typeToId = new Object2IntOpenHashMap<>();

    public int addChatType(ChatType type) {
        final int id = chatTypesId.getAndIncrement();
        idToType.put(id, type);
        typeToId.put(type, id);
        return id;
    }

    public int getIdOf(ChatType type) {
        return typeToId.getInt(type);
    }

    public NBTCompound toNBT() {
        final MutableNBTCompound root = new MutableNBTCompound();
        root.set("type", NBT.String("minecraft:chat_type"));
        root.set("value", NBT.List(NBTType.TAG_Compound, idToType.int2ObjectEntrySet().stream().map(x -> {
            final MutableNBTCompound compound = new MutableNBTCompound();
            compound.setInt("id", x.getIntKey());
            x.getValue().write(compound);
            return compound.toCompound();
        }).collect(Collectors.toList())));
        return root.toCompound();
    }
}
