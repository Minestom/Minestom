package net.minestom.server.utils;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;
import net.minestom.server.ServerFlag;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.SetSlotPacket;
import net.minestom.server.network.packet.server.play.SystemChatPacket;
import net.minestom.server.network.packet.server.play.UpdateScorePacket;
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@EnvTest //TODO(server-properties) Remove assumptions
public class TranslationIntegrationTest {

    @BeforeAll
    static void translator() {
        final var translator = new Translator() {
            @Override
            public Key name() {
                return Key.key("test.reg");
            }

            @Override
            public MessageFormat translate(String key, Locale locale) {
                if (!"test.key".equals(key)) return null;
                return new MessageFormat("This is a test message", MinestomAdventure.getDefaultLocale());
            }
        };

        GlobalTranslator.translator().addSource(translator);
    }

    @Test
    public void testTranslationEnabled(final Env env) {
        Assumptions.assumeTrue(ServerFlag.AUTOMATIC_COMPONENT_TRANSLATION);
        final var instance = env.createFlatInstance();
        final var connection = env.createConnection();
        final var player = connection.connect(instance, new Pos(0, 40, 0));
        final var collector = connection.trackIncoming(SystemChatPacket.class);

        final var message = Component.translatable("test.key");
        final var packet = new SystemChatPacket(message, false);
        PacketSendingUtils.sendGroupedPacket(List.of(player), packet);

        // the message should not be changed if translations are enabled.
        // the translation of the message itself will be proceeded in PlayerConnectionImpl class
        collector.assertSingle(received -> assertNotEquals(message, received.message()));
    }

    @Test
    public void testTranslationDisabled(final Env env) {
        Assumptions.assumeTrue(ServerFlag.AUTOMATIC_COMPONENT_TRANSLATION);
        final var instance = env.createFlatInstance();
        final var connection = env.createConnection();
        final var player = connection.connect(instance, new Pos(0, 40, 0));
        final var collector = connection.trackIncoming(SystemChatPacket.class);

        final var message = Component.translatable("test.key");
        final var packet = new SystemChatPacket(message, false);
        PacketSendingUtils.sendGroupedPacket(List.of(player), packet);

        collector.assertSingle(received -> assertEquals(message, received.message()));
    }

    @Test
    public void testItemStackTranslation(final Env env) {
        Assumptions.assumeTrue(ServerFlag.AUTOMATIC_COMPONENT_TRANSLATION);
        final var instance = env.createFlatInstance();
        final var connection = env.createConnection();
        final var player = connection.connect(instance, new Pos(0, 40, 0));
        final var collector = connection.trackIncoming(SetSlotPacket.class);

        final var message = Component.translatable("test.key");
        final var itemStack = ItemStack.of(Material.STONE)
                .with(DataComponents.ITEM_NAME, message)
                .with(DataComponents.CUSTOM_NAME, message);
        final var packet = new SetSlotPacket((byte) 0x01, 1, (short) 1, itemStack);
        PacketSendingUtils.sendGroupedPacket(List.of(player), packet);

        collector.assertSingle(received -> {
            assertNotEquals(message, received.itemStack().get(DataComponents.ITEM_NAME));
            assertNotEquals(message, received.itemStack().get(DataComponents.CUSTOM_NAME));
        });
    }

    @Test
    public void testUpdateScorePacketTranslations(final Env env) {
        Assumptions.assumeTrue(ServerFlag.AUTOMATIC_COMPONENT_TRANSLATION);
        final var instance = env.createFlatInstance();
        final var connection = env.createConnection();
        final var player = connection.connect(instance, new Pos(0, 40, 0));
        final var collector = connection.trackIncoming(UpdateScorePacket.class);

        final var message = Component.translatable("test.key");
        final var numberFormat = Sidebar.NumberFormat.fixed(message);
        final var packet = new UpdateScorePacket(
                "",
                "",
                0,
                message,
                numberFormat
        );
        PacketSendingUtils.sendGroupedPacket(List.of(player), packet);

        collector.assertSingle(received -> {
            assertNotEquals(message, received.displayName());
            assertNotEquals(message, received.numberFormat().content());
        });

    }
}
