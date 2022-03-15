package net.minestom.server.instance.light.starlight;

import java.util.ArrayDeque;
import java.util.Arrays;

// SWMR -> Single Writer Multi Reader Nibble Array
public final class SWMRNibbleArray {

    /*
     * Null nibble - nibble does not exist, and should not be written to. Just like vanilla - null
     * nibbles are always 0 - and they are never written to directly. Only initialised/uninitialised
     * nibbles can be written to.
     *
     * Uninitialised nibble - They are all 0, but the backing array isn't initialised.
     *
     * Initialised nibble - Has light data.
     */

    protected static final int INIT_STATE_NULL   = 0; // null
    protected static final int INIT_STATE_UNINIT = 1; // uninitialised
    protected static final int INIT_STATE_INIT   = 2; // initialised
    protected static final int INIT_STATE_HIDDEN = 3; // initialised, but conversion to Vanilla data should be treated as if NULL

    public static final int ARRAY_SIZE = 16 * 16 * 16 / (8/4); // blocks / bytes per block
    // this allows us to maintain only 1 byte array when we're not updating
    static final ThreadLocal<ArrayDeque<byte[]>> WORKING_BYTES_POOL = ThreadLocal.withInitial(ArrayDeque::new);

    private static byte[] allocateBytes() {
        final byte[] inPool = WORKING_BYTES_POOL.get().pollFirst();
        if (inPool != null) {
            return inPool;
        }

        return new byte[ARRAY_SIZE];
    }

    private static void freeBytes(final byte[] bytes) {
        WORKING_BYTES_POOL.get().addFirst(bytes);
    }

    public static SWMRNibbleArray fromVanilla(byte[] data) {
        if (data == null) {
            return new SWMRNibbleArray(null, true);
        } else if (data.length == 0) {
            return new SWMRNibbleArray();
        } else {
            return new SWMRNibbleArray(data.clone()); // make sure we don't write to the parameter later
        }
    }

    protected int stateUpdating;
    protected volatile int stateVisible;

    protected byte[] storageUpdating;
    protected boolean updatingDirty; // only returns whether storageUpdating is dirty
    protected volatile byte[] storageVisible;

    public SWMRNibbleArray() {
        this(null, false); // lazy init
    }

    public SWMRNibbleArray(final byte[] bytes) {
        this(bytes, false);
    }

    public SWMRNibbleArray(final byte[] bytes, final boolean isNullNibble) {
        if (bytes != null && bytes.length != ARRAY_SIZE) {
            throw new IllegalArgumentException("Data of wrong length: " + bytes.length);
        }
        this.stateVisible = this.stateUpdating = bytes == null ? (isNullNibble ? INIT_STATE_NULL : INIT_STATE_UNINIT) : INIT_STATE_INIT;
        this.storageUpdating = this.storageVisible = bytes;
    }

    public SWMRNibbleArray(final byte[] bytes, final int state) {
        if (bytes != null && bytes.length != ARRAY_SIZE) {
            throw new IllegalArgumentException("Data of wrong length: " + bytes.length);
        }
        if (bytes == null && (state == INIT_STATE_INIT || state == INIT_STATE_HIDDEN)) {
            throw new IllegalArgumentException("Data cannot be null and have state be initialised");
        }
        this.stateUpdating = this.stateVisible = state;
        this.storageUpdating = this.storageVisible = bytes;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("State: ");
        switch (this.stateVisible) {
            case INIT_STATE_NULL:
                stringBuilder.append("null");
                break;
            case INIT_STATE_UNINIT:
                stringBuilder.append("uninitialised");
                break;
            case INIT_STATE_INIT:
                stringBuilder.append("initialised");
                break;
            case INIT_STATE_HIDDEN:
                stringBuilder.append("hidden");
                break;
            default:
                stringBuilder.append("unknown");
                break;
        }
        stringBuilder.append("\nData:\n");

        final byte[] data = this.storageVisible;
        if (data != null) {
            for (int i = 0; i < 4096; ++i) {
                // Copied from NibbleArray#toString
                final int level = ((data[i >>> 1] >>> ((i & 1) << 2)) & 0xF);

                stringBuilder.append(Integer.toHexString(level));
                if ((i & 15) == 15) {
                    stringBuilder.append("\n");
                }

                if ((i & 255) == 255) {
                    stringBuilder.append("\n");
                }
            }
        } else {
            stringBuilder.append("null");
        }

        return stringBuilder.toString();
    }

