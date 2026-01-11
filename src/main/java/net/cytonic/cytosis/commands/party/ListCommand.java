package net.cytonic.cytosis.commands.party;

import java.util.UUID;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.parties.PartyManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.protocol.data.objects.Party;

class ListCommand extends CytosisCommand {

    ListCommand() {
        super("list");

        setDefaultExecutor((sender, _) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            Party p = Cytosis.get(PartyManager.class).getPlayerParty(player.getUuid());
            if (p == null) {
                player.sendMessage(Msg.whoops("You are not in a party!"));
                return;
            }
            CytonicNetwork cn = Cytosis.get(CytonicNetwork.class);

            StringBuilder leader = new StringBuilder();
            if (cn.isOnline(p.getLeader())) {
                leader.append("<green>• ");
            } else {
                leader.append("<red>• ");
            }
            leader.append(cn.getMiniName(p.getLeader()));

            StringBuilder moderators = new StringBuilder();
            for (UUID moderator : p.getModerators()) {
                if (cn.isOnline(moderator)) {
                    moderators.append("<green>• ");
                } else {
                    moderators.append("<red>• ");
                }
                moderators
                    .append(cn.getMiniName(moderator))
                    .append(" ");
            }

            StringBuilder members = new StringBuilder();
            for (UUID member : p.getMembers()) {
                if (cn.isOnline(member)) {
                    members.append("<green>• ");
                } else {
                    members.append("<red>• ");
                }
                members
                    .append(cn.getMiniName(member))
                    .append(" ");
            }
            StringBuilder msg = new StringBuilder();
            msg.append(PartyManager.LINE)
                .append("<b><#65c6ea>Party Leader:</#65c6ea></b>\n")
                .append(leader).append("\n\n");

            if (!moderators.isEmpty()) {
                msg.append("<b><#65c6ea>Party Moderators:</#65c6ea></b>\n")
                    .append(moderators).append("\n\n");
            }

            if (!members.isEmpty()) {
                msg.append("<b><#65c6ea>Party Members:</#65c6ea></b>\n")
                    .append(members).append("\n\n");
            }
            msg.append(PartyManager.LINE);

            sender.sendMessage(Msg.mm(msg.toString()));
        });
    }
}
