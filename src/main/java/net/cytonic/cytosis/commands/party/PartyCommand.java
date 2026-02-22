package net.cytonic.cytosis.commands.party;

import java.util.UUID;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.CytonicNetwork;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.parties.PartyManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.protocol.data.objects.Party;

public class PartyCommand extends CytosisCommand {

    public static final ArgumentWord PARTY_PLAYER = new ArgumentWord("party_player");

    static {
        PARTY_PLAYER.setSuggestionCallback((sender, _, suggestion) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            PartyManager pm = Cytosis.get(PartyManager.class);
            CytonicNetwork cn = Cytosis.get(CytonicNetwork.class);
            Party party = pm.getPlayerParty(player.getUuid());
            if (party == null) return;
            for (UUID p : party.getAllPlayers()) {
                if (p.equals(player.getUuid())) return;
                suggestion.addEntry(new SuggestionEntry(cn.getLifetimePlayers().getByKey(p)));
            }
        });
    }

    public PartyCommand() {
        super("party", "p");
        addSubcommands(new AcceptCommand(), new HelpCommand(), new JoinCommand(), new LeaveCommand(),
            new InviteCommand(), new ListCommand(), new KickCommand(), new PromoteCommand(), new TransferCommand(),
            new YoinkCommand(), new DisbandCommand(), new MuteCommand(), new OpenInvitesCommand(), new OpenCommand(),
            new DemoteCommand());

        setDefaultExecutor((s, _) -> MinecraftServer.getCommandManager().getDispatcher().execute(s, "party help"));

        addSyntax((s, c) -> MinecraftServer.getCommandManager().getDispatcher()
            .execute(s, "party invite " + c.get(CommandUtils.NETWORK_PLAYERS)), CommandUtils.NETWORK_PLAYERS);
    }
}
