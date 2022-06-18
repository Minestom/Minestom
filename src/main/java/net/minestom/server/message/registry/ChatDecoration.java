package net.minestom.server.message.registry;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.MinecraftServer;
import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.nbt.NBTType;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;
import org.jglrxavpok.hephaistos.parser.SNBTParser;

import java.io.StringReader;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Defines how the message will appear
 *
 * @param translationKey translation used to provide the message format, can also be
 *                       custom string with %s as param placeholder
 * @param params params that will be used in place of %s in the translationKey
 * @param style style of the template, params can override this
 */
public record ChatDecoration(@NotNull String translationKey, @NotNull List<Parameter> params, @NotNull Style style) implements NBTCompoundWriteable {
    private static final String REGEX_ONE_PARAM_OR_TRANSLATION_KEY = "(^(?:(?!%s).)*%s(?!.*%s).*$)|(^\\w+(\\.\\w+)*$)";
    private static final String REGEX_TWO_PARAM_OR_TRANSLATION_KEY = "(^(?:(?!%s).)*%s(?:(?!%s).)*%s(?!.*%s).*$)|(^\\w+(\\.\\w+)*$)";
    private static final String REGEX_THREE_PARAM_OR_TRANSLATION_KEY = "(^(?:(?!%s).)*%s(?:(?!%s).)*%s(?:(?!%s).)*%s(?!.*%s).*$)|(^\\w+(\\.\\w+)*$)";
    public static final List<Parameter> PARAM_ALL = List.of(Parameter.TEAM_NAME, Parameter.SENDER, Parameter.CONTENT);
    public static final List<Parameter> PARAM_NAME = List.of(Parameter.SENDER, Parameter.CONTENT);

    public static ChatDecoration content(@Subst("%s") @Pattern(REGEX_ONE_PARAM_OR_TRANSLATION_KEY) String template) {
        return content(template, Style.style().build());
    }

    public static ChatDecoration content(@Pattern(REGEX_ONE_PARAM_OR_TRANSLATION_KEY) String template, Style style) {
        return new ChatDecoration(template, List.of(Parameter.CONTENT), style);
    }

    public static ChatDecoration contentWithSender(@Subst("%s%s") @Pattern(REGEX_TWO_PARAM_OR_TRANSLATION_KEY) String template) {
        return contentWithSender(template, Style.style().build());
    }

    public static ChatDecoration contentWithSender(@Pattern(REGEX_TWO_PARAM_OR_TRANSLATION_KEY) String template, Style style) {
        return new ChatDecoration(template, PARAM_NAME, style);
    }

    public static ChatDecoration full(@Subst("%s%s%s") @Pattern(REGEX_THREE_PARAM_OR_TRANSLATION_KEY) String template) {
        return full(template, Style.style().build());
    }

    public static ChatDecoration full(@Pattern(REGEX_THREE_PARAM_OR_TRANSLATION_KEY) String template, Style style) {
        return new ChatDecoration(template, PARAM_ALL, style);
    }

    public TextDisplay toTextDisplay() {
        return new TextDisplay(this);
    }

    @Override
    public void write(MutableNBTCompound compound) {
        compound.setString("translation_key", translationKey);
        compound.set("parameters", NBT.List(NBTType.TAG_String, params.stream().map(x -> NBT.String(x.name()
                .toLowerCase(Locale.ROOT))).collect(Collectors.toList())));
        MutableNBTCompound styleCompound;
        try {
            styleCompound = new MutableNBTCompound((NBTCompound) new SNBTParser(new StringReader(GsonComponentSerializer.gson().serialize(Component.empty().style(style)))).parse());
        } catch (NBTException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            MinecraftServer.LOGGER.error("Exception while parsing chat decoration style, falling back to empty style!", e);
            styleCompound = new MutableNBTCompound();
        }
        styleCompound.remove("text");
        compound.set("style", styleCompound.toCompound());
    }

    public enum Parameter {
        SENDER, TEAM_NAME, CONTENT
    }
}
