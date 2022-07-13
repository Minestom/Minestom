package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

@SuppressWarnings("ConstantConditions")
public class CommandPacketFilteringTest {
    private static final Player PLAYER = new Player(UUID.randomUUID(), "", null);

    @Test
    public void singleCommandFilteredFalse() {
        final Command foo = new Command("foo");
        foo.setCondition(((sender, commandString) -> false));
        final DeclareCommandsPacket packet = GraphConverter.createPacket(Graph.merge(Set.of(foo)), PLAYER);
        CommandTestUtils.assertPacket(packet, "");
    }

    @Test
    public void singleCommandFilteredTrue() {
        final Command foo = new Command("foo");
        foo.setCondition(((sender, commandString) -> true));
        final DeclareCommandsPacket packet = GraphConverter.createPacket(Graph.merge(Set.of(foo)), PLAYER);
        CommandTestUtils.assertPacket(packet, """
                foo=%
                0->foo
                """);
    }

    @Test
    public void singleCommandUnfiltered() {
        final Command foo = new Command("foo");
        final DeclareCommandsPacket packet = GraphConverter.createPacket(Graph.merge(Set.of(foo)), PLAYER);
        CommandTestUtils.assertPacket(packet, """
                foo=%
                0->foo
                """);
    }

    @Test
    public void singleCommandFilteredTrueWithFilteredSubcommandTrueWithFilteredSyntaxFalse() {
        final Command foo = new Command("foo");
        foo.setCondition((sender, commandString) -> true);
        final Command bar = new Command("bar");
        bar.setCondition((sender, commandString) -> true);
        foo.addSubcommand(bar);
        bar.addConditionalSyntax((sender, commandString) -> false, null, ArgumentType.Literal("baz"));
        final DeclareCommandsPacket packet = GraphConverter.createPacket(Graph.merge(Set.of(foo)), PLAYER);
        CommandTestUtils.assertPacket(packet, """
                foo bar=%
                0->foo
                foo->bar
                """);
    }

    @Test
    public void singleCommandFilteredTrueWithFilteredSubcommandFalse() {
        final Command foo = new Command("foo");
        foo.setCondition((sender, commandString) -> true);
        final Command bar = new Command("bar");
        bar.setCondition((sender, commandString) -> false);
        foo.addSubcommand(bar);
        final DeclareCommandsPacket packet = GraphConverter.createPacket(Graph.merge(Set.of(foo)), PLAYER);
        CommandTestUtils.assertPacket(packet, """
                foo=%
                0->foo
                """);
    }

    @Test
    public void singleCommandFilteredTrueWithFilteredSubcommandTrue() {
        final Command foo = new Command("foo");
        foo.setCondition((sender, commandString) -> true);
        final Command bar = new Command("bar");
        bar.setCondition((sender, commandString) -> true);
        foo.addSubcommand(bar);
        final DeclareCommandsPacket packet = GraphConverter.createPacket(Graph.merge(Set.of(foo)), PLAYER);
        CommandTestUtils.assertPacket(packet, """
                foo bar=%
                0->foo
                foo->bar
                """);
    }

    @Test
    public void singleCommandFilteredTrueWithFilteredSubcommandTrueWithFilteredSyntaxBoth() {
        final Command foo = new Command("foo");
        foo.setCondition((sender, commandString) -> true);
        final Command bar = new Command("bar");
        bar.setCondition((sender, commandString) -> true);
        foo.addSubcommand(bar);
        bar.addConditionalSyntax((sender, commandString) -> true, null, ArgumentType.Literal("true"));
        bar.addConditionalSyntax((sender, commandString) -> false, null, ArgumentType.Literal("false"));
        final DeclareCommandsPacket packet = GraphConverter.createPacket(Graph.merge(Set.of(foo)), PLAYER);
        CommandTestUtils.assertPacket(packet, """
                foo bar true=%
                0->foo
                foo->bar
                bar->true
                """);
    }
}
