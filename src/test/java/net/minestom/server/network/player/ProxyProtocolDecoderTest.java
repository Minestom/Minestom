package net.minestom.server.network.player;

import net.minestom.server.network.NetworkBuffer;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProxyProtocolDecoderTest {
    private static final InetSocketAddress ORIGINAL = new InetSocketAddress("127.0.0.1", 25565);

    @Test
    void waitsForFragmentedV1Header() throws Exception {
        final byte[] bytes = "PROXY TCP4 192.0.2.1 198.51.100.2 12345 25565\r\n\1"
                .getBytes(StandardCharsets.US_ASCII);
        final NetworkBuffer buffer = NetworkBuffer.wrap(bytes, 0, 3);
        assertEquals(ProxyProtocolDecoder.Status.NEED_MORE, ProxyProtocolDecoder.parse(ORIGINAL, buffer).status());
        assertEquals(0, buffer.readIndex());

        buffer.writeIndex(bytes.length);
        final ProxyProtocolDecoder.Result result = ProxyProtocolDecoder.parse(ORIGINAL, buffer);
        assertEquals(ProxyProtocolDecoder.Status.PRESENT, result.status());
        assertEquals(new InetSocketAddress("192.0.2.1", 12345), result.clientAddress());
        assertEquals((byte) 1, buffer.read(NetworkBuffer.BYTE));
    }

    @Test
    void consumesEntireV2HeaderIncludingTlvs() throws Exception {
        final byte[] bytes = {
                0x0D, 0x0A, 0x0D, 0x0A, 0x00, 0x0D, 0x0A, 0x51, 0x55, 0x49, 0x54, 0x0A,
                0x21, 0x11, 0x00, 0x0F,
                (byte) 192, 0, 2, 10, (byte) 198, 51, 100, 20,
                0x30, 0x39, 0x63, (byte) 0xDD,
                0x01, 0x00, 0x00,
                0x02
        };
        final NetworkBuffer buffer = NetworkBuffer.wrap(bytes, 0, bytes.length);
        final ProxyProtocolDecoder.Result result = ProxyProtocolDecoder.parse(ORIGINAL, buffer);
        assertEquals(new InetSocketAddress("192.0.2.10", 12345), result.clientAddress());
        assertEquals((byte) 2, buffer.read(NetworkBuffer.BYTE));
    }

    @Test
    void validatesV2FamilyProtocolAndAddressLength() {
        final byte[] invalidCombination = v2Header((byte) 0x21, (byte) 0x13, 0);
        assertThrows(Exception.class, () -> ProxyProtocolDecoder.parse(
                ORIGINAL, NetworkBuffer.wrap(invalidCombination, 0, invalidCombination.length)));

        final byte[] truncatedUdp4 = v2Header((byte) 0x21, (byte) 0x12, 11);
        assertThrows(Exception.class, () -> ProxyProtocolDecoder.parse(
                ORIGINAL, NetworkBuffer.wrap(truncatedUdp4, 0, truncatedUdp4.length)));
    }

    @Test
    void distinguishesAbsentAndMalformedHeaders() throws Exception {
        final NetworkBuffer minecraft = NetworkBuffer.wrap(new byte[]{1, 0}, 0, 2);
        assertEquals(ProxyProtocolDecoder.Status.ABSENT, ProxyProtocolDecoder.parse(ORIGINAL, minecraft).status());
        assertEquals(0, minecraft.readIndex());

        final byte[] malformed = "PROXY TCP4 hostname target 12 34\r\n".getBytes(StandardCharsets.US_ASCII);
        final NetworkBuffer proxy = NetworkBuffer.wrap(malformed, 0, malformed.length);
        assertThrows(Exception.class, () -> ProxyProtocolDecoder.parse(ORIGINAL, proxy));
        assertEquals(0, proxy.readIndex());
    }

    @Test
    void acceptsMaximumLengthV1UnknownHeader() throws Exception {
        final byte[] bytes = ("PROXY UNKNOWN " + "x".repeat(91) + "\r\n\1")
                .getBytes(StandardCharsets.US_ASCII);
        assertEquals(108, bytes.length);

        final NetworkBuffer buffer = NetworkBuffer.wrap(bytes, 0, bytes.length);
        final ProxyProtocolDecoder.Result result = ProxyProtocolDecoder.parse(ORIGINAL, buffer);
        assertEquals(ProxyProtocolDecoder.Status.PRESENT, result.status());
        assertEquals(ORIGINAL, result.clientAddress());
        assertEquals((byte) 1, buffer.read(NetworkBuffer.BYTE));
    }

    private static byte[] v2Header(byte versionAndCommand, byte familyAndProtocol, int payloadLength) {
        final byte[] bytes = new byte[16 + payloadLength];
        final byte[] signature = {0x0D, 0x0A, 0x0D, 0x0A, 0x00, 0x0D, 0x0A, 0x51, 0x55, 0x49, 0x54, 0x0A};
        System.arraycopy(signature, 0, bytes, 0, signature.length);
        bytes[12] = versionAndCommand;
        bytes[13] = familyAndProtocol;
        bytes[14] = (byte) (payloadLength >>> 8);
        bytes[15] = (byte) payloadLength;
        return bytes;
    }
}
