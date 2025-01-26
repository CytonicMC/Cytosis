package net.cytonic.cytosis.commands.staff.snooper;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

public class SnooperBlindCommand extends Command {
    public SnooperBlindCommand() {
        super("blind");
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((s, _) -> s.sendMessage(MM."<red><b>WHOOPS!</b></red><gray> You need to specify a channel!"));
        ArgumentWord channel = new ArgumentWord("channel");
        channel.setSuggestionCallback((commandSender, commandContext, suggestion) -> {
            if (!(commandSender instanceof CytosisPlayer player)) return;
            for (String s : Cytosis.getSnooperManager().getAllChannels(player)) {
                suggestion.addEntry(new SuggestionEntry(s));
            }
        });

        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            String rawChannel = context.get(channel);
            if (!Cytosis.getSnooperManager().getAllChannels(player).contains(rawChannel)) {
                player.sendMessage(MM."<red><b>WHOOPS!</b></red><gray> The channel '\{rawChannel}' either doesn't exist, or you don't have access to it.");
                return;
            }
            Cytosis.getSnooperManager().blind(player, rawChannel);
        }, channel);
    }
}
