package net.minestom.server.message;

import net.minestom.server.registry.RegistryKey;

/**
 * Code autogenerated, do not edit!
 */
@SuppressWarnings("unused")
interface ChatTypes {
    RegistryKey<ChatType> EMOTE_COMMAND = RegistryKey.unsafeOf("emote_command");

    RegistryKey<ChatType> TEAM_MSG_COMMAND_INCOMING = RegistryKey.unsafeOf("team_msg_command_incoming");

    RegistryKey<ChatType> TEAM_MSG_COMMAND_OUTGOING = RegistryKey.unsafeOf("team_msg_command_outgoing");

    RegistryKey<ChatType> CHAT = RegistryKey.unsafeOf("chat");

    RegistryKey<ChatType> MSG_COMMAND_INCOMING = RegistryKey.unsafeOf("msg_command_incoming");

    RegistryKey<ChatType> MSG_COMMAND_OUTGOING = RegistryKey.unsafeOf("msg_command_outgoing");

    RegistryKey<ChatType> SAY_COMMAND = RegistryKey.unsafeOf("say_command");
}
