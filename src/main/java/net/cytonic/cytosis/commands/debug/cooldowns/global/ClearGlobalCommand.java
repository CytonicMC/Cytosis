package net.cytonic.cytosis.commands.debug.cooldowns.global;

import net.kyori.adventure.key.Key;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.debug.cooldowns.CooldownCommand;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.managers.NetworkCooldownManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

public class ClearGlobalCommand extends CytosisCommand {

    public ClearGlobalCommand() {
        super("global");
        setDefaultExecutor(CooldownCommand.HELP_EXECUTOR);
        NetworkCooldownManager cooldown = Cytosis.CONTEXT.getComponent(NetworkCooldownManager.class);
        ArgumentWord nodeArg = ArgumentType.Word("node");
        nodeArg.setSuggestionCallback((sender, ctx, suggestion) -> {
            for (Key preference : cooldown.getAllKeys()) {
                suggestion.addEntry(new SuggestionEntry(preference.asString()));
            }
        });

        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) {
                sender.sendMessage("You cannot do this!");
                return;
            }

            Key node = Key.key(context.get(nodeArg));

            cooldown.resetGlobalCooldown(node);
            player.sendMessage(Msg.success("Reset the global cooldown <yellow>'%s'</yellow>.", node.asString()));
        }, nodeArg);
    }
}
