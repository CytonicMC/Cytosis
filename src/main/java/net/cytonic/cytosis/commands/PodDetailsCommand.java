package net.cytonic.cytosis.commands;

import net.cytonic.cytosis.config.CytosisSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

public class PodDetailsCommand extends Command {

    public PodDetailsCommand() {
        super("poddetails");
        setCondition((sender, _) -> sender.hasPermission("cytosis.command.poddetails"));
        addSyntax(((sender, _) -> {
            if (!CytosisSettings.KUBERNETES_SUPPORTED) {
                sender.sendMessage(MM."<RED>This command is not supported on this server!");
                return;
            }
            if (sender instanceof Player player)
                if (player.hasPermission("cytosis.command.poddetails")) {
                    player.sendMessage(MM."<green>Fetching pod details...");
                    player.sendActionBar(MM."<green>Fetching pod details...");
                    String language = System.getenv("LANGUAGE");
                    String hostname = System.getenv("HOSTNAME");
                    String kubernetes_service_host = System.getenv("KUBERNETES_SERVICE_HOST");
                    String pwd = System.getenv("PWD");
                    Component message = MM."<GREEN>Language: \{language}\nHostname: \{hostname}\nKUBERNETES_SERVICE_HOST: \{kubernetes_service_host}\nPwd: \{pwd}";
                    player.sendMessage(message);
                } else sender.sendMessage(Component.text("Hey! You can't do this.", NamedTextColor.RED));
        }));
    }
}
