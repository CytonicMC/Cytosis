package net.cytonic.cytosis.commands.server.nomad;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.commands.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;

/**
 * A command to show the user the details of the pod the instance is running in
 */
public class AllocationDetailsCommand extends CytosisCommand {

    /**
     * See above, but details for pods.
     */
    public AllocationDetailsCommand() {
        super("allocationdetails", "allocation", "alloc");
        setCondition(CommandUtils.IS_ADMIN);
        addSyntax(((sender, cmdc) -> {

            if (!Cytosis.IS_NOMAD) {
                sender.sendMessage(Msg.whoops("This command is not only available on this server!"));
                return;
            }

            if (sender instanceof CytosisPlayer player) {
                player.sendMessage(Msg.network("""
                                Nomad Allocation Details:
                                ID         : %s
                                Job        : %s
                                Task Group : %s
                                Node       : %s
                                """,
                        System.getenv("NOMAD_ALLOC_ID"),
                        System.getenv("NOMAD_JOB_NAME"),
                        System.getenv("NOMAD_TASK_GROUP_NAME"),
                        System.getenv("NOMAD_NODE_NAME")));
            }
        }));
    }
}