    public SaveState getSaveState() {
        synchronized (this) {
            final int state = this.stateVisible;
            final byte[] data = this.storageVisible;
            if (state == INIT_STATE_NULL) {
                return null;
            }
            if (state == INIT_STATE_UNINIT) {
                return new SaveState(null, state);
            }
            final boolean zero = isAllZero(data);
            if (zero) {
                return state == INIT_STATE_INIT ? new SaveState(null, INIT_STATE_UNINIT) : null;
            } else {
                return new SaveState(data.clone(), state);
            }
        }
    }

    protected static boolean isAllZero(final byte[] data) {
        for (int i = 0; i < (ARRAY_SIZE >>> 4); ++i) {
            byte whole = data[i << 4];

            for (int k = 1; k < (1 << 4); ++k) {
                whole |= data[(i << 4) | k];
            }

            if (whole != 0) {
                return false;
            }
        }

        return true;
    }

    // operation type: updating on src, updating on other
    public void extrudeLower(final SWMRNibbleArray other) {
        if (other.stateUpdating == INIT_STATE_NULL) {
            throw new IllegalArgumentException();
        }

        if (other.storageUpdating == null) {
            this.setUninitialised();
            return;
        }

        final byte[] src = other.storageUpdating;
        final byte[] into;

        if (this.storageUpdating != null) {
            into = this.storageUpdating;
        } else {
            this.storageUpdating = into = allocateBytes();
            this.stateUpdating = INIT_STATE_INIT;
        }
        this.updatingDirty = true;

        final int start = 0;
        final int end = (15 | (15 << 4)) >>> 1;

        /* x | (z << 4) | (y << 8) */
        for (int y = 0; y <= 15; ++y) {
            System.arraycopy(src, start, into, y << (8 - 1), end - start + 1);
        }
    }

    // operation type: updating
    public void setFull() {
        if (this.stateUpdating != INIT_STATE_HIDDEN) {
            this.stateUpdating = INIT_STATE_INIT;
        }
        Arrays.fill(this.storageUpdating == null || !this.updatingDirty ? this.storageUpdating = allocateBytes() : this.storageUpdating, (byte)-1);
        this.updatingDirty = true;
    }

    // operation type: updating
    public void setZero() {
        if (this.stateUpdating != INIT_STATE_HIDDEN) {
            this.stateUpdating = INIT_STATE_INIT;
        }
        Arrays.fill(this.storageUpdating == null || !this.updatingDirty ? this.storageUpdating = allocateBytes() : this.storageUpdating, (byte)0);
        this.updatingDirty = true;
    }

    // operation type: updating
    public void setNonNull() {
        if (this.stateUpdating == INIT_STATE_HIDDEN) {
            this.stateUpdating = INIT_STATE_INIT;
            return;
        }
        if (this.stateUpdating != INIT_STATE_NULL) {
            return;
        }
        this.stateUpdating = INIT_STATE_UNINIT;
    }

    // operation type: updating
    public void setNull() {
        this.stateUpdating = INIT_STATE_NULL;
        if (this.updatingDirty && this.storageUpdating != null) {
            freeBytes(this.storageUpdating);
        }
        this.storageUpdating = null;
        this.updatingDirty = false;
    }

    // operation type: updating
    public void setUninitialised() {
        this.stateUpdating = INIT_STATE_UNINIT;
        if (this.storageUpdating != null && this.updatingDirty) {
            freeBytes(this.storageUpdating);
        }
        this.storageUpdating = null;
        this.updatingDirty = false;
    }

    // operation type: updating
    public void setHidden() {
        if (this.stateUpdating == INIT_STATE_HIDDEN) {
            return;
        }
        if (this.stateUpdating != INIT_STATE_INIT) {
            this.setNull();
        } else {
            this.stateUpdating = INIT_STATE_HIDDEN;
        }
    }

    // operation type: updating
    public boolean isDirty() {
        return this.stateUpdating != this.stateVisible || this.updatingDirty;
    }

    // operation type: updating
    public boolean isNullNibbleUpdating() {
        return this.stateUpdating == INIT_STATE_NULL;
    }

    // operation type: visible
    public boolean isNullNibbleVisible() {
        return this.stateVisible == INIT_STATE_NULL;
    }

    // opeartion type: updating
    public boolean isUninitialisedUpdating() {
        return this.stateUpdating == INIT_STATE_UNINIT;
    }

    // operation type: visible
    public boolean isUninitialisedVisible() {
        return this.stateVisible == INIT_STATE_UNINIT;
    }

