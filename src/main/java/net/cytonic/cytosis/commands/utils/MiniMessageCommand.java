package net.cytonic.cytosis.commands.utils;

import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;

import net.cytonic.cytosis.utils.Msg;

public class MiniMessageCommand extends CytosisCommand {

    public MiniMessageCommand() {
        super("parsemini", "mm");
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((sender, _) -> sender.sendMessage("You must include a message to parse!"));
        ArgumentStringArray stringArg = ArgumentType.StringArray("message");
        addSyntax((sender, context) -> {
            String toParse = String.join(" ", context.get(stringArg));
            sender.sendMessage(Msg.mm(toParse));
        }, stringArg);
    }

}
