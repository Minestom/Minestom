package net.minestom.server.network.packet.server.play;

import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

import static net.minestom.server.network.NetworkBuffer.*;

public record StopSoundPacket(Action action) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<StopSoundPacket> SERIALIZER = NetworkBufferTemplate.template(
            BYTE.unionType(Action::typeFromId, Action::flag), StopSoundPacket::action,
            StopSoundPacket::new
    );

    public StopSoundPacket {
        Objects.requireNonNull(action, "action");
    }

    public sealed interface Action {
        private static Type<? extends Action> typeFromId(byte id) {
            return switch (id) {
                case 0b00 -> All.SERIALIZER;
                case 0b01 -> Source.SERIALIZER;
                case 0b10 -> Sound.SERIALIZER;
                case 0b11 -> SourceAndSound.SERIALIZER;
                default -> throw new IllegalStateException("Unexpected value: " + id);
            };
        }

        @ApiStatus.Internal
        byte flag();
    }

    public record All() implements Action {
        public static final Type<All> SERIALIZER = NetworkBufferTemplate.template(new All());

        @Override
        public byte flag() {
            return 0b00;
        }
    }

    public record Source(net.kyori.adventure.sound.Sound.Source source) implements Action {
        public static final Type<Source> SERIALIZER = NetworkBufferTemplate.template(
                AdventurePacketConvertor.SOUND_SOURCE_TYPE, Source::source,
                Source::new
        );

        public Source {
            Objects.requireNonNull(source, "source");
        }

        @Override
        public byte flag() {
            return 0b01;
        }
    }

    public record Sound(String sound) implements Action {
        public static final Type<Sound> SERIALIZER = NetworkBufferTemplate.template(
                STRING, Sound::sound,
                Sound::new
        );

        public Sound {
            Objects.requireNonNull(sound, "sound");
        }

        @Override
        public byte flag() {
            return 0b10;
        }
    }

    public record SourceAndSound(net.kyori.adventure.sound.Sound.Source source, String sound) implements Action {
        public static final Type<SourceAndSound> SERIALIZER = NetworkBufferTemplate.template(
                AdventurePacketConvertor.SOUND_SOURCE_TYPE, SourceAndSound::source,
                STRING, SourceAndSound::sound,
                SourceAndSound::new
        );

        public SourceAndSound {
            Objects.requireNonNull(source, "source");
            Objects.requireNonNull(sound, "sound");
        }

        @Override
        public byte flag() {
            return 0b11;
        }
    }
}
