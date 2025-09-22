package net.cytonic.cytosis.commands.debug.cooldowns.personal;

import java.time.Instant;

import net.kyori.adventure.key.Key;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.debug.cooldowns.CooldownCommand;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.DurationParser;
import net.cytonic.cytosis.utils.Msg;

public class SetPersonalCommand extends CytosisCommand {

    public SetPersonalCommand() {
        super("personal");
        setDefaultExecutor(CooldownCommand.HELP_EXECUTOR);

        ArgumentWord nodeArg = ArgumentType.Word("node");
        nodeArg.setSuggestionCallback((sender, ctx, suggestion) -> {
            for (Key preference : Cytosis.getNetworkCooldownManager().getAllKeys()) {
                suggestion.addEntry(new SuggestionEntry(preference.asString()));
            }
        });
        ArgumentStringArray durationArg = ArgumentType.StringArray("duration");
        durationArg.setDefaultValue(new String[0]);

        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) {
                sender.sendMessage("You cannot do this!");
                return;
            }

            String[] duration = context.get(durationArg);

            if (duration.length == 0) {
                player.sendMessage(Msg.whoops("You must provide a duration!"));
                return;
            }

            Instant expiry = DurationParser.parse(String.join(" ", duration));
            Key node = Key.key(context.get(nodeArg));
            Cytosis.getNetworkCooldownManager().setPersonal(player.getUuid(), node, expiry);
            player.sendMessage(
                Msg.success("Set your personal cooldown <yellow>'%s'</yellow> to expire in %s.", node.asString(),
                    DurationParser.unparseFull(expiry)));

        }, nodeArg, durationArg);
    }
}
