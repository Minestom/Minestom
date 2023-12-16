package net.minestom.server.network.packet.server.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;

public record TradeListPacket(int windowId, @NotNull List<Trade> trades,
                              int villagerLevel, int experience,
                              boolean regularVillager, boolean canRestock) implements ServerPacket {
    public TradeListPacket {
        trades = List.copyOf(trades);
    }

    public TradeListPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.readCollection(Trade::new),
                reader.read(VAR_INT), reader.read(VAR_INT),
                reader.read(BOOLEAN), reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, windowId);
        writer.writeCollection(trades);
        writer.write(VAR_INT, villagerLevel);
        writer.write(VAR_INT, experience);
        writer.write(BOOLEAN, regularVillager);
        writer.write(BOOLEAN, canRestock);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case PLAY -> ServerPacketIdentifier.TRADE_LIST;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.PLAY);
        };
    }

    public record Trade(ItemStack inputItem1, ItemStack result,
                        ItemStack inputItem2, boolean tradeDisabled,
                        int tradeUsesNumber, int maxTradeUsesNumber, int exp,
                        int specialPrice, float priceMultiplier, int demand) implements NetworkBuffer.Writer {
        public Trade(@NotNull NetworkBuffer reader) {
            this(reader.read(ITEM), reader.read(ITEM),
                    reader.readOptional(ITEM), reader.read(BOOLEAN),
                    reader.read(INT), reader.read(INT), reader.read(INT),
                    reader.read(INT), reader.read(FLOAT), reader.read(INT));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(ITEM, inputItem1);
            writer.write(ITEM, result);
            writer.writeOptional(ITEM, inputItem2);
            writer.write(BOOLEAN, tradeDisabled);
            writer.write(INT, tradeUsesNumber);
            writer.write(INT, maxTradeUsesNumber);
            writer.write(INT, exp);
            writer.write(INT, specialPrice);
            writer.write(FLOAT, priceMultiplier);
            writer.write(INT, demand);
        }
    }
}
