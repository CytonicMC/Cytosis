package net.cytonic.cytosis.commands.server;

import net.cytonic.cytosis.commands.util.CommandUtils;
import net.cytonic.cytosis.commands.util.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.kyori.adventure.text.Component;

/**
 * A command to show the user the details of the pod the instance is running in
 */
public class PodDetailsCommand extends CytosisCommand {

    /**
     * See above, but details for pods.
     */
    public PodDetailsCommand() {
        super("poddetails", "pod");
        setCondition(CommandUtils.IS_ADMIN);
        addSyntax(((sender, cmdc) -> {

            if (true) {
                sender.sendMessage(Msg.whoops("This command is currently disabled."));
                return;
            }

            if (sender instanceof CytosisPlayer player) {
                player.sendMessage(Msg.mm("<green>Fetching pod details..."));
                player.sendActionBar(Msg.mm("<green>Fetching pod details..."));
                String language = System.getenv("LANGUAGE");
                String hostname = System.getenv("HOSTNAME");
                String kubernetes_service_host = System.getenv("KUBERNETES_SERVICE_HOST");
                String pwd = System.getenv("PWD");
                Component message = Msg.mm("<bold><yellow>Pod Details:</yellow></bold>\n\n<green>Language:</green><gray> " + language + "\n<green>Pod Name: <gray>" + hostname + "\n<green>Service Host: <gray>" + kubernetes_service_host + "\n<green>Pwd: <gray>" + pwd);
                player.sendMessage(message);
            }
        }));
    }
}
