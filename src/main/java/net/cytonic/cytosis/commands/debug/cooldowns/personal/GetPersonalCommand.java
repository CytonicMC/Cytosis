package net.cytonic.cytosis.commands.debug.cooldowns.personal;

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

public class GetPersonalCommand extends CytosisCommand {

    public GetPersonalCommand() {
        super("personal");
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

            if (Cytosis.getNetworkCooldownManager().isOnPersonalCooldown(player.getUuid(), node)) {
                Instant expires = Cytosis.getNetworkCooldownManager().getPersonalExpiry(player.getUuid(), node);
                player.sendMessage(Msg.yellowSplash("TICK TOCK!",
                    "Your personal cooldown <yellow>'%s'/yellow> is set to expire in %s.", node.asString(),
                    DurationParser.unparseFull(expires)));
                return;
            }
            player.sendMessage(
                Msg.whoops("Your personal cooldown <yellow>'%s'/yellow> isn't active!", node.asString()));
        }, nodeArg);
    }
}
