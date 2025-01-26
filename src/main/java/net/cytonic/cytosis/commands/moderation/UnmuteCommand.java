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

public class UnmuteCommand extends Command {

    public UnmuteCommand() {
        super("unmute");
        setCondition(CommandUtils.IS_ADMIN);
        setDefaultExecutor((sender, ignored) -> sender.sendMessage(Msg.mm("<RED>Usage: /unmute (player)")));
        var playerArg = ArgumentType.Word("target");
        playerArg.setSuggestionCallback((sender, ignored, suggestion) -> {
            if (sender instanceof CytosisPlayer player) {
                player.sendActionBar(Msg.mm("<green>Fetching muted players..."));
                Cytosis.getCytonicNetwork().getMutedPlayers().forEach((uuid, ignored1) -> suggestion.addEntry(new SuggestionEntry(Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(uuid))));
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
            if (!Cytosis.getCytonicNetwork().getMutedPlayers().containsKey(uuid)) {
                sender.sendMessage(Msg.mm("<red>" + player + " is not muted!"));
                return;
            }
            Cytosis.getDatabaseManager().getMysqlDatabase().unmutePlayer(uuid, new Entry(uuid, actor.getUuid(), Category.UNMUTE, "command"));
            sender.sendMessage(Msg.mm("<GREEN><b>UNMUTED!</green> <gray>" + player + " was successfully unmuted!"));
        }, playerArg);
    }
}
