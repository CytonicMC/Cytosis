package net.cytonic.cytosis.commands.staff.snooper;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.util.CommandUtils;
import net.cytonic.cytosis.commands.util.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

public class SnooperCommand extends CytosisCommand {

    public static final ArgumentWord CHANNELS = ArgumentType.Word("channels");

    static {
        CHANNELS.setSuggestionCallback((commandSender, commandContext, suggestion) -> {
            if (!(commandSender instanceof CytosisPlayer player)) return;
            CommandUtils.filterEntries(commandContext.get(CHANNELS), Cytosis.getSnooperManager().getAllChannels(player).stream().map(SuggestionEntry::new).toList())
                    .forEach(suggestion::addEntry);
        });
    }

    public SnooperCommand() {
        super("snooper");
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((sender, ctx) -> Cytosis.getCommandManager().execute(sender, "snooper help"));
        addSubcommand(new SnooperHelpCommand());
        addSubcommand(new SnooperListenCommand());
        addSubcommand(new SnooperBlindCommand());
        addSubcommand(new SnooperTestCommand());
        addSubcommand(new SnooperListCommand());
        addSubcommand(new SnooperMuteCommand());
        addSubcommand(new SnooperUnmuteCommand());
        addSubcommand(new SnooperAuditCommand());
        addSubcommand(new SnooperAboutCommand());
    }
}
