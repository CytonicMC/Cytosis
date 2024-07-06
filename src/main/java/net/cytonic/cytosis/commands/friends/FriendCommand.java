package net.cytonic.cytosis.commands.friends;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;

import static net.cytonic.utils.MiniMessageTemplate.MM;

public class FriendCommand extends Command {
    public FriendCommand() {
        super("friend", "f");
        setCondition(Conditions::playerOnly);

        var action = ArgumentType.Word("action").from("add", "remove", "list", "accept", "decline");
        action.setCallback((sender, exception) -> sender.sendMessage(MM."<red>Please specify an action!"));

        var target = ArgumentType.Word("target").setDefaultValue("");


    }
}
