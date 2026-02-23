package net.cytonic.cytosis.commands.server;

import java.util.UUID;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.event.ClickEvent;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.report.ReportManager;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.PlayerUtils;
import net.cytonic.cytosis.utils.Preferences;

public class ReportCommand extends CytosisCommand {


    public ReportCommand() {
        super("report");

        setDefaultExecutor((s, _) -> s.sendMessage(Msg.whoops("You must specify a player!")));

        addSyntax((s, ctx) -> {
            if (!(s instanceof CytosisPlayer player)) return;
            if (player.getPreference(Preferences.REPORT_BANNED)) {
                player.sendMessage(
                    Msg.whoops("You are no longer able to submit reports due to your abuse of the report system."));
                return;
            }
            String raw = ctx.get(CommandUtils.NETWORK_PLAYERS);

            String mini = Cytosis.get(CytonicNetwork.class).getMiniNameFragile(raw);
            if (mini == null) {
                player.sendMessage(Msg.whoops("Could not find a player with the name '%s'.", raw));
                return;
            }

            UUID target = PlayerUtils.resolveUuid(raw);
            if (target == null) {
                player.sendMessage(Msg.whoops("Could not find a player with the name '%s'.", raw));
                return;
            }
            if (player.getUuid().equals(target)) {
                player.sendMessage(Msg.whoops("You cannot report yourself!"));
                return;
            }

            player.openBook(getConfirmation(mini, target));
        }, CommandUtils.NETWORK_PLAYERS);
    }

    Book getConfirmation(String user, UUID target) {
        return Book.builder()
            .author(Msg.aqua("CytonicMC"))
            .addPage(Msg.mm("""
                    You are reporting %s. Is this correct?
                    
                    
                    
                    
                    
                    
                    
                    """, user)
                .append(Msg.darkGreen("<b>[YES, Continue]<b/>").clickEvent(
                    Msg.callback(p -> p.openBook(Cytosis.get(ReportManager.class).getTypesMenu(user, target))))
                ).appendNewline().appendNewline()
                .append(Msg.red("<b>[NO, Cancel]</b>").clickEvent(ClickEvent.runCommand("dummy"))))
            .title(Msg.white("Report Player Confirmation"))
            .build();
    }
}
