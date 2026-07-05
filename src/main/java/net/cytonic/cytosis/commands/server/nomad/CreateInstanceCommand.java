package net.cytonic.cytosis.commands.server.nomad;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.managers.ServerInstancingManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Utils;

public class CreateInstanceCommand extends CytosisCommand {

    public CreateInstanceCommand() { //todo (Foxikle): Update this to reflect the current options
        super("createinstance");
        setCondition(CommandUtils.IS_ADMIN);
        ArgumentWord typeArg = ArgumentType.Word("type").from(ServerInstancingManager.TYPES);
        typeArg.setCallback(
            (sender, e) -> sender.sendMessage(Msg.whoops("Invalid type! Valid types are: cytosis, cynder")));
        typeArg.setSuggestionCallback((_, _, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("cytosis"));
            suggestion.addEntry(new SuggestionEntry("cynder"));
        });

        ArgumentInteger quantityArg = ArgumentType.Integer("quantity");
        quantityArg.setDefaultValue(1);
        quantityArg.between(1, 10);
        quantityArg.setCallback(
            (sender, _) -> sender.sendMessage(Msg.whoops("Invalid quantity! Valid quantities are: 1-10")));

        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            String type = context.get(typeArg).toLowerCase();
            if (!ServerInstancingManager.isServerType(type)) {
                player.whoops("Invalid instance type!");
                return;
            }
            int quantity = context.get(quantityArg);
            String niceName = Utils.captializeFirstLetters(type.replace("_", " "));
            Cytosis.get(ServerInstancingManager.class).createServerInstances(type, quantity);
            player.success("Dispatched the creation of new %s instance! It may take up to 30s to fully start up.",
                niceName);
        }, typeArg, quantityArg);
    }
}