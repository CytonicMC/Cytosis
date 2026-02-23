package net.cytonic.cytosis.utils;

import java.util.List;
import java.util.function.Consumer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import net.cytonic.cytosis.data.objects.BanData;
import net.cytonic.cytosis.player.CytosisPlayer;

/**
 * A class that holds utils for formatting messages
 */
@SuppressWarnings("unused")
public interface Msg {

    /**
     * The overridable provider, for messages to be changed. To change it, use {@link Holder#set(Object)} to set the new
     * value.
     */
    Holder<SplashProvider> PROVIDER = new Holder<>(SplashProvider.DEFAULT);

    static List<Component> wrap(String minimessage) {
        return ComponentWrapper.wrap(mm(minimessage), 36);
    }

    static List<Component> wrap(String minimessage, int width) {
        return ComponentWrapper.wrap(mm(minimessage), width);
    }

    /**
     * Parses MiniMessage into a Component
     */
    static Component mm(String message, Object... args) {
        return MiniMessage.miniMessage().deserialize(String.format(message, args))
            .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    static Component whoops(String str, Object... args) {
        return mm(PROVIDER.get().whoops() + "<gray> " + str, args);
    }

    static Component serverError(String str, Object... args) {
        return mm(PROVIDER.get().serverError() + "<gray> " + str, args);
    }

    static Component success(String str, Object... args) {
        return mm(PROVIDER.get().success() + "<gray> " + str, args);
    }

    static Component network(String str, Object... args) {
        return mm(PROVIDER.get().network() + "<gray> " + str, args);
    }

    static Component tip(String str, Object... args) {
        return mm(PROVIDER.get().tip() + "<gray> " + str, args);
    }

    static Component snoop(Component component) {
        return mm(PROVIDER.get().snoop()).append(Msg.mm("<!i><!b>").append(component));
    }

    static Component aquaSplash(String splash, String text, Object... args) {
        return mm("<aqua><b>" + splash + "</b></aqua><gray> " + text, args);
    }

    static Component darkAquaSplash(String splash, String text, Object... args) {
        return mm("<dark_aqua><b>" + splash + "</b></dark_aqua><gray> " + text, args);
    }

    static Component greenSplash(String splash, String text, Object... args) {
        return mm("<green><b>" + splash + "</b></green><gray> " + text, args);
    }

    static Component darkGreenSplash(String splash, String text, Object... args) {
        return mm("<dark_green><b>" + splash + "</b></dark_green><gray> " + text, args);
    }

    static Component blueSplash(String splash, String text, Object... args) {
        return mm("<blue><b>" + splash + "</b></blue><gray> " + text, args);
    }

    static Component darkBlueSplash(String splash, String text, Object... args) {
        return mm("<dark_blue><b>" + splash + "</b></dark_blue><gray> " + text, args);
    }

    static Component redSplash(String splash, String text, Object... args) {
        return mm("<red><b>" + splash + "</b></red><gray> " + text, args);
    }

    static Component darkRedSplash(String splash, String text, Object... args) {
        return mm("<dark_red><b>" + splash + "</b></dark_red><gray> " + text, args);
    }

    static Component pinkSplash(String splash, String text, Object... args) {
        return mm("<light_purple><b>" + splash + "</b></light_purple><gray> " + text, args);
    }

    static Component purpleSplash(String splash, String text, Object... args) {
        return mm("<dark_purple><b>" + splash + "</b></dark_purple><gray> " + text, args);
    }

    static Component greySplash(String splash, String text, Object... args) {
        return mm("<gray><b>" + splash + "</b></gray><gray> " + text, args);
    }

    static Component darkGreySplash(String splash, String text, Object... args) {
        return mm("<dark_gray><b>" + splash + "</b></dark_gray><gray> " + text, args);
    }

    static Component yellowSplash(String splash, String text, Object... args) {
        return mm("<yellow><b>" + splash + "</b></yellow><gray> " + text, args);
    }

    static Component whiteSplash(String splash, String text, Object... args) {
        return mm("<white><b>" + splash + "</b></white><gray> " + text, args);
    }

    static Component blackSplash(String splash, String text, Object... args) {
        return mm("<black><b>" + splash + "</b></black><gray> " + text, args);
    }

    static Component goldSplash(String splash, String text, Object... args) {
        return mm("<gold><b>" + splash + "</b></gold><gray> " + text, args);
    }

    static Component splash(String splash, TextColor color, String text, Object... args) {
        return mm("<" + color.asHexString() + "><b>" + splash + "</b></" + color.asHexString() + "><gray> " + text,
            args);
    }

    static Component splash(String splash, String hex, String text, Object... args) {
        return mm("<#" + hex.replace("#", "") + "><b>" + splash + "</b></#" + hex.replace("#", "") + "><gray> " + text,
            args);
    }

    /**
     * Makes a badge with the specified DEFAULT COLOR("red", "yellow", ect)
     *
     * @param splash the splash text
     * @param color  the default minecraft color
     * @return the formatted badge
     */
    static Component coloredBadge(String splash, String color) {
        return mm("<" + color + "><b>" + splash + "</b></" + color + ">");
    }

    static Component badge(String splash, String hex) {
        return mm("<#" + hex.replace("#", "") + "><b>" + splash + "</b></#" + hex.replace("#", "") + ">");
    }

    static Component badge(String splash, TextColor color) {
        return mm("<" + color.asHexString() + "><b>" + splash + "</b></" + color.asHexString() + ">");
    }

    static Component aqua(String text, Object... args) {
        return mm("<aqua>" + text, args);
    }

    static Component darkAqua(String text, Object... args) {
        return mm("<dark_aqua>" + text, args);
    }

    static Component green(String text, Object... args) {
        return mm("<green>" + text, args);
    }

    static Component darkGreen(String text, Object... args) {
        return mm("<dark_green>" + text, args);
    }

    static Component blue(String text, Object... args) {
        return mm("<blue>" + text, args);
    }

    static Component darkBlue(String text, Object... args) {
        return mm("<dark_blue>" + text, args);
    }

    static Component red(String text, Object... args) {
        return mm("<red>" + text, args);
    }

    static Component darkRed(String text, Object... args) {
        return mm("<dark_red>" + text, args);
    }

    static Component pink(String text, Object... args) {
        return mm("<light_purple>" + text, args);
    }

    static Component purple(String text, Object... args) {
        return mm("<dark_purple>" + text, args);
    }

    static Component grey(String text, Object... args) {
        return mm("<gray>" + text, args);
    }

    static Component darkGrey(String text, Object... args) {
        return mm("<dark_gray>" + text, args);
    }

    static Component yellow(String text, Object... args) {
        return mm("<yellow>" + text, args);
    }

    static Component white(String text, Object... args) {
        return mm("<white>" + text, args);
    }

    static Component black(String text, Object... args) {
        return mm("<black>" + text, args);
    }

    static Component gold(String text, Object... args) {
        return mm("<gold>" + text, args);
    }

    static String stripTags(String str) {
        return MiniMessage.miniMessage().stripTags(str);
    }

    static String toText(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    static String toMini(Component component) {
        return MiniMessage.miniMessage().serialize(component);
    }

    static String toJson(Component component) {
        return JSONComponentSerializer.json().serialize(component);
    }

    static Component fromJson(String json) {
        return JSONComponentSerializer.json().deserialize(json);
    }

    static ClickEvent callback(Consumer<CytosisPlayer> c) {
        return ClickEvent.callback(audience -> {
            if (!(audience instanceof CytosisPlayer player)) return;
            c.accept(player);
        });
    }

    /**
     * Gets a formatted ban message
     *
     * @param banData the data to format
     * @return the formatted message in Component format
     */
    static Component formatBanMessage(BanData banData) {
        if (!banData.isBanned()) {
            return Component.empty();
        }

        return Component.empty()
            .append(Msg.mm("<red>You are currently banned from the Cytonic Network!").appendNewline().appendNewline())
            .append(Msg.mm("<gray>Reason:<gray> <white>" + banData.reason() + "</white>").appendNewline()).append(
                Msg.mm("<gray>Expires:</gray> <white>" + DurationParser.unparse(banData.expiry(), " ") + "</white>")
                    .appendNewline().appendNewline()).append(
                Msg.mm("<gray>Appeal at: </gray><aqua><underlined>https://cytonic.net</underlined></aqua>")
                    .appendNewline());
    }
}
