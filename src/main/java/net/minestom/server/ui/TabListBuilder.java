package net.minestom.server.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabListBuilder {

    public boolean hasPlayerList = false;

    public Component header = null;
    public Component footer = null;

    public final List<Component> beforeText = new ArrayList<>();
    public final List<PlayerSkin> beforeSkin = new ArrayList<>();
    public final List<Component> afterText = new ArrayList<>();
    public final List<PlayerSkin> afterSkin = new ArrayList<>();

    /*package-private*/ TabListBuilder() {}

    public TabList build() {
        return new TabListImpl(
                header, footer,
                hasPlayerList,
                beforeText, beforeSkin,
                afterText, afterSkin
        );
    }

    public TabListBuilder header(@NotNull Component header) {
        this.header = header;
        return this;
    }

    public TabListBuilder footer(@NotNull Component footer) {
        this.footer = footer;
        return this;
    }

    public TabListBuilder addBefore(@NotNull Component text, @Nullable PlayerSkin skin) {
        hasPlayerList = true;
        beforeText.add(text);
        beforeSkin.add(skin == null ? DEFAULT_SKIN_GRAY : skin);
        return this;
    }

    public TabListBuilder addAfter(@NotNull Component text, @Nullable PlayerSkin skin) {
        hasPlayerList = true;
        afterText.add(text);
        afterSkin.add(skin == null ? DEFAULT_SKIN_GRAY : skin);
        return this;
    }

    public TabListBuilder setBefore(@Range(from = 0, to = 80) int index, @NotNull Component text, @Nullable PlayerSkin skin) {
        return set(beforeText, beforeSkin, index, text, skin);
    }

    public TabListBuilder setAfter(@Range(from = 0, to = 80) int index, @NotNull Component text, @Nullable PlayerSkin skin) {
        return set(afterText, afterSkin, index, text, skin);
    }

    private TabListBuilder set(List<Component> textList, List<PlayerSkin> skinList, int index,
                               Component text, PlayerSkin skin) {
        while (textList.size() <= index) {
            textList.add(Component.empty());
            skinList.add(DEFAULT_SKIN_GRAY);
        }

        textList.set(index, text);
        skinList.set(index, skin == null ? DEFAULT_SKIN_GRAY : skin);

        return this;
    }

    public static final PlayerSkin DEFAULT_SKIN_GRAY = new PlayerSkin(
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNzcxNTc0MzIzMSwKICAicHJvZmlsZUlkIiA6ICI3NTE0NDQ4MTkxZTY0NTQ2OGM5NzM5YTZlMzk1N2JlYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGFua3NNb2phbmciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjQ2YTNkZmYyNTk4ZjBlZWQ3MGYwODI2YmQ1MWY0NGYzZTI1YmI5MmUxYTU5ODMxNWE2ZGEwMmViZGViMDcwNSIKICAgIH0KICB9Cn0=",
            "NsGdhmj7Hag3+1O64vCcMn/F91J9uIqL6vnbeGHA3nN8aeOGufxyHt7YqWh8wX2DrsfyuEE/9y03QmmmKQSjOs80eAjKmMNP6CKP85S0IUc236JqoVIQcERD/KymWswK3G0slSEuYr4lxSqbr7v06zM4H03V8HlkMAUEJB3OD14tSYiVqaA564c+Scq9+g3J+Hu20PP1YA0Ai5Iendo63O60regmRUS2QGMIJKvQH712HIUurmyPTb5zKCojjP3dCmNzoQW7tuBegTmoUcn9FQW/sjskyPKN+NhqYfPQHqzSVENHKGX5657LWYQdSnk3tnXnd6RhPlQ65zJKLfQptOofTT2F99Y50isbsIEgVYwKGfut+RvJH/m1+Z6nbbxnPBawDQDcRq00V2LNfR0y4UDRs8WgmSh1q56IQApeiMLn9KGNrJY/m5pwGuIQGUYUAxg7pJ5cTpcaCAHJQ5qkpzplVizWW9g6iKrn80Ozp9W6yqWK+7QKp88E1Mmtge3aOZ32RYxNVim/mDIrHRJPlRe+lvZsilzs62OIoQi1Gc3GFwggkLUJ4VOB6dcgarZ+DxhHSYR7sEcAcK3935LjOtyQf9WZ565R3CinYv2SP775ptIQMSLRBymkMgCiVcWk/Hd7gQYz2ucKhgzCe6ALhpNUpO7Sz816kccu6G6Q5PY="
    );

}
