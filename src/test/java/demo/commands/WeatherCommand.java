package demo.commands;

import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentTime;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.utils.time.UpdateOption;
import net.minestom.server.weather.Weather;
import net.minestom.server.weather.Weather.Type;
import net.minestom.server.weather.WeatherContainer;
import net.minestom.server.weather.manager.GlobalWeatherManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.minestom.server.MinecraftServer.getGlobalWeatherManager;
import static net.minestom.server.MinecraftServer.getInstanceManager;

public class WeatherCommand extends Command {
    // weather
    ArgumentEnum<Type> type = ArgumentType.Enum("type", Type.class)
            .setFormat(ArgumentEnum.Format.LOWER_CASED);
    ArgumentTime length = ArgumentType.Time("length");
    Argument<Float> rainStrength = ArgumentType.Float("rainStrength")
                .min(0f)
                .setDefaultValue(1f),
            thunderStrength = ArgumentType.Float("thunderStrength")
                .min(0f)
                .setDefaultValue(1f);

    public WeatherCommand() {
        super("weather");

        // operation
        var get = ArgumentType.Literal("get");
        var set = ArgumentType.Literal("set");

        // types
        var global = ArgumentType.Literal("global");
        var instance = ArgumentType.Literal("instance");
        var player = ArgumentType.Literal("player");

        // targets
        var instanceId = ArgumentType.UUID("instanceId");
        var playerId = ArgumentType.Entity("playerId")
                .onlyPlayers(true)
                .singleEntity(true);

        // syntax
        setDefaultExecutor((sender, context) -> sender.sendMessage(
                text("/weather <get | set> <global | instance [uuid]> | player [player]> <clear | rain | thunder> <length> [rainStrength] [thunderStrength]")
                    .hoverEvent(text("Click to get this command."))
                    .clickEvent(ClickEvent.suggestCommand("/weather "))));

        // == GETTERS ==

        // global
        addSyntax((sender, context) -> getWeather(getGlobalWeatherManager(), sender),
                get, global);

        // instance
        addConditionalSyntax(Conditions::playerOnly,
                (sender, context) -> getWeather(sender.asPlayer().getInstance(), sender),
                get, instance);
        addSyntax((sender, context) -> getWeather(getInstanceManager().getInstance(context.get(instanceId)), sender),
                get, instance, instanceId);

        // player
        addConditionalSyntax(Conditions::playerOnly,
                (sender, context) -> getWeather(sender.asPlayer(), sender),
                get, player);
        addSyntax((sender, context) -> getWeather(context.get(playerId).findFirstPlayer(sender), sender),
                get, player, playerId);

        // == SETTERS ==

        // global
        addSyntax((sender, context) -> setWeather(getGlobalWeatherManager(), sender, context),
                set, global, type, length, rainStrength, thunderStrength);

        // instance
        addSyntax((sender, context) -> setWeather(getInstanceManager().getInstance(context.get(instanceId)), sender, context),
                set, instance, instanceId, type, length, rainStrength, thunderStrength);

        // player
        addSyntax((sender, context) -> setWeather(context.get(playerId).findFirstPlayer(sender), sender, context),
                set, player, playerId, type, length, rainStrength, thunderStrength);
    }

    private void getWeather(@Nullable WeatherContainer container, @NotNull CommandSender sender) {
        if (container == null) {
            sender.sendMessage(text("Could not find the weather container!", RED));
        } else {
            if (!(container.getWeatherManager() instanceof GlobalWeatherManager)) {
                if (!container.hasWeather()) {
                    sender.sendMessage(text("The container is a child without any weather. However, it has the weather of it's parent."));
                }
            }

            var weather = container.getWeather();

            sender.sendMessage(text("Type: " + weather.getType().name() + ". "));

            var dur = weather.getRemainingDuration();
            if (dur == null) {
                sender.sendMessage(text("Remaining duration: forever!"));
            } else {
                sender.sendMessage(text(String.format("Remaining duration: %dh %dm %ds.",
                        dur.toHours(), dur.toMinutesPart(), dur.toSecondsPart())));
            }

            if (weather.getType() != Type.CLEAR) {
                sender.sendMessage(text("Rain strength: " + weather.getRainStrength() + ". "));
            }

            if (weather.getType() == Type.THUNDER) {
                sender.sendMessage(text("Thunder strength: " + weather.getThunderStrength() + "."));
            }
        }
    }

    private void setWeather(@Nullable WeatherContainer container, @NotNull CommandSender sender, @NotNull CommandContext context) {
        if (container == null) {
            sender.sendMessage(text("Could not find the weather container!", RED));
        } else {
            UpdateOption time = context.get(length);

            Weather weather = Weather.builder()
                    .type(context.get(type))
                    .rainStrength(context.get(rainStrength))
                    .thunderStrength(context.get(thunderStrength))
                    .length(time.getTimeUnit().toMilliseconds(time.getValue()))
                    .build();

            container.setWeather(weather);

            sender.sendMessage(text("The weather has been set successfully!"));
        }
    }
}
