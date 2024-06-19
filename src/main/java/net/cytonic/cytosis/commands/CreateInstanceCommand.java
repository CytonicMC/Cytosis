package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.Cytosis;
import net.minestom.server.command.builder.Command;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

public class CreateInstanceCommand extends Command {

    public CreateInstanceCommand() {
        //todo: specify different kinds of instances
        super("createinstance");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.createinstance"));
        addSyntax((sender, _) -> {
            if (!sender.hasPermission("cytosis.commands.createinstance")) {
                sender.sendMessage(MM."<RED>You do not have permission to use this command!");
                return;
            }
            Cytosis.getContainerizedInstanceManager().createCytosisInstance();
            sender.sendMessage(MM."<GREEN>Created new Cytosis instance! It may take a few seconds to fully start up.");
        });
    }
}
