package net.minestom.server.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.HashMap;
import java.util.UUID;

public class TextAction {
    private static boolean enabled = false;
    private static final HashMap<String, CallbackInfo> callbackInfoHashMap = new HashMap<>();

    private TextAction() {

    }

    public static void enable() {
        Check.stateCondition(enabled, "TextAction is already enabled");
        enabled = true;
        MinecraftServer.getCommandManager().register(new CallbackCommand());
    }

    // TODO: Implement hashMap cleanup
    // TODO: Add configurable error messages
    // TODO: Add method overrides

    public static ClickEvent runCallback(Runnable callback, @Nullable Duration ttl, @Nullable CommandSender commandSender, @Nullable Integer limit) {
        Check.stateCondition(!enabled, "TextAction must be enabled by calling net.minestom.server.utils.TextAction.enable!");

        String id = UUID.randomUUID().toString();
        callbackInfoHashMap.put(id, new CallbackInfo(callback, commandSender, ttl, limit));
        return ClickEvent.runCommand("/minestom:callback " + id);
    }

    private static class CallbackInfo {
        private final Runnable runnable;
        private final CommandSender commandSender;
        private final long expireAt;
        private Integer usagesLeft;

        public CallbackInfo(Runnable runnable, CommandSender commandSender, Duration ttl, Integer limit) {
            this.runnable = runnable;
            this.commandSender = commandSender;
            this.expireAt = ttl != null ? ttl.toMillis() + System.currentTimeMillis() : Long.MAX_VALUE;
            this.usagesLeft = limit;
        }

        public Runnable getRunnable() {
            return runnable;
        }

        public CommandSender getCommandSender() {
            return commandSender;
        }

        public boolean isExpired() {
            return expireAt <= System.currentTimeMillis();
        }

        public boolean hasUsagesLeft() {
            return usagesLeft == null || usagesLeft > 0;
        }

        public boolean canBeUsedBy(CommandSender commandSender) {
            return this.commandSender == null || this.commandSender.equals(commandSender);
        }

        public void decrementUsagesLeft() {
            if (usagesLeft != null) usagesLeft -= 1;
        }
    }

    private static class CallbackCommand extends Command {
        private final ArgumentString id = new ArgumentString("id");

        public CallbackCommand() {
            super("minestom:callback");
            addSyntax(this::execute, id);
        }

        private void execute(@NotNull CommandSender commandSender, @NotNull CommandContext commandContext) {
            final CallbackInfo callbackInfo = callbackInfoHashMap.get(commandContext.get(id));

            // Conditions
            if (callbackInfo == null) return;
            if (callbackInfo.isExpired()) return;
            if (!callbackInfo.hasUsagesLeft()) return;
            if (!callbackInfo.canBeUsedBy(commandSender)) return;

            // Use
            callbackInfo.decrementUsagesLeft();
            callbackInfo.getRunnable().run();
        }
    }
}
