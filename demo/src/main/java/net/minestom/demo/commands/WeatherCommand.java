package net.minestom.demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Weather;

public class WeatherCommand extends Command {
    public WeatherCommand() {
        super("weather");

        var rainLevel = ArgumentType.Float("rainLevel").setDefaultValue(0.0f);
        var thunderLevel = ArgumentType.Float("thunderLevel").setDefaultValue(0.0f);
        var transitionTicks = ArgumentType.Integer("transition").setDefaultValue(0);
        addSyntax(this::handleWeather, rainLevel, thunderLevel, transitionTicks);
    }

    private void handleWeather(CommandSender source, CommandContext context) {
        Player player = (Player) source;
        float rainLevel = context.get("rainLevel");
        float thunderLevel = context.get("thunderLevel");
        int transitionTicks = context.get("transition");
        player.getInstance().setWeather(new Weather(rainLevel, thunderLevel), transitionTicks);
    }
}
