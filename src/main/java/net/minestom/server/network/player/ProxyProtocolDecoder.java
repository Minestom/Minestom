package net.minestom.server.network.player;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * HAProxy PROXY protocol v1 + v2 parser for Java NIO SocketChannel.
 * <p>
 * Detects:
 * - PROXY v1 text header
 * - PROXY v2 binary header
 * - no header
 * <p>
 * Returns the real client address when present.
 */
@ApiStatus.Internal
final class ProxyProtocolDecoder {

    private static final byte[] V1_SIG = "PROXY ".getBytes(StandardCharsets.US_ASCII);
    // Number pulled from https://www.haproxy.org/download/1.8/doc/proxy-protocol.txt
    // The specification calls for a 108-byte buffer: at most 107 on-wire header bytes
    // followed by a trailing zero for C string processing.
    private static final int V1_HEADER_MAX_LENGTH = 107;

    private static final byte[] V2_SIG = new byte[]{
            0x0D, 0x0A, 0x0D, 0x0A,
            0x00, 0x0D, 0x0A, 0x51,
            0x55, 0x49, 0x54, 0x0A
    };
    private static final int V2_HEADER_LENGTH = 16;
    private static final NetworkBuffer.Type<byte[]> V2_SIG_BYTES = NetworkBuffer.FixedRawBytes(V2_SIG.length);
    private static final NetworkBuffer.Type<V2Header> V2_HEADER_TYPE = NetworkBufferTemplate.template(
            V2_SIG_BYTES, V2Header::signature,
            NetworkBuffer.BYTE, V2Header::versionAndCommand,
            NetworkBuffer.BYTE, V2Header::familyAndProtocol,
            NetworkBuffer.UNSIGNED_SHORT, V2Header::payloadLength,
            V2Header::new);
    private static final NetworkBuffer.Type<V2Address> IPV4_ADDRESS_TYPE = v2AddressType(4);
    private static final NetworkBuffer.Type<V2Address> IPV6_ADDRESS_TYPE = v2AddressType(16);

    static Result parse(SocketAddress originalAddress, NetworkBuffer buffer) throws IOException {
        final long readableBytes = buffer.readableBytes();
        if (readableBytes == 0) return new Result(Status.NEED_MORE, originalAddress);

        final int prefixLength = (int) Math.min(readableBytes, V2_SIG.length);
        final byte[] prefix = buffer.readAt(buffer.readIndex(), NetworkBuffer.FixedRawBytes(prefixLength));
        final boolean v1Prefix = matchesPrefix(prefix, V1_SIG);
        final boolean v2Prefix = matchesPrefix(prefix, V2_SIG);
        if (!v1Prefix && !v2Prefix) return new Result(Status.ABSENT, originalAddress);
        if ((v1Prefix && readableBytes < V1_SIG.length) ||
                (v2Prefix && readableBytes < V2_SIG.length)) {
            return new Result(Status.NEED_MORE, originalAddress);
        }
        if (v1Prefix) return parseV1(originalAddress, buffer);
        return parseV2(originalAddress, buffer);
    }

    private static Result parseV1(SocketAddress originalAddress, NetworkBuffer buffer) throws IOException {
        final int available = (int) Math.min(buffer.readableBytes(), V1_HEADER_MAX_LENGTH);
        final byte[] bytes = buffer.readAt(buffer.readIndex(), NetworkBuffer.FixedRawBytes(available));
        int headerLength = -1;
        for (int i = V1_SIG.length; i < available; i++) {
            if (bytes[i - 1] == '\r' && bytes[i] == '\n') {
                headerLength = i + 1;
                break;
            }
        }
        if (headerLength == -1) {
            if (buffer.readableBytes() < V1_HEADER_MAX_LENGTH) return new Result(Status.NEED_MORE, originalAddress);
            throw new IOException("PROXY protocol v1 header exceeds " + V1_HEADER_MAX_LENGTH + " bytes");
        }

        final String[] parts = new String(bytes, 0, headerLength - 2, StandardCharsets.US_ASCII).split(" ", 0);
        final SocketAddress clientAddress;
        if (parts.length >= 2 && parts[1].equals("UNKNOWN")) {
            clientAddress = originalAddress;
        } else if (parts.length == 6 && (parts[1].equals("TCP4") || parts[1].equals("TCP6"))) {
            try {
                final InetAddress address = InetAddress.ofLiteral(parts[2]);
                final int port = Integer.parseInt(parts[4]);
                if (port < 0 || port > 65535) throw new IllegalArgumentException();
                final boolean expectedIpv4 = parts[1].equals("TCP4");
                if (expectedIpv4 != (address.getAddress().length == 4)) throw new IllegalArgumentException();
                clientAddress = new InetSocketAddress(address, port);
            } catch (IllegalArgumentException exception) {
                throw new IOException("Invalid PROXY protocol v1 source address", exception);
            }
        } else {
            throw new IOException("Invalid PROXY protocol v1 header");
        }
        buffer.advanceRead(headerLength);
        return new Result(Status.PRESENT, clientAddress);
    }

