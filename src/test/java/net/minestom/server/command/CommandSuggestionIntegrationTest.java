package net.minestom.server.command;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.client.play.ClientTabCompletePacket;
import net.minestom.server.network.packet.server.play.TabCompletePacket;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@EnvTest
public class CommandSuggestionIntegrationTest {

    @Test
    public void suggestion(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0)).join();

        var command = new Command("test");
        command.addSyntax((sender, context) -> {

        }, Literal("arg").setSuggestionCallback((sender, context, suggestion) -> {
            assertEquals(player, sender);
            assertNull(context.get("arg"));
            assertEquals("test", context.getCommandName());
            assertEquals("test te", context.getInput());
            suggestion.addEntry(new SuggestionEntry("test1"));
        }));

        env.process().command().register(command);

        var listener = connection.trackIncoming(TabCompletePacket.class);
        player.addPacketToQueue(new ClientTabCompletePacket(3, "test te"));
        player.interpretPacketQueue();

        listener.assertSingle(tabCompletePacket -> {
            assertEquals(3, tabCompletePacket.transactionId());
            assertEquals(6, tabCompletePacket.start());
            assertEquals(2, tabCompletePacket.length());
            assertEquals(List.of(new TabCompletePacket.Match("test1", null)), tabCompletePacket.matches());
        });
    }
}
