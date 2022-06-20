package net.minestom.server.message.registry;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.kyori.adventure.key.Key;
import net.minestom.server.ConfigurationManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.ObjectCache;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTType;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Used to register {@link ChatTypeBuilder chat types} and retrieve their protocol id.
 */
public final class ChatRegistryManager {
    private final AtomicInteger chatTypesId = new AtomicInteger();
    private final Int2ObjectMap<NBTCompound> idToCompound = new Int2ObjectOpenHashMap<>();
    private final Object2IntMap<Key> nameToId = new Object2IntOpenHashMap<>();
    private final ObjectCache<NBTCompound> nbtCompoundCache = new ObjectCache<>(() -> {
        final MutableNBTCompound root = new MutableNBTCompound();
        root.set("type", NBT.String("minecraft:chat_type"));
        root.set("value", NBT.List(NBTType.TAG_Compound, idToCompound.int2ObjectEntrySet().stream().map(x -> {
            final MutableNBTCompound compound = new MutableNBTCompound(x.getValue());
            compound.setInt("id", x.getIntKey());
            return compound.toCompound();
        }).collect(Collectors.toList())));
        return root.toCompound();
    });

    /**
     * Registers a new ChatType
     *
     * @param type type to register
     * @return the registered type id
     */
    @Contract("_ -> new")
    public ChatType addChatType(NBTCompound type) {
        final int id = chatTypesId.getAndIncrement();
        final @Subst("chat") String name = type.getString("name");
        if (name == null) {
            throw new IllegalArgumentException("ChatType compound doesn't contain required name tag!");
        }
        final ChatType chatType = ChatType.of(id, Key.key(name));
        idToCompound.put(id, type);
        if (nameToId.containsKey(chatType.key())) {
            MinecraftServer.LOGGER.warn("A ChatType has already been added with name: '{}'. This will overwrite name based lookups!", chatType.key());
        }
        nameToId.put(chatType.key(), id);
        nbtCompoundCache.invalidate();
        return chatType;
    }

    /**
     * @return the protocol id of the type, -1 if it isn't registered
     */
    public int idOf(ChatType type) {
        return idOf(type.key());
    }

    /**
     * @return the protocol id of the type with the provided name, -1 if it isn't registered
     */
    public int idOf(Key name) {
        return nameToId.getOrDefault(name, -1);
    }

    public boolean isRegistered(ChatType type) {
        return idOf(type) != -1;
    }

    /**
     * Registers default types
     */
    @ApiStatus.Internal
    public void initDefaults() {
        final ConfigurationManager conf = MinecraftServer.getConfigurationManager();
        ((MutableChatTypeImpl) ChatType.CHAT).setId(addChatType(conf.PLAYER_CHAT_TYPE.get()).id());
        ((MutableChatTypeImpl) ChatType.SYSTEM).setId(addChatType(conf.SYSTEM_CHAT_TYPE.get()).id());
        // TODO Should we add all default types?
    }

    public NBTCompound toNBT() {
        return nbtCompoundCache.get();
    }
}
