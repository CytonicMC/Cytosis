package net.cytonic.cytosis.commands.debug.cooldowns.personal;

import net.kyori.adventure.key.Key;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.debug.cooldowns.ClearCooldownCommand;
import net.cytonic.cytosis.commands.debug.cooldowns.CooldownCommand;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.commands.utils.SubCommand;
import net.cytonic.cytosis.managers.NetworkCooldownManager;
import net.cytonic.cytosis.player.CytosisPlayer;

@SubCommand(ClearCooldownCommand.class)
public class ClearPersonalCommand extends CytosisCommand {

    public ClearPersonalCommand() {
        super("personal");
        setDefaultExecutor(CooldownCommand.HELP_EXECUTOR);
        NetworkCooldownManager cooldown = Cytosis.get(NetworkCooldownManager.class);
        ArgumentWord nodeArg = ArgumentType.Word("node");
        nodeArg.setSuggestionCallback((_, _, suggestion) -> {
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
            player.success("Reset your personal cooldown <yellow>'%s'</yellow>.", node.asString());
        }, nodeArg);
    }
}
