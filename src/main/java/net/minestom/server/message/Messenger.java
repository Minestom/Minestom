package net.minestom.server.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.crypto.MessageSignature;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.PlayerChatMessagePacket;
import net.minestom.server.network.packet.server.play.SystemChatPacket;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;

import java.io.StringReader;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Utility class to handle client chat settings.
 */
public final class Messenger {
    /**
     * The message sent to the client if they send a chat message but it is rejected by the server.
     */
    public static final Component CANNOT_SEND_MESSAGE = Component.translatable("chat.cannotSend", NamedTextColor.RED);
    private static final SystemChatPacket CANNOT_SEND_PACKET = new SystemChatPacket(CANNOT_SEND_MESSAGE, ChatPosition.SYSTEM_MESSAGE.getID());

    private static final NBTCompound CHAT_REGISTRY;

    static {
        try {
            CHAT_REGISTRY = (NBTCompound) new SNBTParser(new StringReader("""
                    {
                       "type":"minecraft:chat_type",
                       "value":[
                          {
                             "element":{
                                "chat":{
                                   "decoration":{
                                      "parameters":[
                                         "sender",
                                         "content"
                                      ],
                                      "style":{
                                        \s
                                      },
                                      "translation_key":"chat.type.text"
                                   }
                                },
                                "narration":{
                                   "decoration":{
                                      "parameters":[
                                         "sender",
                                         "content"
                                      ],
                                      "style":{
                                        \s
                                      },
                                      "translation_key":"chat.type.text.narrate"
                                   },
                                   "priority":"chat"
                                }
                             },
                             "id":0,
                             "name":"minecraft:chat"
                          },
                          {
                             "element":{
                                "chat":{
                                  \s
                                },
                                "narration":{
                                   "priority":"system"
                                }
                             },
                             "id":1,
                             "name":"minecraft:system"
                          },
                          {
                             "element":{
                                "overlay":{
                                  \s
                                }
                             },
                             "id":2,
                             "name":"minecraft:game_info"
                          },
                          {
                             "element":{
                                "chat":{
                                   "decoration":{
                                      "parameters":[
                                         "sender",
                                         "content"
                                      ],
                                      "style":{
                                        \s
                                      },
                                      "translation_key":"chat.type.announcement"
                                   }
                                },
                                "narration":{
                                   "decoration":{
                                      "parameters":[
                                         "sender",
                                         "content"
                                      ],
                                      "style":{
                                        \s
                                      },
                                      "translation_key":"chat.type.text.narrate"
                                   },
                                   "priority":"chat"
                                }
                             },
                             "id":3,
                             "name":"minecraft:say_command"
                          },
                          {
                             "element":{
                                "chat":{
                                   "decoration":{
                                      "parameters":[
                                         "sender",
                                         "content"
                                      ],
                                      "style":{
                                         "color":"gray",
                                         "italic":1b
                                      },
                                      "translation_key":"commands.message.display.incoming"
                                   }
                                },
                                "narration":{
                                   "decoration":{
                                      "parameters":[
                                         "sender",
                                         "content"
                                      ],
                                      "style":{
                                        \s
                                      },
                                      "translation_key":"chat.type.text.narrate"
                                   },
                                   "priority":"chat"
                                }
                             },
                             "id":4,
                             "name":"minecraft:msg_command"
                          },
                          {
                             "element":{
                                "chat":{
                                   "decoration":{
                                      "parameters":[
                                         "team_name",
                                         "sender",
                                         "content"
                                      ],
                                      "style":{
                                        \s
                                      },
                                      "translation_key":"chat.type.team.text"
                                   }
                                },
                                "narration":{
                                   "decoration":{
                                      "parameters":[
                                         "sender",
                                         "content"
                                      ],
                                      "style":{
                                        \s
                                      },
                                      "translation_key":"chat.type.text.narrate"
                                   },
                                   "priority":"chat"
                                }
                             },
                             "id":5,
                             "name":"minecraft:team_msg_command"
                          },
                          {
                             "element":{
                                "chat":{
                                   "decoration":{
                                      "parameters":[
                                         "sender",
                                         "content"
                                      ],
                                      "style":{
                                        \s
                                      },
                                      "translation_key":"chat.type.emote"
                                   }
                                },
                                "narration":{
                                   "decoration":{
                                      "parameters":[
                                         "sender",
                                         "content"
                                      ],
                                      "style":{
                                        \s
                                      },
                                      "translation_key":"chat.type.emote"
                                   },
                                   "priority":"chat"
                                }
                             },
                             "id":6,
                             "name":"minecraft:emote_command"
                          },
                          {
                             "element":{
                                "chat":{
                                  \s
                                },
                                "narration":{
                                   "priority":"chat"
                                }
                             },
                             "id":7,
                             "name":"minecraft:tellraw_command"
                          }
                       ]
                    }
                    """)).parse();
        } catch (NBTException e) {
            throw new RuntimeException(e);
        }
    }

    public static @NotNull NBTCompound chatRegistry() {
        return CHAT_REGISTRY;
    }

    /**
     * Sends a message to a player, respecting their chat settings.
     *
     * @param player   the player
     * @param message  the message
     * @param position the position
     * @param uuid     the UUID of the sender, if any
     * @return if the message was sent
     */
    //fixme
    public static boolean sendMessage(@NotNull Player player, @NotNull Component message, @NotNull ChatPosition position, @Nullable UUID uuid) {
        if (getChatMessageType(player).accepts(position)) {
            player.sendPacket(new SystemChatPacket(message, 1));
            return true;
        }
        return false;
    }

    /**
     * Sends a message to some players, respecting their chat settings.
     *
     * @param recipients  the players
     * @param message  the message
     * @param position the position
     */
    public static void sendSignedPlayerMessage(@NotNull Collection<Player> recipients, @NotNull Component message,
                                               @NotNull ChatPosition position, @NotNull MessageSender sender,
                                               @NotNull MessageSignature signature) {
        PacketUtils.sendGroupedPacket(recipients.stream().filter(x -> x.getSettings().getChatMessageType() == null ||
                        x.getSettings().getChatMessageType().accepts(position)).collect(Collectors.toList()),
                PlayerChatMessagePacket.signed(message, position, sender, signature));
    }

    /**
     * Checks if the server should receive messages from a player, given their chat settings.
     *
     * @param player the player
     * @return if the server should receive messages from them
     */
    public static boolean canReceiveMessage(@NotNull Player player) {
        return getChatMessageType(player) == ChatMessageType.FULL;
    }

    /**
     * Checks if the server should receive commands from a player, given their chat settings.
     *
     * @param player the player
     * @return if the server should receive commands from them
     */
    public static boolean canReceiveCommand(@NotNull Player player) {
        return getChatMessageType(player) != ChatMessageType.NONE;
    }

    /**
     * Sends a message to the player informing them we are rejecting their message or command.
     *
     * @param player the player
     */
    public static void sendRejectionMessage(@NotNull Player player) {
        player.sendPacket(CANNOT_SEND_PACKET);
    }

    /**
     * Gets the chat message type for a player, returning {@link ChatMessageType#FULL} if not set.
     *
     * @param player the player
     * @return the chat message type
     */
    private static @NotNull ChatMessageType getChatMessageType(@NotNull Player player) {
        return Objects.requireNonNullElse(player.getSettings().getChatMessageType(), ChatMessageType.FULL);
    }
}
