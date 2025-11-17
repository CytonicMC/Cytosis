package net.cytonic.cytosis.commands.server.whitelist;

import net.kyori.adventure.text.Component;

import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.utils.Msg;

public class WhitelistCommand extends CytosisCommand {

    private static final Component HELP = Msg.mm("""
        <aqua><bold>Server Whitelist</bold></aqua>
        
        Server whitelists allow non-staff players to join the <red>Alpha</red> and <#ab2bfb>Development</#ab2bfb> \
        networks. <gray><i>Whitelists do not override bans.</i></gray>
        
        Usages: <i><gray>The network defaults to the current environment</gray></i>
        - /whitelist grant <player> [network]
        - /whitelist revoke <player> [network]
        - /whitelist status <player> [network]
        """);

    public WhitelistCommand() {
        super("whitelist");
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((sender, _) -> sender.sendMessage(HELP));
        addSubcommands(new GrantCommand(), new RevokeCommand(), new StatusCommand());
    }
}
