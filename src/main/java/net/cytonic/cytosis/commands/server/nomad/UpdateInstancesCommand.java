package net.cytonic.cytosis.commands.server.nomad;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.managers.ServerInstancingManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Utils;

public class UpdateInstancesCommand extends CytosisCommand {

    public UpdateInstancesCommand() {
        super("updateinstances");
        setCondition(CommandUtils.IS_ADMIN);
        ArgumentWord typeArg = ArgumentType.Word("type").from(ServerInstancingManager.TYPES);

        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            String type = context.get(typeArg).toLowerCase();
            if (!ServerInstancingManager.isServerType(type)) {
                player.whoops("Invalid instance type!");
                return;
            }

            String niceName = Utils.captializeFirstLetters(type.replace("_", " "));
            Cytosis.get(ServerInstancingManager.class).updateServers(type);
            player.success(
                "Dispatched the update of every %s instance! It may be a while until every instance has been updated!",
                niceName);
        }, typeArg);
    }
}