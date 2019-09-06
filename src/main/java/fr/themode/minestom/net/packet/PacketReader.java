package fr.themode.minestom.net.packet;

import com.github.simplenet.Client;
import com.github.simplenet.utility.exposed.consumer.BooleanConsumer;
import com.github.simplenet.utility.exposed.consumer.ByteConsumer;
import com.github.simplenet.utility.exposed.consumer.FloatConsumer;
import com.github.simplenet.utility.exposed.consumer.ShortConsumer;
import fr.themode.minestom.net.ConnectionUtils;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.Utils;
import fr.themode.minestom.utils.consumer.StringConsumer;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public class PacketReader {

    private Client client;
    private int length;
    private int sizeOffset;

    public PacketReader(Client client, int length, int sizeOffset) {
        this.client = client;
        this.length = length;
        this.sizeOffset = sizeOffset;
    }

    public void readVarInt(IntConsumer consumer) {
        ConnectionUtils.readVarInt(client, value -> {
            consumer.accept(value);
            sizeOffset += Utils.lengthVarInt(value);
        });
    }

    public void readBoolean(BooleanConsumer consumer) {
        sizeOffset += Byte.BYTES;
        client.readBoolean(consumer);
    }

    public void readByte(ByteConsumer consumer) {
        sizeOffset += Byte.BYTES;
        client.readByte(consumer);
    }

    public void readShort(ShortConsumer consumer) {
        sizeOffset += Short.BYTES;
        client.readShort(consumer);
    }

    public void readLong(LongConsumer consumer) {
        sizeOffset += Long.BYTES;
        client.readLong(consumer);
    }

    public void readFloat(FloatConsumer consumer) {
        sizeOffset += Float.BYTES;
        client.readFloat(consumer);
    }

    public void readDouble(DoubleConsumer consumer) {
        sizeOffset += Double.BYTES;
        client.readDouble(consumer);
    }

    public void readSizedString(StringConsumer consumer) {
        Utils.readString(client, consumer);
    }

    public void readSizedString(Consumer<String> consumer) {
        readSizedString((string, length1) -> consumer.accept(string));
    }

    public void getRemainingBytes(int offset, Consumer<byte[]> consumer) {
        int size = length - 1 - offset;
        client.readBytes(size, consumer);
    }

    public void getRemainingBytes(Consumer<byte[]> consumer) {
        getRemainingBytes(0, consumer);
    }

    public void readBlockPosition(Consumer<BlockPosition> consumer) {
        Utils.readPosition(client, consumer);
    }

}
