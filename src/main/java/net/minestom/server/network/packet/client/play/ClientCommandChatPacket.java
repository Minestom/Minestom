package net.minestom.server.network.packet.client.play;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.minecraft.SignableArgument;
import net.minestom.server.crypto.MessageSignature;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record ClientCommandChatPacket(@NotNull String message, long timestamp,
                                      long salt, Map<String, byte[]> signatures,
                                      boolean signedPreview) implements ClientPacket {
    public ClientCommandChatPacket {
        signatures = Map.copyOf(signatures);
    }

    public ClientCommandChatPacket(BinaryReader reader) {
        this(reader.readSizedString(256), reader.readLong(),
                reader.readLong(), readSignature(reader), reader.readBoolean());
    }

    /**
     * Constructs a new {@link MessageSignature} to use in validation
     *
     * @param signer player's uuid who sent this packet
     * @return null if the client didn't sign the parameter
     */
    @Contract("_, _ -> new")
    public @Nullable MessageSignature signatureOf(String parameterName, UUID signer) {
        final byte[] signature = signatures.get(parameterName);
        return signature == null ? null : new MessageSignature(signer, Instant.ofEpochMilli(timestamp), salt, signature);
    }

    /**
     * @see #signatureOf(String, UUID)
     */
    @SuppressWarnings("ConstantConditions")
    @Contract("_, _ -> new")
    public <T extends Argument<?> & SignableArgument> @Nullable MessageSignature signatureOf(T argument, UUID signer) {
        return signatureOf(argument.getId(), signer);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(message);
        writer.writeLong(timestamp);
        writer.writeLong(salt);
        this.signatures.forEach((s, bytes) -> {
            writer.writeSizedString(s);
            writer.writeByteArray(bytes);
        });
        writer.writeBoolean(signedPreview);
    }

    private static Map<String, byte[]> readSignature(BinaryReader reader) {
        final int length = reader.readVarInt();
        Map<String, byte[]> signatures = new HashMap<>();
        for (int i = 0; i < length; i++) {
            final String s = reader.readSizedString(256);
            final byte[] bytes = reader.readByteArray();
            signatures.put(s, bytes);
        }
        return Map.copyOf(signatures);
    }
}