    // operation type: updating
    public boolean isInitialisedUpdating() {
        return this.stateUpdating == INIT_STATE_INIT;
    }

    // operation type: visible
    public boolean isInitialisedVisible() {
        return this.stateVisible == INIT_STATE_INIT;
    }

    // operation type: updating
    public boolean isHiddenUpdating() {
        return this.stateUpdating == INIT_STATE_HIDDEN;
    }

    // operation type: updating
    public boolean isHiddenVisible() {
        return this.stateVisible == INIT_STATE_HIDDEN;
    }

    // operation type: updating
    protected void swapUpdatingAndMarkDirty() {
        if (this.updatingDirty) {
            return;
        }

        if (this.storageUpdating == null) {
            this.storageUpdating = allocateBytes();
            Arrays.fill(this.storageUpdating, (byte)0);
        } else {
            System.arraycopy(this.storageUpdating, 0, this.storageUpdating = allocateBytes(), 0, ARRAY_SIZE);
        }

        if (this.stateUpdating != INIT_STATE_HIDDEN) {
            this.stateUpdating = INIT_STATE_INIT;
        }
        this.updatingDirty = true;
    }

    // operation type: updating
    public boolean updateVisible() {
        if (!this.isDirty()) {
            return false;
        }

        synchronized (this) {
            if (this.stateUpdating == INIT_STATE_NULL || this.stateUpdating == INIT_STATE_UNINIT) {
                this.storageVisible = null;
            } else {
                if (this.storageVisible == null) {
                    this.storageVisible = this.storageUpdating.clone();
                } else {
                    if (this.storageUpdating != this.storageVisible) {
                        System.arraycopy(this.storageUpdating, 0, this.storageVisible, 0, ARRAY_SIZE);
                    }
                }

                if (this.storageUpdating != this.storageVisible) {
                    freeBytes(this.storageUpdating);
                }
                this.storageUpdating = this.storageVisible;
            }
            this.updatingDirty = false;
            this.stateVisible = this.stateUpdating;
        }

        return true;
    }

    // operation type: visible
    public byte[] toVanillaNibble() {
        synchronized (this) {
            switch (this.stateVisible) {
                case INIT_STATE_HIDDEN:
                case INIT_STATE_NULL:
                    return null;
                case INIT_STATE_UNINIT:
                    return new byte[0];
                case INIT_STATE_INIT:
//                    return this.storageVisible.clone();
                    return this.storageVisible;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    /* x | (z << 4) | (y << 8) */

    // operation type: updating
    public int getUpdating(final int x, final int y, final int z) {
        return this.getUpdating((x & 15) | ((z & 15) << 4) | ((y & 15) << 8));
    }

    // operation type: updating
    public int getUpdating(final int index) {
        // indices range from 0 -> 4096
        final byte[] bytes = this.storageUpdating;
        if (bytes == null) {
            return 0;
        }
        final byte value = bytes[index >>> 1];

        // if we are an even index, we want lower 4 bits
        // if we are an odd index, we want upper 4 bits
        return ((value >>> ((index & 1) << 2)) & 0xF);
    }

    // operation type: visible
    public int getVisible(final int x, final int y, final int z) {
        return this.getVisible((x & 15) | ((z & 15) << 4) | ((y & 15) << 8));
    }

    // operation type: visible
    public int getVisible(final int index) {
        // indices range from 0 -> 4096
        final byte[] visibleBytes = this.storageVisible;
        if (visibleBytes == null) {
            return 0;
        }
        final byte value = visibleBytes[index >>> 1];

        // if we are an even index, we want lower 4 bits
        // if we are an odd index, we want upper 4 bits
        return ((value >>> ((index & 1) << 2)) & 0xF);
    }

    // operation type: updating
    public void set(final int x, final int y, final int z, final int value) {
        this.set((x & 15) | ((z & 15) << 4) | ((y & 15) << 8), value);
    }

    // operation type: updating
    public void set(final int index, final int value) {
        if (!this.updatingDirty) {
            this.swapUpdatingAndMarkDirty();
        }
        final int shift = (index & 1) << 2;
        final int i = index >>> 1;

        this.storageUpdating[i] = (byte)((this.storageUpdating[i] & (0xF0 >>> shift)) | (value << shift));
    }

    public static final class SaveState {

        public final byte[] data;
        public final int state;

        public SaveState(final byte[] data, final int state) {
            this.data = data;
            this.state = state;
        }
    }
}
