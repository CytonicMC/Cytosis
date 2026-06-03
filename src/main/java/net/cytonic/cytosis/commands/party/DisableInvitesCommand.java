package net.cytonic.cytosis.commands.party;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Preferences;

class DisableInvitesCommand extends CytosisCommand {

    DisableInvitesCommand() {
        super("disableinvites");
        setDefaultExecutor((s, _) -> {
            if (!(s instanceof CytosisPlayer player)) return;
            player.togglePreference(Preferences.ACCEPT_PARTY_INVITES, () ->
                    player.sendMessage(
                        Msg.success("Other players will now be able to send you invitations to their parties.")),
                () ->
                    player.sendMessage(
                        Msg.success(
                            "Other players will now be <red>unable</red> to send you invitations to their parties.")));
        });
    }
}
