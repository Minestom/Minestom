package net.minestom.demo.feature.dialog;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.minestom.demo.core.Feature;
import net.minestom.server.ServerProcess;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.dialog.Dialog;
import net.minestom.server.dialog.DialogAction;
import net.minestom.server.dialog.DialogActionButton;
import net.minestom.server.dialog.DialogAfterAction;
import net.minestom.server.dialog.DialogBody;
import net.minestom.server.dialog.DialogInput;
import net.minestom.server.dialog.DialogMetadata;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerCustomClickEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.io.IOException;
import java.util.List;

/** Chat-triggered multi-action dialog exercising every {@link DialogInput} type. */
public final class DialogFeature implements Feature {

    @Override
    public void register(ServerProcess process) {
        process.eventHandler().addListener(PlayerChatEvent.class, event -> {
            var dialog = new Dialog.MultiAction(
                    new DialogMetadata(
                            Component.text("Are you sure you want to confirm?".repeat(12))
                                    .hoverEvent(HoverEvent.showText(Component.text("Hover text here"))),
                            null, true, false,
                            DialogAfterAction.CLOSE,
                            List.of(
                                    new DialogBody.PlainMessage(
                                            Component.text("plain message here").hoverEvent(HoverEvent.showText(Component.text("Hover text here"))),
                                            DialogBody.PlainMessage.DEFAULT_WIDTH),
                                    new DialogBody.Item(ItemStack.of(Material.DIAMOND, 5),
                                            new DialogBody.PlainMessage(Component.text("item message"), DialogBody.PlainMessage.DEFAULT_WIDTH),
                                            false, true, 16, 16)
                            ),
                            List.of(
                                    new DialogInput.Text("text", DialogInput.DEFAULT_WIDTH * 2,
                                            Component.text("Enter some text").hoverEvent(HoverEvent.showText(Component.text("Hover text here"))),
                                            true, "", Integer.MAX_VALUE, new DialogInput.Text.Multiline(15, null)),
                                    new DialogInput.Boolean("bool", Component.text("Checkbox"), false, "true", "false"),
                                    new DialogInput.SingleOption("single_option", DialogInput.DEFAULT_WIDTH, List.of(
                                            new DialogInput.SingleOption.Option("option1", Component.text("Option 1"), true),
                                            new DialogInput.SingleOption.Option("option2", Component.text("Option 2"), false),
                                            new DialogInput.SingleOption.Option("option3", Component.text("Option 3"), false)
                                    ), Component.text("Single option"), true),
                                    new DialogInput.NumberRange("number_range", DialogInput.DEFAULT_WIDTH, Component.text("Number range"),
                                            "options.generic_value", 0, 500, 250f, 1f),
                                    new DialogInput.NumberRange("number_r2ange", DialogInput.DEFAULT_WIDTH, Component.text("Number range"),
                                            "options.generic_value", 0, 500, 250f, 1f),
                                    new DialogInput.NumberRange("number_r3ange", DialogInput.DEFAULT_WIDTH, Component.text("Number range"),
                                            "options.generic_value", 0, 500, 250f, 1f),
                                    new DialogInput.NumberRange("number_r4ange", DialogInput.DEFAULT_WIDTH, Component.text("Number range"),
                                            "options.generic_value", 0, 500, 250f, 1f),
                                    new DialogInput.NumberRange("number_r5ange", DialogInput.DEFAULT_WIDTH, Component.text("Number range"),
                                            "options.generic_value", 0, 500, 250f, 1f),
                                    new DialogInput.NumberRange("number_r6ange", DialogInput.DEFAULT_WIDTH, Component.text("Number range"),
                                            "options.generic_value", 0, 500, 250f, 1f)
                            )
                    ),
                    List.of(
                            new DialogActionButton(Component.text("Done"), null, DialogActionButton.DEFAULT_WIDTH, new DialogAction.DynamicCustom(Key.key("done_action"), null)),
                            new DialogActionButton(Component.text("Done"), null, DialogActionButton.DEFAULT_WIDTH, null)
                    ),
                    null, 2
            );

            event.getPlayer().sendMessage(Component.text("Click for dialog!").clickEvent(ClickEvent.showDialog(dialog)));
        });

        process.eventHandler().addListener(PlayerCustomClickEvent.class, event -> {
            String payload = "null";
            if (event.getPayload() != null) {
                try {
                    payload = MinestomAdventure.tagStringIO().asString(event.getPayload());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println(event.getKey() + " -> " + payload);
        });
    }
}
