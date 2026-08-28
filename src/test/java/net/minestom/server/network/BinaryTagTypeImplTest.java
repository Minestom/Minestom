package net.minestom.server.network;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagType;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.ByteArrayBinaryTag;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.EndBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.LongArrayBinaryTag;
import net.kyori.adventure.nbt.LongBinaryTag;
import net.kyori.adventure.nbt.ShortBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import static net.minestom.server.ServerFlag.NBT_MAX_DEPTH;
import static net.minestom.server.network.BinaryTagTypeImpl.TAG_BYTE_ARRAY;
import static net.minestom.server.network.BinaryTagTypeImpl.TAG_COMPOUND;
import static net.minestom.server.network.BinaryTagTypeImpl.TAG_END;
import static net.minestom.server.network.BinaryTagTypeImpl.TAG_INT_ARRAY;
import static net.minestom.server.network.BinaryTagTypeImpl.TAG_LIST;
import static net.minestom.server.network.BinaryTagTypeImpl.TAG_LONG_ARRAY;
import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.INT;
import static net.minestom.server.network.NetworkBuffer.NBT;
import static net.minestom.server.network.NetworkBuffer.NBT_COMPOUND;
import static net.minestom.server.network.NetworkBuffer.STRING_IO_UTF8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BinaryTagTypeImplTest {
    private static final byte TAG_UNKNOWN = 42;

    static {
        BinaryTagTypes.COMPOUND.id(); // Populates the adventure type registry read from below
    }

    @Test
    public void scalars() {
        assertCompatible(EndBinaryTag.endBinaryTag());
        assertCompatible(ByteBinaryTag.byteBinaryTag((byte) -12));
        assertCompatible(ShortBinaryTag.shortBinaryTag((short) 4321));
        assertCompatible(IntBinaryTag.intBinaryTag(Integer.MIN_VALUE));
        assertCompatible(LongBinaryTag.longBinaryTag(Long.MAX_VALUE));
        assertCompatible(FloatBinaryTag.floatBinaryTag(3.5f));
        assertCompatible(DoubleBinaryTag.doubleBinaryTag(-0.25));
        assertCompatible(StringBinaryTag.stringBinaryTag(""));
        assertCompatible(StringBinaryTag.stringBinaryTag("hello \u00FCn\u00EFcod\u00E9 \u26CF \uD83D\uDE00 \0"));
        assertCompatible(ByteArrayBinaryTag.byteArrayBinaryTag());
        assertCompatible(ByteArrayBinaryTag.byteArrayBinaryTag((byte) 1, (byte) 2, (byte) 3));
        assertCompatible(IntArrayBinaryTag.intArrayBinaryTag());
        assertCompatible(IntArrayBinaryTag.intArrayBinaryTag(1, -2, 3));
        assertCompatible(LongArrayBinaryTag.longArrayBinaryTag());
        assertCompatible(LongArrayBinaryTag.longArrayBinaryTag(1L, -2L));
    }

    @Test
    public void containers() {
        assertCompatible(CompoundBinaryTag.empty());
        assertCompatible(ListBinaryTag.empty());
        assertCompatible(CompoundBinaryTag.builder()
                .putString("name", "test")
                .putByte("b", (byte) 1)
                .put("nested", CompoundBinaryTag.builder().putInt("x", 5).build())
                .put("list", ListBinaryTag.listBinaryTag(BinaryTagTypes.STRING,
                        List.of(StringBinaryTag.stringBinaryTag("a"), StringBinaryTag.stringBinaryTag("b"))))
                .put("emptyCompound", CompoundBinaryTag.empty())
                .put("emptyList", ListBinaryTag.empty())
                .build());
        // List of lists of compounds
        assertCompatible(ListBinaryTag.listBinaryTag(BinaryTagTypes.LIST, List.of(
                ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, List.of(
                        CompoundBinaryTag.builder().putInt("a", 1).build(),
                        CompoundBinaryTag.empty())),
                ListBinaryTag.empty())));
    }

    @Test
    public void compoundRoot() {
        final CompoundBinaryTag tag = CompoundBinaryTag.builder().putInt("a", 1).build();
        final byte[] data = NetworkBuffer.makeArray(buffer -> buffer.write(NBT_COMPOUND, tag));
        assertArrayEquals(adventureBytes(tag), data);
        assertEquals(tag, NetworkBuffer.wrap(data, 0, data.length).read(NBT_COMPOUND));

        // A non compound root is rejected on its type id, before its payload is read
        final byte[] scalar = bytes(IntBinaryTag.intBinaryTag(1));
        assertThrows(IllegalArgumentException.class,
                () -> NetworkBuffer.wrap(scalar, 0, scalar.length).read(NBT_COMPOUND));
    }

    @Test
    public void heterogeneousList() {
        final ListBinaryTag list = ListBinaryTag.heterogeneousListBinaryTag()
                .add(IntBinaryTag.intBinaryTag(1))
                .add(StringBinaryTag.stringBinaryTag("two"))
                .build();
        final byte[] data = bytes(list);
        assertArrayEquals(adventureBytes(list), data);
        assertEquals(adventureRead(data), read(data));
    }

    @Test
    public void randomTrees() {
        final Random random = new Random(1234);
        for (int i = 0; i < 200; i++) assertCompatible(randomTag(random, 4));
    }

    @Test
    public void deepNesting() {
        // NBT_MAX_DEPTH containers is the deepest which round trips, one more must fail either way
        final BinaryTag deepest = nestedCompounds(NBT_MAX_DEPTH);
        assertEquals(deepest, read(bytes(deepest)));

        final BinaryTag tooDeep = nestedCompounds(NBT_MAX_DEPTH + 1);
        assertThrows(IllegalArgumentException.class, () -> bytes(tooDeep));

        final byte[] data = nestedCompoundBytes(NBT_MAX_DEPTH + 1);
        assertThrows(IllegalArgumentException.class, () -> read(data));
    }

    @Test
    public void oversizedLengths() {
        // Announced sizes which cannot possibly fit in the buffer must be rejected without allocating
        for (byte type : new byte[]{TAG_BYTE_ARRAY, TAG_INT_ARRAY, TAG_LONG_ARRAY}) {
            assertRejected(IndexOutOfBoundsException.class, buffer -> {
                buffer.write(BYTE, type);
                buffer.write(INT, Integer.MAX_VALUE);
            });
            assertRejected(IllegalArgumentException.class, buffer -> {
                buffer.write(BYTE, type);
                buffer.write(INT, -1);
            });
        }
        assertRejected(IndexOutOfBoundsException.class, buffer -> {
            buffer.write(BYTE, TAG_LIST);
            buffer.write(BYTE, TAG_COMPOUND); // One byte minimum per entry
            buffer.write(INT, Integer.MAX_VALUE);
        });
        assertRejected(IllegalArgumentException.class, buffer -> {
            buffer.write(BYTE, TAG_LIST);
            buffer.write(BYTE, TAG_COMPOUND);
            buffer.write(INT, -1);
        });
    }

    @Test
    public void malformedInput() {
        assertRejected(IllegalArgumentException.class, buffer -> { // Non empty list of TAG_End
            buffer.write(BYTE, TAG_LIST);
            buffer.write(BYTE, TAG_END);
            buffer.write(INT, 1);
        });
        assertRejected(IllegalArgumentException.class, buffer -> buffer.write(BYTE, TAG_UNKNOWN));
    }

    @Test
    public void truncatedInput() {
        final byte[] data = bytes(CompoundBinaryTag.builder().putLong("a", 5).build());
        for (int i = 1; i < data.length; i++) {
            final byte[] truncated = Arrays.copyOf(data, i);
            assertThrows(RuntimeException.class, () -> read(truncated), "expected failure at length " + i);
        }
    }

    private static byte[] bytes(BinaryTag tag) {
        return NetworkBuffer.makeArray(buffer -> buffer.write(NBT, tag));
    }

    private static BinaryTag read(byte[] data) {
        return NetworkBuffer.wrap(data, 0, data.length).read(NBT);
    }

    @SuppressWarnings("unchecked")
    private static byte[] adventureBytes(BinaryTag tag) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (DataOutputStream data = new DataOutputStream(out)) {
            data.writeByte(tag.type().id());
            ((BinaryTagType<BinaryTag>) tag.type()).write(tag, data);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toByteArray();
    }

    private static BinaryTag adventureRead(byte[] data) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
            final byte id = in.readByte();
            if (id == TAG_END) return EndBinaryTag.endBinaryTag();
            return BinaryTagType.binaryTagType(id).read(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void assertCompatible(BinaryTag tag) {
        final byte[] newBytes = bytes(tag);
        assertArrayEquals(adventureBytes(tag), newBytes, "written bytes differ for " + tag);
        assertEquals(newBytes.length, NBT.sizeOf(tag), "sizeOf differs for " + tag);
        final BinaryTag read = read(newBytes);
        assertEquals(adventureRead(newBytes), read, "read results differ for " + tag);
        assertEquals(tag, read, "round trip failed for " + tag);
    }

    private static void assertRejected(Class<? extends Throwable> expected, Consumer<NetworkBuffer> writer) {
        final NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
        writer.accept(buffer);
        assertThrows(expected, () -> buffer.read(NBT));
    }

    private static BinaryTag nestedCompounds(int depth) {
        BinaryTag tag = CompoundBinaryTag.empty();
        for (int i = 1; i < depth; i++) tag = CompoundBinaryTag.builder().put("a", tag).build();
        return tag;
    }

    // Written by hand because the writer rejects anything past the limit.
    private static byte[] nestedCompoundBytes(int depth) {
        return NetworkBuffer.makeArray(buffer -> {
            buffer.write(BYTE, TAG_COMPOUND);
            for (int i = 1; i < depth; i++) {
                buffer.write(BYTE, TAG_COMPOUND);
                buffer.write(STRING_IO_UTF8, "a");
            }
            for (int i = 0; i < depth; i++) buffer.write(BYTE, TAG_END);
        });
    }

    private static BinaryTag randomTag(Random random, int depth) {
        return randomTag(random, depth, random.nextInt(depth <= 0 ? 10 : 12));
    }

    private static BinaryTag randomTag(Random random, int depth, int kind) {
        return switch (kind) {
            case 0 -> ByteBinaryTag.byteBinaryTag((byte) random.nextInt());
            case 1 -> ShortBinaryTag.shortBinaryTag((short) random.nextInt());
            case 2 -> IntBinaryTag.intBinaryTag(random.nextInt());
            case 3 -> LongBinaryTag.longBinaryTag(random.nextLong());
            case 4 -> FloatBinaryTag.floatBinaryTag(random.nextFloat());
            case 5 -> DoubleBinaryTag.doubleBinaryTag(random.nextDouble());
            case 6 -> {
                final byte[] array = new byte[random.nextInt(8)];
                random.nextBytes(array);
                yield ByteArrayBinaryTag.byteArrayBinaryTag(array);
            }
            case 7 -> StringBinaryTag.stringBinaryTag(randomString(random));
            case 8 -> {
                final int[] array = new int[random.nextInt(8)];
                for (int i = 0; i < array.length; i++) array[i] = random.nextInt();
                yield IntArrayBinaryTag.intArrayBinaryTag(array);
            }
            case 9 -> {
                final long[] array = new long[random.nextInt(8)];
                for (int i = 0; i < array.length; i++) array[i] = random.nextLong();
                yield LongArrayBinaryTag.longArrayBinaryTag(array);
            }
            case 10 -> {
                final int size = random.nextInt(5);
                if (size == 0) yield ListBinaryTag.empty();
                // Every entry of a list shares its kind, and therefore its type
                final int entryKind = random.nextInt(depth <= 1 ? 10 : 12);
                final List<BinaryTag> entries = new ArrayList<>();
                for (int i = 0; i < size; i++) entries.add(randomTag(random, depth - 1, entryKind));
                yield ListBinaryTag.listBinaryTag(entries.getFirst().type(), entries);
            }
            default -> {
                final CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                final int size = random.nextInt(5);
                for (int i = 0; i < size; i++) builder.put(randomString(random) + i, randomTag(random, depth - 1));
                yield builder.build();
            }
        };
    }

    private static String randomString(Random random) {
        final StringBuilder builder = new StringBuilder();
        final int length = random.nextInt(10);
        for (int i = 0; i < length; i++) builder.append((char) ('a' + random.nextInt(26)));
        return builder.toString();
    }
}
