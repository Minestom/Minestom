package net.minestom.scratch.tools;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.play.*;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.utils.chunk.ChunkUtils;

import java.util.function.Consumer;

public interface ScratchFeature extends Consumer<ClientPacket> {
    record Messaging(Mapping mapping) implements ScratchFeature {
        @Override
        public void accept(ClientPacket packet) {
            if (packet instanceof ClientChatMessagePacket chatMessagePacket) {
                final String message = chatMessagePacket.message();
                final Component formatted = mapping.formatMessage(message);
                mapping.signal(new SystemChatPacket(formatted, false));
            }
        }

        public interface Mapping {
            Component formatMessage(String message);

            void signal(ServerPacket.Play packet);
        }
    }

    record Movement(Mapping mapping) implements ScratchFeature {
        @Override
        public void accept(ClientPacket packet) {
            final int id = mapping.id();
            if (packet instanceof ClientPlayerPositionAndRotationPacket positionAndRotationPacket) {
                final Pos position = positionAndRotationPacket.position();
                mapping.updatePosition(position);
                mapping.signalMovement(new EntityTeleportPacket(id, position, positionAndRotationPacket.onGround()));
                mapping.signalMovement(new EntityHeadLookPacket(id, position.yaw()));
            } else if (packet instanceof ClientPlayerPositionPacket positionPacket) {
                final Pos position = mapping.position().withCoord(positionPacket.position());
                mapping.updatePosition(position);
                mapping.signalMovement(new EntityTeleportPacket(id, position, positionPacket.onGround()));
            } else if (packet instanceof ClientPlayerRotationPacket rotationPacket) {
                final Pos position = mapping.position().withView(rotationPacket.yaw(), rotationPacket.pitch());
                mapping.signalMovement(new EntityRotationPacket(id, position.yaw(), position.pitch(), rotationPacket.onGround()));
                mapping.signalMovement(new EntityHeadLookPacket(id, position.yaw()));
            }
        }

        public interface Mapping {
            int id();

            Pos position();

            void updatePosition(Pos position);

            void signalMovement(ServerPacket.Play packet);
        }
    }

    record ChunkLoading(Mapping mapping) implements ScratchFeature {
        @Override
        public void accept(ClientPacket packet) {
            final Pos oldPosition = mapping.oldPosition();
            Pos position = null;
            if (packet instanceof ClientPlayerPositionAndRotationPacket positionAndRotationPacket) {
                position = positionAndRotationPacket.position();
            } else if (packet instanceof ClientPlayerPositionPacket positionPacket) {
                position = Pos.fromPoint(positionPacket.position());
            }
            if (position == null || position.sameChunk(oldPosition)) return;
            final int oldChunkX = oldPosition.chunkX();
            final int oldChunkZ = oldPosition.chunkZ();
            final int newChunkX = position.chunkX();
            final int newChunkZ = position.chunkZ();
            mapping.sendPacket(new UpdateViewPositionPacket(newChunkX, newChunkZ));
            ChunkUtils.forDifferingChunksInRange(newChunkX, newChunkZ, oldChunkX, oldChunkZ,
                    mapping.viewDistance(),
                    (x, z) -> mapping.sendPacket(mapping.chunkPacket(x, z)),
                    (x, z) -> mapping.sendPacket(new UnloadChunkPacket(x, z)));
        }

        public interface Mapping {
            int viewDistance();

            Pos oldPosition();

            ChunkDataPacket chunkPacket(int chunkX, int chunkZ);

            void sendPacket(ServerPacket.Play packet);
        }
    }

    record EntityInteract(Mapping mapping) implements ScratchFeature {
        @Override
        public void accept(ClientPacket packet) {
            if (packet instanceof ClientInteractEntityPacket interactEntityPacket) {
                final int targetId = interactEntityPacket.targetId();
                final ClientInteractEntityPacket.Type type = interactEntityPacket.type();
                if (type instanceof ClientInteractEntityPacket.Interact interact) {
                    mapping.right(targetId);
                } else if (type instanceof ClientInteractEntityPacket.Attack attack) {
                    mapping.left(targetId);
                }
            }
        }

        public interface Mapping {
            void left(int id);

            void right(int id);
        }
    }

    record BlockInteract(Mapping mapping) implements ScratchFeature {
        @Override
        public void accept(ClientPacket packet) {
            if (packet instanceof ClientPlayerDiggingPacket diggingPacket) {
                final ClientPlayerDiggingPacket.Status status = diggingPacket.status();
                final Point blockPosition = diggingPacket.blockPosition();
                mapping.acknowledge(new AcknowledgeBlockChangePacket(diggingPacket.sequence()));
                switch (status) {
                    case STARTED_DIGGING -> {
                        if (mapping.creative()) {
                            mapping.breakBlock(blockPosition);
                        }
                    }
                }
            }
        }

        public interface Mapping {
            boolean creative();

            void breakBlock(Point point);

            void acknowledge(ServerPacket.Play packet);
        }
    }
}
