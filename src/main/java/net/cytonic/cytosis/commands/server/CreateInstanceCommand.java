package net.cytonic.cytosis.commands.server;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisSettings;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * A command to create a new instance
 */
public class CreateInstanceCommand extends Command {

    /**
     * A command to create a new instance
     */
    public CreateInstanceCommand() {
        super("createinstance");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.createinstance"));
        var typeArg = ArgumentType.Word("type").from("cytosis", "proxy");
        typeArg.setCallback((sender, _) -> sender.sendMessage(MM."<red>ERROR"));
        typeArg.setSuggestionCallback((_, _, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("cytosis"));
            suggestion.addEntry(new SuggestionEntry("proxy"));
        });
        addSyntax((sender, context) -> {
            if (!sender.hasPermission("cytosis.commands.createinstance")) {
                sender.sendMessage(MM."<RED>You do not have permission to use this command!");
                return;
            }
            if (!CytosisSettings.KUBERNETES_SUPPORTED) {
                sender.sendMessage(MM."<RED>This command is not supported on this server!");
                return;
            }
            String type = context.get(typeArg);
            switch (type.toLowerCase()) {
                case "proxy" -> {
                    Cytosis.getContainerizedInstanceManager().createProxyInstance();
                    sender.sendMessage(MM."<GREEN>Created new Proxy instance! It may take a few seconds to fully start up.");
                }
                case "cytosis" -> {
                    Cytosis.getContainerizedInstanceManager().createCytosisInstance();
                    sender.sendMessage(MM."<GREEN>Created new Cytosis instance! It may take a few seconds to fully start up.");
                }
            }
        });
    }
}
