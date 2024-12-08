package net.cytonic.cytosis.commands.server;

import net.cytonic.cytosis.player.CytosisPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;

import static net.cytonic.utils.MiniMessageTemplate.MM;

/**
 * A command to show the user the details of the pod the instance is running in
 */
public class PodDetailsCommand extends Command {

    /**
     * See above, but details for pods.
     */
    public PodDetailsCommand() {
        super("poddetails", "pod");
        setCondition((sender, _) -> sender.hasPermission("cytosis.commands.poddetails"));
        addSyntax(((sender, _) -> {

            if (true) {
                sender.sendMessage(MM."<red><b>WHOOPS!</b><red><gray> This command is currently disabled.");
                return;
            }

            if (sender instanceof CytosisPlayer player)
                if (player.hasPermission("cytosis.command.poddetails")) {
                    player.sendMessage(MM."<green>Fetching pod details...");
                    player.sendActionBar(MM."<green>Fetching pod details...");
                    String language = System.getenv("LANGUAGE");
                    String hostname = System.getenv("HOSTNAME");
                    String kubernetes_service_host = System.getenv("KUBERNETES_SERVICE_HOST");
                    String pwd = System.getenv("PWD");
                    Component message = MM."<bold><yellow>Pod Details:</yellow></bold>\n\n<green>Language:</green><gray> \{language}\n<green>Pod Name: <gray>\{hostname}\n<green>Service Host: <gray>\{kubernetes_service_host}\n<green>Pwd: <gray>\{pwd}";
                    player.sendMessage(message);
                } else sender.sendMessage(Component.text("Hey! You can't do this.", NamedTextColor.RED));
        }));
    }
}
