package net.cytonic.cytosis.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.utils.StringUtils;

import java.util.*;

/**
 * A util class that wraps text components
 */
public final class ComponentWrapper {
    private ComponentWrapper() {
    }

    /**
     * Wraps the text component with a specified line length
     *
     * @param component The component to wrap
     * @param length    The line length
     * @return The list of wrapped components
     */
    public static List<Component> wrap(Component component, int length) {
        if (!(component instanceof TextComponent text)) return Collections.singletonList(component);
        var wrapped = new ArrayList<Component>();
        var parts = flatten(text);
        var currentLine = Component.empty();
        var lineLength = 0;
        for (var i = 0; i < parts.size(); i++) {
            var part = parts.get(i);
            var style = part.style();
            var content = part.content();
            var nextPart = i == parts.size() - 1 ? null : parts.get(i + 1);
            var join = nextPart != null && (part.content().endsWith(" ") || nextPart.content().startsWith(" "));
            var lineBuilder = new StringBuilder();
            var words = content.split(" ");
            words = Arrays.stream(words)
                    .flatMap(word -> Arrays.stream(word.splitWithDelimiters("\n", -1)))
                    .toArray(String[]::new);
            for (var j = 0; j < words.length; j++) {
                var word = words[j];
                var lastWord = j == words.length - 1;
                if (word.isEmpty()) continue;
                var isLongEnough = lineLength != 0 && lineLength + word.length() > length;
                var newLines = StringUtils.countMatches(word, '\n') + (isLongEnough ? 1 : 0);
                for (var k = 0; k < newLines; ++k) {
                    var endOfLine = lineBuilder.toString();
                    currentLine = currentLine.append(Component.text(endOfLine).style(style));
                    wrapped.add(currentLine);
                    lineLength = 0;
                    currentLine = Component.empty().style(style);
                    lineBuilder = new StringBuilder();
                }
                var addSpace = (!lastWord || join) && !word.endsWith("\n");
                var cleanWord = word.replace("\n", "");
                lineBuilder.append(cleanWord).append(addSpace ? " " : "");
                lineLength += word.length() + 1;
            }
            var endOfComponent = lineBuilder.toString();
            if (!endOfComponent.isEmpty())
                currentLine = currentLine.append(Component.text(endOfComponent).style(style));
        }
        if (lineLength > 0) wrapped.add(currentLine);
        return wrapped;
    }

    private static List<TextComponent> flatten(TextComponent component) {
        var flattened = new ArrayList<TextComponent>();
        var enforcedState = enforceStates(component.style());
        component = component.style(enforcedState);
        var toCheck = new Stack<TextComponent>();
        toCheck.add(component);
        while (!toCheck.empty()) {
            var parent = toCheck.pop();
            if (!parent.content().isEmpty()) flattened.add(parent);
            for (var child : parent.children().reversed()) {
                if (child instanceof TextComponent text) {
                    Style style = parent.style();
                    style = style.merge(child.style());
                    toCheck.add(text.style(style));
                } else {
                    toCheck.add(unsupported());
                }
            }
        }
        return flattened;
    }

    private static Style enforceStates(Style style) {
        var builder = style.toBuilder();
        style.decorations().forEach((decoration, state) -> {
            if (state == TextDecoration.State.NOT_SET) {
                builder.decoration(decoration, false);
            }
        });
        return builder.build();
    }

    private static TextComponent unsupported() {
        return Component.text("!CANNOT WRAP!").color(NamedTextColor.DARK_RED);
    }
}