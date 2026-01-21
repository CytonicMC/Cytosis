package net.cytonic.cytosis.entity.npc.dialogs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;

import net.cytonic.cytosis.entity.npc.NPC;
import net.cytonic.cytosis.entity.npc.dialogs.element.DialogActionElement;
import net.cytonic.cytosis.entity.npc.dialogs.element.DialogDelayElement;
import net.cytonic.cytosis.entity.npc.dialogs.element.DialogMessageElement;
import net.cytonic.cytosis.entity.npc.dialogs.element.DialogOptionElement;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

@Getter
@SuppressWarnings("unused")
public class Dialog {

    private final NPC npc;
    private final CytosisPlayer player;
    private final List<DialogElement> elements = new ArrayList<>();
    private final List<ClickCallback<?>> clickCallbacks = new ArrayList<>();
    private final Set<Integer> usedOptionGroups = new HashSet<>();
    private boolean finished = false;

    public Dialog(NPC npc, CytosisPlayer player) {
        this.npc = npc;
        this.player = player;
    }

    public Dialog message(String message) {
        return message(Msg.mm(message));
    }

    public Dialog message(Component message) {
        elements.add(new DialogMessageElement(message));
        return this;
    }

    public Dialog messageIf(boolean condition, Component message) {
        return messageIf(_ -> condition, message);
    }

    public Dialog messageIf(Function<CytosisPlayer, Boolean> condition, Component message) {
        if (condition.apply(player)) {
            elements.add(new DialogMessageElement(message));
        }
        return this;
    }

    public Dialog delay(int ticks) {
        elements.add(new DialogDelayElement(ticks));
        return this;
    }

    public Dialog option(String text, Consumer<Dialog> callback) {
        return option(Msg.mm(text), callback);
    }

    public Dialog option(Component text, Consumer<Dialog> callback) {
        elements.add(new DialogOptionElement(text, callback, p -> true));
        return this;
    }

    public Dialog optionIf(boolean condition, Component text, Consumer<Dialog> callback) {
        if (condition) {
            elements.add(new DialogOptionElement(text, callback, p -> true));
        }
        return this;
    }

    public Dialog optionWhen(Predicate<CytosisPlayer> predicate, Component text, Consumer<Dialog> callback) {
        elements.add(new DialogOptionElement(text, callback, predicate));
        return this;
    }

    public Dialog execute(Consumer<Dialog> action) {
        elements.add(new DialogActionElement(action));
        return this;
    }

    public void send() {
        if (elements.isEmpty()) {
            end();
            return;
        }
        finished = false;
        sendElements(0);
    }

    void sendElements(int index) {
        if (finished) {
            return;
        }
        if (index >= elements.size()) {
            end();
            return;
        }

        DialogElement element = elements.get(index);
        element.run(this, index);
    }

    public void markOptionGroupUsed(int startIndex) {
        usedOptionGroups.add(startIndex);
    }

    public void clearElements() {
        elements.clear();
        usedOptionGroups.clear();
        finished = false;
    }

    public boolean isOptionGroupUsed(int startIndex) {
        return usedOptionGroups.contains(startIndex);
    }

    public void end() {
        end(true);
    }

    public void end(boolean remove) {
        if (finished && !remove) return;
        finished = true;
        if (remove) npc.removeDialog(player);
    }
}
