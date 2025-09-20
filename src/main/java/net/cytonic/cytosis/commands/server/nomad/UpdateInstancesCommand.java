package net.cytonic.cytosis.commands.server.nomad;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.managers.ServerInstancingManager;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Utils;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;

public class UpdateInstancesCommand extends CytosisCommand {
    public UpdateInstancesCommand() {
        super("updateinstances");
        setCondition(CommandUtils.IS_ADMIN);
        ArgumentWord typeArg = ArgumentType.Word("type").from(ServerInstancingManager.TYPES);

        addSyntax((sender, context) -> {
            String type = context.get(typeArg).toLowerCase();
            if (!ServerInstancingManager.isServerType(type)) {
                sender.sendMessage(Msg.whoops("Invalid instance type!"));
                return;
            }

            String niceName = Utils.captializeFirstLetters(type.replace("_", " "));
            Cytosis.CONTEXT.getComponent(ServerInstancingManager.class).updateServers(type);
            sender.sendMessage(Msg.success("Dispatched the update of every %s instance! It may take a while until every instance has been updated!", niceName));
        }, typeArg);
    }
}