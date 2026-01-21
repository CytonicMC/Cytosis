package net.cytonic.cytosis.entity.npc.dialogs.element;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import net.cytonic.cytosis.entity.npc.dialogs.Dialog;
import net.cytonic.cytosis.entity.npc.dialogs.DialogElement;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

public record DialogOptionElement<P extends CytosisPlayer>(
    Component text,
    BiConsumer<P, Dialog<P>> callback,
    Predicate<CytosisPlayer> condition
) implements DialogElement<P> {

    @Override
    public void run(P player, Dialog<P> dialog, int index) {
        if (dialog.isFinished()) return;
        List<DialogOptionElement<P>> options = new ArrayList<>();
        for (int i = index; i < dialog.getElements().size(); i++) {
            if (dialog.getElements().get(i) instanceof DialogOptionElement<?> opt) {
                if (opt.condition().test(player)) {
                    //noinspection unchecked
                    options.add((DialogOptionElement<P>) opt);
                }
            } else {
                break;
            }
        }

        if (options.isEmpty()) {
            return;
        }

        player.sendMessage(Component.empty());
        for (DialogOptionElement<P> option : options) {
            Component clickable =
                Msg.mm("  [")
                    .append(option.text().color(NamedTextColor.YELLOW))
                    .append(Msg.mm("]"))
                    .clickEvent(ClickEvent.callback(_ -> {
                            if (dialog.isOptionGroupUsed(index)) {
                                return;
                            }

                            dialog.markOptionGroupUsed(index);
                            dialog.clearElements();
                            option.callback().accept(player, dialog);
                        },
                        ClickCallback.Options.builder()
                            .uses(1)
                            .lifetime(Duration.ofSeconds(30))
                            .build()));
            player.sendMessage(clickable);
        }
        dialog.end(player, false);
    }

    @Override
    public void sendNextElement(P player, Dialog<P> dialog, int index) {
        // Do nothing, we want to stop the dialog here
    }
}