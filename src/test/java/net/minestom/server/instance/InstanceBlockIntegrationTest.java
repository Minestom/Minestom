package net.minestom.server.instance;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.network.packet.server.play.BlockEntityDataPacket;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.parser.SNBTParser;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class InstanceBlockIntegrationTest {

    @Test
    public void replaceAir(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        connection.connect(instance, new Pos(0, 40, 0)).join();

        var blockPoint = new Vec(5, 41, 0);

        assertEquals(Block.AIR, instance.getBlock(blockPoint));

        var tracker = connection.trackIncoming();
        instance.setBlock(blockPoint, Block.STONE);
        tracker.assertSingle(BlockChangePacket.class, packet -> {
            assertEquals(blockPoint, packet.blockPosition());
            assertEquals(Block.STONE.stateId(), packet.blockStateId());
        });

        assertEquals(Block.STONE, instance.getBlock(blockPoint));
    }

    @Test
    public void placeBlockEntity(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        connection.connect(instance, new Pos(0, 40, 0)).join();

        var blockPoint = new Vec(5, 41, 0);

        BlockHandler signHandler = new BlockHandler() {
            @Override
            public @NotNull Collection<Tag<?>> getBlockEntityTags() {
                return List.of(Tag.Byte("GlowingText"),
                        Tag.String("Color"),
                        Tag.String("Text1"),
                        Tag.String("Text2"),
                        Tag.String("Text3"),
                        Tag.String("Text4"));
            }

            @Override
            public @NotNull NamespaceID getNamespaceId() {
                return NamespaceID.from("minecraft:sign");
            }
        };

        assertEquals(Block.AIR, instance.getBlock(blockPoint));

        final Block block;
        final NBTCompound data;
        try {
            data = (NBTCompound) new SNBTParser(new StringReader("{\"GlowingText\":0B,\"Color\":\"black\",\"Text1\":\"{\\\"text\\\":\\\"wawsd\\\"}\"," +
                    "\"Text2\":\"{\\\"text\\\":\\\"\\\"}\",\"Text3\":\"{\\\"text\\\":\\\"\\\"}\",\"Text4\":\"{\\\"text\\\":\\\"\\\"}\"}")).parse();
            block = Block.OAK_SIGN.withHandler(signHandler).withNbt(data);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        var blockChangeTracker = connection.trackIncoming(BlockChangePacket.class);
        var blockEntityTracker = connection.trackIncoming(BlockEntityDataPacket.class);
        instance.setBlock(blockPoint, block);
        blockChangeTracker.assertSingle(packet -> {
            assertEquals(blockPoint, packet.blockPosition());
            assertEquals(block.stateId(), packet.blockStateId());
        });
        blockEntityTracker.assertSingle(packet -> {
            assertEquals(blockPoint, packet.blockPosition());
            assertEquals(block.registry().blockEntityId(), packet.action());
            assertEquals(data, packet.data());
        });

        assertEquals(block, instance.getBlock(blockPoint));
    }
}
