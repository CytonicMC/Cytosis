package net.cytonic.cytosis.entity.npc.dialogs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
public class Dialog<P extends CytosisPlayer> {

    private final NPC npc;
    private final List<DialogElement<P>> elements = new ArrayList<>();
    private final List<ClickCallback<?>> clickCallbacks = new ArrayList<>();
    private final Set<Integer> usedOptionGroups = new HashSet<>();
    private boolean finished = false;

    public Dialog(NPC npc) {
        this.npc = npc;
    }

    public Dialog<P> message(String message) {
        return message(Msg.mm(message));
    }

    public Dialog<P> message(Component message) {
        elements.add(new DialogMessageElement<>(message));
        return this;
    }

    public Dialog<P> messageIf(boolean condition, Component message) {
        return messageIf(() -> condition, message);
    }

    public Dialog<P> messageIf(Supplier<Boolean> condition, Component message) {
        if (condition.get()) {
            elements.add(new DialogMessageElement<>(message));
        }
        return this;
    }

    public Dialog<P> delay(int ticks) {
        elements.add(new DialogDelayElement<>(ticks));
        return this;
    }

    public Dialog<P> option(String text, BiConsumer<P, Dialog<P>> callback) {
        return option(Msg.mm(text), callback);
    }

    public Dialog<P> option(String text, Consumer<Dialog<P>> callback) {
        return option(Msg.mm(text), callback);
    }

    public Dialog<P> option(Component text, BiConsumer<P, Dialog<P>> callback) {
        elements.add(new DialogOptionElement<>(text, callback, p -> true));
        return this;
    }

    public Dialog<P> option(Component text, Consumer<Dialog<P>> callback) {
        elements.add(new DialogOptionElement<>(text, (_, dialog) -> callback.accept(dialog), p -> true));
        return this;
    }

    public Dialog<P> optionIf(boolean condition, Component text, BiConsumer<P, Dialog<P>> callback) {
        if (condition) {
            elements.add(new DialogOptionElement<>(text, callback, p -> true));
        }
        return this;
    }

    public Dialog<P> optionWhen(Predicate<CytosisPlayer> predicate, Component text, BiConsumer<P, Dialog<P>> callback) {
        elements.add(new DialogOptionElement<>(text, callback, predicate));
        return this;
    }

    public Dialog<P> execute(BiConsumer<P, Dialog<P>> action) {
        elements.add(new DialogActionElement<>(action));
        return this;
    }

    public void send(P player) {
        if (elements.isEmpty()) {
            end(player);
            return;
        }
        finished = false;
        sendElements(player, 0);
    }

    void sendElements(P player, int index) {
        if (finished) {
            return;
        }
        if (index >= elements.size()) {
            end(player);
            return;
        }

        DialogElement<P> element = elements.get(index);
        element.run(player, this, index);
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

    public void end(P player) {
        end(player, true);
    }

    public static <P extends CytosisPlayer> void end(P player, Dialog<P> dialog) {
        dialog.end(player);
    }

    public void end(P player, boolean remove) {
        if (finished && !remove) return;
        finished = true;
        if (remove) npc.removeDialog(player);
    }
}
