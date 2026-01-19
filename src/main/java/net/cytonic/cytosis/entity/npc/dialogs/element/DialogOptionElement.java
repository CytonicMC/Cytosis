package net.cytonic.cytosis.entity.npc.dialogs.element;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import net.cytonic.cytosis.entity.npc.dialogs.Dialog;
import net.cytonic.cytosis.entity.npc.dialogs.DialogElement;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

public record DialogOptionElement(
    Component text,
    Consumer<Dialog> callback,
    Predicate<CytosisPlayer> condition
) implements DialogElement {

    @Override
    public void run(Dialog dialog, int index) {
        CytosisPlayer player = dialog.getPlayer();
        List<DialogOptionElement> options = new ArrayList<>();
        for (int i = index; i < dialog.getElements().size(); i++) {
            if (dialog.getElements().get(i) instanceof DialogOptionElement opt) {
                if (opt.condition().test(player)) {
                    options.add(opt);
                }
            } else {
                break;
            }
        }

        if (options.isEmpty()) {
            return;
        }

        player.sendMessage(Component.empty());
        for (DialogOptionElement option : options) {
            Component clickable =
                Msg.mm("  [")
                    .append(option.text().color(NamedTextColor.YELLOW))
                    .append(Msg.mm("]"))
                    .clickEvent(ClickEvent.callback(_ -> {
                            if (dialog.isOptionGroupUsed(index)) {
                                return;
                            }

                            dialog.markOptionGroupUsed(index);
                            option.callback().accept(dialog);
                        },
                        ClickCallback.Options.builder()
                            .uses(1)
                            .lifetime(Duration.ofSeconds(10))
                            .build()));
            player.sendMessage(clickable);
        }
    }
}