package net.cytonic.cytosis.commands.staff;

import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.commands.CytosisCommand;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.entity.Player;

/**
 * The class representing the fly command
 */
public class FlyCommand extends CytosisCommand {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public FlyCommand() {
        super("fly");
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((sender, cmdc) -> {
            if (sender instanceof final Player player) {

                if (!player.isAllowFlying()) {
                    player.setAllowFlying(true);
                    player.setFlying(true);
                    player.sendMessage(Msg.mm("<GREEN>Flight enabled."));
                } else {
                    player.setAllowFlying(false);
                    player.setFlying(false);
                    player.sendMessage(Msg.mm("<RED>Flight disabled."));
                }

            } else {
                sender.sendMessage(Msg.mm("<RED>Only players may execute this command!"));
            }
        });
    }
}
