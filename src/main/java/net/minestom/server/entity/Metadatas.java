package net.minestom.server.entity;

import java.util.concurrent.atomic.AtomicInteger;

// TODO(autogenerate)
sealed interface Metadatas permits Metadata {
    AtomicInteger NEXT_ID = new AtomicInteger(0);

    byte TYPE_BYTE = nextId();
    byte TYPE_VARINT = nextId();
    byte TYPE_LONG = nextId();
    byte TYPE_FLOAT = nextId();
    byte TYPE_STRING = nextId();
    byte TYPE_CHAT = nextId();
    byte TYPE_OPT_CHAT = nextId();
    byte TYPE_ITEM_STACK = nextId();
    byte TYPE_BOOLEAN = nextId();
    byte TYPE_ROTATION = nextId();
    byte TYPE_BLOCK_POSITION = nextId();
    byte TYPE_OPT_BLOCK_POSITION = nextId();
    byte TYPE_DIRECTION = nextId();
    byte TYPE_OPT_UUID = nextId();
    byte TYPE_BLOCKSTATE = nextId();
    byte TYPE_OPT_BLOCKSTATE = nextId();
    byte TYPE_NBT = nextId();
    byte TYPE_PARTICLE = nextId();
    byte TYPE_PARTICLE_LIST = nextId();
    byte TYPE_VILLAGERDATA = nextId();
    byte TYPE_OPT_VARINT = nextId();
    byte TYPE_POSE = nextId();
    byte TYPE_CAT_VARIANT = nextId();
    byte TYPE_COW_VARIANT = nextId();
    byte TYPE_WOLF_VARIANT = nextId();
    byte TYPE_WOLF_SOUND_VARIANT = nextId();
    byte TYPE_FROG_VARIANT = nextId();
    byte TYPE_PIG_VARIANT = nextId();
    byte TYPE_CHICKEN_VARIANT = nextId();
    byte TYPE_OPT_GLOBAL_POSITION = nextId(); // Unused by protocol it seems
    byte TYPE_PAINTING_VARIANT = nextId();
    byte TYPE_SNIFFER_STATE = nextId();
    byte TYPE_ARMADILLO_STATE = nextId();
    byte TYPE_VECTOR3 = nextId();
    byte TYPE_QUATERNION = nextId();

    // Impl Note: Adding an entry here requires that a default value entry is added in MetadataImpl.EMPTY_VALUES

    private static byte nextId() {
        return (byte) NEXT_ID.getAndIncrement();
    }

}
