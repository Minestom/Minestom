package net.minestom.server.network.player;

import net.minestom.server.network.NetworkBuffer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;

/**
 * HAProxy PROXY protocol v1 + v2 parser for Java NIO SocketChannel.
 *
 * Detects:
 *   - PROXY v1 text header
 *   - PROXY v2 binary header
 *   - no header
 *
 * Returns the real client address when present.
 */
final class ProxyProtocolDecoder {

    private static final byte[] V1_SIG = "PROXY ".getBytes();
    private static final NetworkBuffer.Type<byte[]> V1_SIG_BYTES = NetworkBuffer.FixedRawBytes(V1_SIG.length);

    private static final byte[] V2_SIG = new byte[]{
            0x0D, 0x0A, 0x0D, 0x0A,
            0x00, 0x0D, 0x0A, 0x51,
            0x55, 0x49, 0x54, 0x0A
    };
    private static final NetworkBuffer.Type<byte[]> V2_SIG_BYTES = NetworkBuffer.FixedRawBytes(V2_SIG.length);

    private static final NetworkBuffer.Type<byte[]> IPV4_BYTES = NetworkBuffer.FixedRawBytes(4);
    private static final NetworkBuffer.Type<byte[]> IPV6_BYTES = NetworkBuffer.FixedRawBytes(16);

    public record ClientInfo(
        SocketAddress clientAddress,
        boolean proxyUsed
    ) {
    }

    public static ClientInfo parse(SocketAddress original, NetworkBuffer buffer, final long length) throws IOException {
        if (length <= 0) {
            return new ClientInfo(original, false);
        }

        if (looksLikeV2(buffer)) {
            return parseV2(buffer, original);
        }

        if (looksLikeV1(buffer)) {
            return parseV1(buffer, original);
        }

        return new ClientInfo(original, false);
    }

    private static boolean looksLikeV1(NetworkBuffer buffer) {
        if (buffer.readableBytes() < 6) return false;
        byte[] p = buffer.readAt(buffer.readIndex(), V1_SIG_BYTES);

        return Arrays.equals(p, V1_SIG);
    }

    private static boolean looksLikeV2(NetworkBuffer buffer) {
        if (buffer.readableBytes() < 12) return false;
        byte[] sig = buffer.readAt(buffer.readIndex(), V2_SIG_BYTES);

        return Arrays.equals(sig, V2_SIG);
    }

    private static ClientInfo parseV1(NetworkBuffer buffer, SocketAddress fallback) {
        byte[] bytes = buffer.readAt(buffer.readIndex(), NetworkBuffer.RAW_BYTES);

        String s = new String(bytes);
        int end = s.indexOf("\r\n");
        if (end < 0) return new ClientInfo(fallback, false);

        String[] parts = s.substring(0, end).split(" ");

        if (parts.length >= 6 &&
                ("TCP4".equals(parts[1]) || "TCP6".equals(parts[1]))) {

            String ip = parts[2];
            int port = Integer.parseInt(parts[4]);

            //Move along so mc doesnt see the header
            buffer.advanceRead(s.substring(0, end + 2).getBytes().length);

            return new ClientInfo(
                    new InetSocketAddress(ip, port),
                    true
            );
        }

        return new ClientInfo(fallback, false);
    }

    private static ClientInfo parseV2(NetworkBuffer buffer, SocketAddress fallback) throws IOException {
        if (buffer.readableBytes() < 16) {
            return new ClientInfo(fallback, false);
        }

        buffer.advanceRead(12);

        byte verCmd = buffer.read(NetworkBuffer.BYTE);
        byte famProto = buffer.read(NetworkBuffer.BYTE);
        int len = buffer.read(NetworkBuffer.UNSIGNED_SHORT);

        int version = (verCmd >> 4) & 0xF;
        int command = verCmd & 0x0F;

        if (version != 2) {
            return new ClientInfo(fallback, false);
        }

        // LOCAL command => ignore forwarded info
        if (command == 0x00) {
            return new ClientInfo(fallback, false);
        }

        int family = (famProto >> 4) & 0x0F;

        if (family == 0x1) { // AF_INET
            if (len < 12 || buffer.readableBytes() < 12)
                return new ClientInfo(fallback, false);

            byte[] src = buffer.read(IPV4_BYTES);
            buffer.read(IPV4_BYTES); // dst ip

            int srcPort = buffer.read(NetworkBuffer.UNSIGNED_SHORT);
            buffer.read(NetworkBuffer.UNSIGNED_SHORT); // dst port

            InetAddress addr = InetAddress.getByAddress(src);
            return new ClientInfo(
                    new InetSocketAddress(addr, srcPort),
                    true
            );
        }

        if (family == 0x2) { // AF_INET6
            if (len < 36 || buffer.readableBytes() < 36)
                return new ClientInfo(fallback, false);

            byte[] src = buffer.read(IPV6_BYTES);
            buffer.read(IPV6_BYTES); // dst ip

            int srcPort = buffer.read(NetworkBuffer.UNSIGNED_SHORT);
            buffer.read(NetworkBuffer.UNSIGNED_SHORT); // dst port

            InetAddress addr = InetAddress.getByAddress(src);
            return new ClientInfo(
                    new InetSocketAddress(addr, srcPort),
                    true
            );
        }

        return new ClientInfo(fallback, false);
    }
}