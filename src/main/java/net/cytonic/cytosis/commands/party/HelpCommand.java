package net.cytonic.cytosis.commands.party;

import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.parties.PartyManager;
import net.cytonic.cytosis.utils.Msg;

class HelpCommand extends CytosisCommand {

    private static final Component MSG = Msg.mm("""
        %s
        <b><#65c6ea>PARTY HELP</#65c6ea></b>
        <#83cae4>/party invite <player></#83cae4> Invites a player to your party
        <#83cae4>/party accept <player></#83cae4> Accepts a player's invite
        <#83cae4>/party promote <player></#83cae4> Promote a player to moderator
        <#83cae4>/party kick <player></#83cae4> Remove a player from the party
        <#83cae4>/party transfer <player></#83cae4> Transfer ownership of the party to another player
        <#83cae4>/party open</#83cae4> Allow any player to join via '<#83cae4>/party join <member></#83cae4>'
        <#83cae4>/party open-invites</#83cae4> Allows any player to send invitations
        <#83cae4>/party mute</#83cae4> Prevents players from speaking in party chat
        <#83cae4>/party join <player></#83cae4> An alternative way of joining a player's party.
        %s
        """, PartyManager.LINE, PartyManager.LINE);

    HelpCommand() {
        super("help");
        setDefaultExecutor((s, _) -> s.sendMessage(MSG));
    }
}
