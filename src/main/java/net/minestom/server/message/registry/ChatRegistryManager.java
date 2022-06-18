package net.minestom.server.message.registry;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.ObjectCache;
import org.jetbrains.annotations.ApiStatus;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTType;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Used to register {@link ChatType chat types} and retrieve their protocol id.
 */
public final class ChatRegistryManager {
    // TODO Should this be in an enum e.g. DefaultChatType?
    public static final Key DEFAULT_CHAT = Key.key("minecraft:chat");

    private final AtomicInteger chatTypesId = new AtomicInteger();
    private final Int2ObjectMap<ChatType> idToType = new Int2ObjectOpenHashMap<>();
    private final Object2IntMap<ChatType> typeToId = new Object2IntOpenHashMap<>();
    private final Object2IntMap<Key> nameToId = new Object2IntOpenHashMap<>();
    private final ObjectCache<NBTCompound> nbtCompoundCache = new ObjectCache<>(() -> {
        final MutableNBTCompound root = new MutableNBTCompound();
        root.set("type", NBT.String("minecraft:chat_type"));
        root.set("value", NBT.List(NBTType.TAG_Compound, idToType.int2ObjectEntrySet().stream().map(x -> {
            final MutableNBTCompound compound = new MutableNBTCompound();
            compound.setInt("id", x.getIntKey());
            x.getValue().write(compound);
            return compound.toCompound();
        }).collect(Collectors.toList())));
        return root.toCompound();
    });

    /**
     * Registers a new ChatType
     *
     * @param type type to register
     * @return the protocol id of this type
     */
    public int addChatType(ChatType type) {
        final int id = chatTypesId.getAndIncrement();
        idToType.put(id, type);
        typeToId.put(type, id);
        if (nameToId.containsKey(type.name())) {
            MinecraftServer.LOGGER.warn("A ChatType has already been added with name: '{}'. This will overwrite name based lookups!", type.name());
        }
        nameToId.put(type.name(), id);
        nbtCompoundCache.invalidate();
        return id;
    }

    /**
     * @return the protocol id of the type, -1 if it isn't registered
     */
    public int getIdOf(ChatType type) {
        return typeToId.getOrDefault(type, -1);
    }

    /**
     * @return the protocol id of the type with the provided name, -1 if it isn't registered
     */
    public int getIdOf(Key name) {
        return nameToId.getOrDefault(name, -1);
    }

    /**
     * Registers default types if the user didn't provide them before calling {@link MinecraftServer#start}
     */
    @ApiStatus.Internal
    public void initDefaults() {
        // Player chat
        if (getIdOf(DEFAULT_CHAT) == -1) {
            addChatType(ChatType.chat(DEFAULT_CHAT, ChatDecoration.contentWithSender("chat.type.text").toTextDisplay()));
            logDefaultRegistering(DEFAULT_CHAT);
        }
        // TODO Should we add all default types?
    }

    private void logDefaultRegistering(Key name) {
        MinecraftServer.LOGGER.debug("Registering default ChatType for: {}", name);
    }

    public NBTCompound toNBT() {
        return nbtCompoundCache.get();
    }
}
