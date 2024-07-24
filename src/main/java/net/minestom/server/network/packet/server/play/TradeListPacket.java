package net.minestom.server.network.packet.server.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;

public record TradeListPacket(int windowId, @NotNull List<Trade> trades,
                              int villagerLevel, int experience,
                              boolean regularVillager, boolean canRestock) implements ServerPacket.Play {
    public static final int MAX_TRADES = Short.MAX_VALUE;

    public static final NetworkBuffer.Type<TradeListPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, TradeListPacket::windowId,
            Trade.SERIALIZER.list(MAX_TRADES), TradeListPacket::trades,
            VAR_INT, TradeListPacket::villagerLevel,
            VAR_INT, TradeListPacket::experience,
            BOOLEAN, TradeListPacket::regularVillager,
            BOOLEAN, TradeListPacket::canRestock,
            TradeListPacket::new);

    public TradeListPacket {
        trades = List.copyOf(trades);
    }

    public record Trade(ItemStack inputItem1, ItemStack result,
                        ItemStack inputItem2, boolean tradeDisabled,
                        int tradeUsesNumber, int maxTradeUsesNumber, int exp,
                        int specialPrice, float priceMultiplier, int demand) {
        public static final NetworkBuffer.Type<Trade> SERIALIZER = NetworkBufferTemplate.template(
                ItemStack.NETWORK_TYPE, Trade::inputItem1,
                ItemStack.NETWORK_TYPE, Trade::result,
                ItemStack.NETWORK_TYPE.optional(), Trade::inputItem2,
                BOOLEAN, Trade::tradeDisabled,
                INT, Trade::tradeUsesNumber,
                INT, Trade::maxTradeUsesNumber,
                INT, Trade::exp,
                INT, Trade::specialPrice,
                FLOAT, Trade::priceMultiplier,
                INT, Trade::demand,
                Trade::new);
    }
}
