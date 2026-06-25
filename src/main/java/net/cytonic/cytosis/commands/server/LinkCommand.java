package net.cytonic.cytosis.commands.server;

import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.protocol.impl.objects.AccountLinkProtocolObject;

public class LinkCommand extends CytosisCommand {

    public LinkCommand() {
        super("link");
        setDefaultExecutor((sender, _) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            player.sendMessage(Msg.grey("<i>Generating link token..."));
            new AccountLinkProtocolObject.Packet(player.getUuid()).request((response, throwable) -> {
                if (throwable != null) {
                    player.sendMessage(Msg.serverError("An error occurred whilst generating a token!"));
                    Logger.error("Failed to generate link token: ", throwable);
                    return;
                }
                if (response.error() != null && !response.error().isEmpty()) {
                    player.sendMessage(
                        Msg.serverError("An error occurred whilst generating a token! Please try again later"));
                    Logger.error("Failed to create link token. Server response: %s", response.error());
                    return;
                }
                player.sendMessage(Msg.success(
                    "Your account link token is <click:copy_to_clipboard:%s><b><green>%s</green></b></click>! Don't share this with anyone. It expires in 10 minutes.",
                    response.token(), response.token()));
            });
        });
    }
}
