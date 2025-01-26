package net.cytonic.cytosis.commands.moderation;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.auditlog.Category;
import net.cytonic.cytosis.auditlog.Entry;
import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import java.util.UUID;

public class UnbanCommand extends Command {

    public UnbanCommand() {
        super("unban");
        setCondition(CommandUtils.IS_MODERATOR);
        setDefaultExecutor((sender, ignored) -> sender.sendMessage(Msg.mm("<RED>Usage: /unban (player)")));
        var playerArg = ArgumentType.Word("target");
        playerArg.setSuggestionCallback((sender, ignored, suggestion) -> {
            if (sender instanceof CytosisPlayer player) {
                player.sendActionBar(Msg.mm("<green>Fetching banned players..."));
                Cytosis.getCytonicNetwork().getBannedPlayers().forEach((uuid, ignored1) -> suggestion.addEntry(new SuggestionEntry(Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(uuid))));
            }
        });
        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer actor)) {
                return;
            }

            final String player = context.get(playerArg);
            if (!Cytosis.getCytonicNetwork().getLifetimePlayers().containsValue(player)) {
                sender.sendMessage(Msg.mm("<red>The player " + player + " doesn't exist!"));
                return;
            }
            UUID uuid = Cytosis.getCytonicNetwork().getLifetimePlayers().getByValue(player);
            if (!Cytosis.getCytonicNetwork().getBannedPlayers().containsKey(uuid)) {
                sender.sendMessage(Msg.mm("<red>" + player + " is not banned!"));
                return;
            }
            Cytosis.getDatabaseManager().getMysqlDatabase().unbanPlayer(uuid, new Entry(uuid, actor.getUuid(), Category.UNBAN, "command"));
            sender.sendMessage(Msg.mm("<green><b>UNBANNED!</b></green><gray> " + player + " was successfully unbanned!"));
        }, playerArg);
    }
}