    private static Result parseV2(SocketAddress originalAddress, NetworkBuffer buffer) throws IOException {
        if (buffer.readableBytes() < V2_HEADER_LENGTH) return new Result(Status.NEED_MORE, originalAddress);
        final V2Header fixedHeader = buffer.readAt(buffer.readIndex(), V2_HEADER_TYPE);
        final int versionAndCommand = Byte.toUnsignedInt(fixedHeader.versionAndCommand());
        final int familyAndProtocol = Byte.toUnsignedInt(fixedHeader.familyAndProtocol());
        final int payloadLength = fixedHeader.payloadLength();
        final int headerLength = V2_HEADER_LENGTH + payloadLength;
        if (buffer.readableBytes() < headerLength) return new Result(Status.NEED_MORE, originalAddress);

        if (versionAndCommand >>> 4 != 2) throw new IOException("Invalid PROXY protocol v2 version");
        final int command = versionAndCommand & 0x0F;
        if (command == 0) {
            buffer.advanceRead(headerLength);
            return new Result(Status.PRESENT, originalAddress);
        }
        if (command != 1) throw new IOException("Invalid PROXY protocol v2 command");

        final int family = familyAndProtocol >>> 4;
        final int protocol = familyAndProtocol & 0x0F;
        final int addressLength = switch (family) {
            case 0 -> 0;
            case 1 -> 12;
            case 2 -> 36;
            case 3 -> 216;
            default -> -1;
        };
        if (addressLength < 0 || protocol > 2 || (family == 0) != (protocol == 0))
            throw new IOException("Invalid PROXY protocol v2 family and protocol");
        if (payloadLength < addressLength) throw new IOException("Truncated PROXY protocol v2 address");
        if (addressLength == 0 || protocol != 1 || family == 3) {
            buffer.advanceRead(headerLength);
            return new Result(Status.PRESENT, originalAddress);
        }

        final NetworkBuffer header = buffer.slice(buffer.readIndex(), headerLength, 0, headerLength);
        header.read(V2_HEADER_TYPE);
        final V2Address address = header.read(family == 1 ? IPV4_ADDRESS_TYPE : IPV6_ADDRESS_TYPE);
        buffer.advanceRead(headerLength);
        return new Result(Status.PRESENT,
                new InetSocketAddress(InetAddress.getByAddress(address.source()), address.sourcePort()));
    }

    private static NetworkBuffer.Type<V2Address> v2AddressType(int addressLength) {
        final NetworkBuffer.Type<byte[]> addressType = NetworkBuffer.FixedRawBytes(addressLength);
        return NetworkBufferTemplate.template(
                addressType, V2Address::source,
                addressType, V2Address::destination,
                NetworkBuffer.UNSIGNED_SHORT, V2Address::sourcePort,
                NetworkBuffer.UNSIGNED_SHORT, V2Address::destinationPort,
                V2Address::new);
    }

    private static boolean matchesPrefix(byte[] bytes, byte[] signature) {
        final int length = Math.min(bytes.length, signature.length);
        for (int i = 0; i < length; i++) {
            if (bytes[i] != signature[i]) return false;
        }
        return true;
    }

    enum Status {
        NEED_MORE,
        ABSENT,
        PRESENT
    }

    record Result(Status status, SocketAddress clientAddress) {
    }

    private record V2Header(byte[] signature, byte versionAndCommand, byte familyAndProtocol, int payloadLength) {
    }

    private record V2Address(byte[] source, byte[] destination, int sourcePort, int destinationPort) {
    }
}
