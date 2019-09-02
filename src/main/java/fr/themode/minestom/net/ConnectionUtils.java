package fr.themode.minestom.net;

import simplenet.Client;
import simplenet.packet.Packet;
import simplenet.utility.exposed.consumer.ByteConsumer;
import simplenet.utility.exposed.predicate.BytePredicate;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ConnectionUtils {

    /**
     * Reads a variable-length number from a {@link Client}
     *
     * @param client   The {@link Client} to read from
     * @param maxBytes The maximum size (in bytes) of the VarNum
     * @param callback The {@link LongConsumer} callback for the read VarNum
     */
    private static void readVarNum(Client client, int maxBytes, LongConsumer callback) {
        client.readByteUntil(new BytePredicate() {
            int count;
            long value;

            @Override
            public boolean test(byte data) {
                value |= ((int) data & 0x7F) << (7 * count);
                if (++count > maxBytes) throw new RuntimeException("VarNum is too big");
                if ((data & 0x80) == 0) {
                    callback.accept(value);
                    return false;
                } else return true;
            }
        });
    }

    /**
     * Writes a variable-length number to a byte array
     *
     * @param value The value to wrapPacket
     * @return The array containing the VarNum's bytes
     */
    public static byte[] writeVarNum(long value) {
        final byte[] bytes = new byte[10];
        int written = 0;
        do {
            byte temp = (byte) (value & 0x7F);
            value >>>= 7;
            if (value != 0) {
                temp |= 0x80;
            }
            bytes[written++] = temp;
        } while (value != 0);
        return Arrays.copyOfRange(bytes, 0, written);
    }

    /**
     * Reads a variable-length String from the {@link Client}
     *
     * @param client   The {@link Client} to read from
     * @param callback The {@link Consumer<String>} callback for the read String
     */
    public static void readString(Client client, Consumer<String> callback) {
        readVarInt(client, stringLength ->
                client.readBytes(stringLength, bytes ->
                        callback.accept(new String(bytes, UTF_8))
                )
        );
    }

    /**
     * Reads a variable-length integer from the client, up to 5 bytes long
     *
     * @param client   The client to read from
     * @param callback See {@link ConnectionUtils#readVarNum(Client, int, LongConsumer)}
     */
    public static void readVarInt(Client client, IntConsumer callback) {
        readVarNum(client, 5, it -> callback.accept((int) it));
    }

    /**
     * Reads a variable-length long from the client, up to 10 bytes long
     *
     * @param client   The client to read from
     * @param callback See {@link ConnectionUtils#readVarNum(Client, int, LongConsumer)}
     */
    public static void readVarLong(Client client, LongConsumer callback) {
        readVarNum(client, 10, callback);
    }

    /**
     * Writes a legacy string to a packet container
     * Legacy strings are prefixed with their length in characters as a short
     * Modern strings are prefixed with their length in bytes as a VarInt
     *
     * @param string  The String to write to the packet container
     * @param charset The {@link Charset} this string is encoded in
     * @param packet  The {@link Packet} for this string to be written to
     */
    public static void writeLegacyString(String string, Charset charset, Packet packet) {
        final int length = string.length();
        packet.putShort(length);
        packet.putBytes(string.getBytes(charset));
    }

    /**
     * Writes a legacy UTF-8 String to a packet container
     *
     * @param string The String to write
     * @param packet The {@link Packet} for this String to be written to
     */
    public static void writeLegacyString(String string, Packet packet) {
        writeLegacyString(string, UTF_8, packet);
    }

    /**
     * Writes a standard string to a packet container
     *
     * @param string  The String to write to the packet container
     * @param charset The {@link Charset} this String is encoded in
     * @param packet  The {@link Packet} for this string to be written to
     */
    public static void writeString(String string, Charset charset, Packet packet) {
        final byte[] bytes = string.getBytes(charset);
        final int length = bytes.length;
        final byte[] strLen = writeVarNum(length);
        packet.putBytes(strLen);
        packet.putBytes(bytes);
    }

    /**
     * Writes a standard UTF-8 string to a packet container
     *
     * @param string The String to write
     * @param packet The {@link Packet} for this String to be written to
     */
    public static void writeString(String string, Packet packet) {
        writeString(string, UTF_8, packet);
    }

    /**
     * Reads a Variable Length integer from the {@link Client}, restarting when complete
     *
     * @param client   The {@link Client} to read from
     * @param callback The {@link IntConsumer} callback for the read VarInt
     */
    public static void readVarIntAlways(Client client, IntConsumer callback) {
        client.readByteAlways(new ByteConsumer() {
            int count;
            long value;

            @Override
            public void accept(byte data) {
                value |= ((int) data & 0x7F) << (7 * count);
                if (++count > 5) throw new RuntimeException("VarNum is too big");
                if ((data & 0x80) == 0) {
                    callback.accept((int) value);
                    count = 0;
                    value = 0;
                }
            }
        });
    }

}
