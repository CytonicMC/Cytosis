package net.cytonic.cytosis.commands.debug.cooldowns.global;

import java.time.Instant;

import net.kyori.adventure.key.Key;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.debug.cooldowns.CooldownCommand;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.DurationParser;
import net.cytonic.cytosis.utils.Msg;

public class GetGlobalCommand extends CytosisCommand {

    public GetGlobalCommand() {
        super("global");
        setDefaultExecutor(CooldownCommand.HELP_EXECUTOR);

        ArgumentWord nodeArg = ArgumentType.Word("node");
        nodeArg.setSuggestionCallback((sender, ctx, suggestion) -> {
            for (Key preference : Cytosis.getNetworkCooldownManager().getAllKeys()) {
                suggestion.addEntry(new SuggestionEntry(preference.asString()));
            }
        });

        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) {
                sender.sendMessage("You cannot do this!");
                return;
            }

            Key node = Key.key(context.get(nodeArg));

            if (Cytosis.getNetworkCooldownManager().isOnGlobalCooldown(node)) {
                Instant expires = Cytosis.getNetworkCooldownManager().getGlobalExpiry(node);
                player.sendMessage(Msg.yellowSplash("TICK TOCK!",
                    "The global cooldown <yellow>'%s'/yellow> is set to expire in %s.", node.asString(),
                    DurationParser.unparseFull(expires)));
                return;
            }
            player.sendMessage(
                Msg.whoops("The global cooldown <yellow>'%s'/yellow> isn't active!", node.asString()));
        }, nodeArg);
    }
}
