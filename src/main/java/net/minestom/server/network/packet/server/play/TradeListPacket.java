package net.minestom.server.network.packet.server.play;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;

public record TradeListPacket(int windowId, List<Trade> trades,
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

    public record Trade(
            ItemCost inputItem1,
            ItemStack result,
            @Nullable ItemCost inputItem2,
            boolean tradeDisabled,
            int tradeUsesNumber,
            int maxTradeUsesNumber,
            int exp,
            int specialPrice,
            float priceMultiplier,
            int demand
    ) {

        public static final NetworkBuffer.Type<Trade> SERIALIZER = NetworkBufferTemplate.template(
                ItemCost.NETWORK_TYPE, Trade::inputItem1,
                ItemStack.NETWORK_TYPE, Trade::result,
                ItemCost.NETWORK_TYPE.optional(), Trade::inputItem2,
                BOOLEAN, Trade::tradeDisabled,
                INT, Trade::tradeUsesNumber,
                INT, Trade::maxTradeUsesNumber,
                INT, Trade::exp,
                INT, Trade::specialPrice,
                FLOAT, Trade::priceMultiplier,
                INT, Trade::demand,
                Trade::new);

        public Trade(
                ItemStack inputItem1,
                ItemStack result,
                @Nullable ItemStack inputItem2,
                boolean tradeDisabled,
                int tradeUsesNumber,
                int maxTradeUsesNumber,
                int exp,
                int specialPrice,
                float priceMultiplier,
                int demand
        ) {
            this(
                    new ItemCost(inputItem1),
                    result,
                    inputItem2 == null ? null : new ItemCost(inputItem2),
                    tradeDisabled,
                    tradeUsesNumber,
                    maxTradeUsesNumber,
                    exp,
                    specialPrice,
                    priceMultiplier,
                    demand
            );
        }
    }

    public record ItemCost(Material material, int amount, DataComponentMap components) {
        private static final NetworkBuffer.Type<ItemCost> NETWORK_TYPE = NetworkBufferTemplate.template(
                Material.NETWORK_TYPE, ItemCost::material,
                VAR_INT, ItemCost::amount,
                DataComponent.MAP_NETWORK_TYPE, ItemCost::components,
                ItemCost::new);

        public ItemCost(ItemStack itemStack) {
            this(itemStack.material(), itemStack.amount(), itemStack.componentPatch());
        }
    }
}
